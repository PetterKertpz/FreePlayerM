// en: app/src/main/java/com/example/freeplayerm/data/repository/LocalMusicRepository.kt
package com.example.freeplayerm.data.repository

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import android.util.Log
import androidx.core.net.toUri
import com.example.freeplayerm.data.local.dao.CancionDao
import com.example.freeplayerm.data.local.entity.AlbumEntity
import com.example.freeplayerm.data.local.entity.ArtistaEntity
import com.example.freeplayerm.data.local.entity.CancionEntity
import com.example.freeplayerm.data.local.entity.GeneroEntity
import com.example.freeplayerm.utils.MusicTitleCleanerUtil
import com.example.freeplayerm.utils.StringSimilarityUtil
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ⚡ REPOSITORIO DE MÚSICA LOCAL - v2.0 OPTIMIZADO
 *
 * Características:
 * - Escaneo eficiente con inserción por batches
 * - Detección de archivos nuevos, modificados y eliminados
 * - Control de concurrencia con Mutex (suspendible)
 * - StateFlow para observar progreso del escaneo
 * - Parseo inteligente de metadatos
 * - Caché en memoria para reducir queries durante escaneo
 * - Limpieza automática de datos huérfanos
 *
 * @author Refactorizado para máximo rendimiento
 * @version 2.0
 */
@Singleton
class LocalMusicRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val cancionDao: CancionDao
) {
    companion object {
        private const val TAG = "MusicScanner"
        private const val BATCH_SIZE = 50
        private const val MIN_DURATION_MS = 10_000 // 10 segundos mínimo
        private const val PROGRESS_UPDATE_INTERVAL = 10
    }

    // ==================== ESTADO DEL ESCANEO ====================

    private val escaneoMutex = Mutex()

    private val _estadoEscaneo = MutableStateFlow<EstadoEscaneo>(EstadoEscaneo.Inactivo)
    val estadoEscaneo: StateFlow<EstadoEscaneo> = _estadoEscaneo.asStateFlow()

    sealed class EstadoEscaneo {
        data object Inactivo : EstadoEscaneo()
        data class Escaneando(
            val progreso: Int,
            val total: Int,
            val mensaje: String = "Escaneando..."
        ) : EstadoEscaneo()
        data class Completado(
            val nuevas: Int,
            val eliminadas: Int,
            val actualizadas: Int,
            val tiempoMs: Long
        ) : EstadoEscaneo()
        data class Error(val mensaje: String, val excepcion: Throwable? = null) : EstadoEscaneo()
    }

    // ==================== DATOS INTERNOS ====================

    private data class DatosCancionEscaneada(
        val uri: String,
        val titulo: String,
        val artista: String?,
        val album: String?,
        val albumId: Long,
        val duracionMs: Int,
        val anio: Int,
        val numeroPista: Int?,
        val fechaModificacion: Long
    )

    private data class ResultadoParseo(
        val artista: String,
        val titulo: String,
        val versionInfo: String?
    )

    private data class ExtraccionResultado(
        val artista: String,
        val titulo: String,
        val versionInfo: String?
    )

    // ==================== API PÚBLICA ====================

    /**
     * Ejecuta un escaneo completo de la biblioteca de música.
     *
     * @return Resultado del escaneo o null si ya había uno en progreso
     */
    suspend fun escanearYGuardarMusica(): EstadoEscaneo.Completado? {
        // Intentar adquirir el mutex sin bloquear
        if (!escaneoMutex.tryLock()) {
            Log.d(TAG, "⚠️ Escaneo ya en progreso, ignorando solicitud")
            return null
        }

        val tiempoInicio = System.currentTimeMillis()

        return try {
            withContext(Dispatchers.IO) {
                ejecutarEscaneoCompleto(tiempoInicio)
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error crítico durante el escaneo", e)
            val error = EstadoEscaneo.Error("Error al escanear: ${e.localizedMessage}", e)
            _estadoEscaneo.value = error
            null
        } finally {
            escaneoMutex.unlock()
            Log.d(TAG, "🔓 Mutex de escaneo liberado")
        }
    }

    /**
     * Verifica si hay un escaneo en progreso.
     */
    fun escaneoEnProgreso(): Boolean = escaneoMutex.isLocked

    /**
     * Reinicia el estado del escaneo a Inactivo.
     * Útil para limpiar mensajes de error o completado.
     */
    fun reiniciarEstado() {
        if (!escaneoMutex.isLocked) {
            _estadoEscaneo.value = EstadoEscaneo.Inactivo
        }
    }

    // ==================== LÓGICA DE ESCANEO ====================

    private suspend fun ejecutarEscaneoCompleto(tiempoInicio: Long): EstadoEscaneo.Completado {
        var cancionesNuevas = 0
        var cancionesEliminadas = 0
        var cancionesActualizadas = 0

        Log.d(TAG, "🎵 Iniciando escaneo de biblioteca musical...")
        _estadoEscaneo.value = EstadoEscaneo.Escaneando(0, 0, "Leyendo MediaStore...")

        // 1. Obtener datos de ambas fuentes
        val cancionesEnDispositivo = obtenerCancionesDeMediaStore()
        val cancionesEnBD = cancionDao.obtenerTodasLasCancionesSnapshot()

        Log.d(TAG, "📊 MediaStore: ${cancionesEnDispositivo.size} | BD: ${cancionesEnBD.size}")

        // 2. Crear índices para búsqueda eficiente O(1)
        val urisEnDispositivo = cancionesEnDispositivo.associateBy { it.uri }
        val cancionesPorUri = cancionesEnBD.associateBy { it.archivoPath }

        _estadoEscaneo.value = EstadoEscaneo.Escaneando(0, cancionesEnDispositivo.size, "Sincronizando...")

        // 3. Detectar y eliminar canciones huérfanas (ya no existen en dispositivo)
        val cancionesAEliminar = cancionesEnBD.filter { it.archivoPath !in urisEnDispositivo }
        if (cancionesAEliminar.isNotEmpty()) {
            val idsAEliminar = cancionesAEliminar.map { it.idCancion }
            cancionDao.eliminarCancionesPorIds(idsAEliminar)
            cancionesEliminadas = cancionesAEliminar.size
            Log.d(TAG, "🗑️ Eliminadas $cancionesEliminadas canciones huérfanas")
        }

        // 4. Procesar canciones nuevas y modificadas
        val cancionesParaInsertar = mutableListOf<DatosCancionEscaneada>()
        val cancionesParaActualizar = mutableListOf<Pair<CancionEntity, DatosCancionEscaneada>>()

        cancionesEnDispositivo.forEachIndexed { index, datos ->
            val cancionExistente = cancionesPorUri[datos.uri]

            when {
                // Canción nueva
                cancionExistente == null -> {
                    cancionesParaInsertar.add(datos)
                }
                // Canción modificada (fecha diferente)
                cancionExistente.fechaModificacion != datos.fechaModificacion -> {
                    cancionesParaActualizar.add(cancionExistente to datos)
                }
                // Sin cambios - ignorar
            }

            // Actualizar progreso
            if (index % PROGRESS_UPDATE_INTERVAL == 0) {
                _estadoEscaneo.value = EstadoEscaneo.Escaneando(
                    progreso = index + 1,
                    total = cancionesEnDispositivo.size,
                    mensaje = "Procesando ${index + 1}/${cancionesEnDispositivo.size}..."
                )
            }
        }

        // 5. Insertar nuevas canciones por batches
        if (cancionesParaInsertar.isNotEmpty()) {
            Log.d(TAG, "➕ Insertando ${cancionesParaInsertar.size} canciones nuevas...")
            _estadoEscaneo.value = EstadoEscaneo.Escaneando(
                progreso = 0,
                total = cancionesParaInsertar.size,
                mensaje = "Guardando canciones nuevas..."
            )

            cancionesParaInsertar.chunked(BATCH_SIZE).forEachIndexed { batchIndex, batch ->
                insertarBatchDeCanciones(batch)
                _estadoEscaneo.value = EstadoEscaneo.Escaneando(
                    progreso = (batchIndex + 1) * BATCH_SIZE,
                    total = cancionesParaInsertar.size,
                    mensaje = "Guardando lote ${batchIndex + 1}..."
                )
            }
            cancionesNuevas = cancionesParaInsertar.size
        }

        // 6. Actualizar canciones modificadas
        if (cancionesParaActualizar.isNotEmpty()) {
            Log.d(TAG, "🔄 Actualizando ${cancionesParaActualizar.size} canciones modificadas...")
            cancionesParaActualizar.forEach { (existente, nuevos) ->
                actualizarCancionExistente(existente, nuevos)
            }
            cancionesActualizadas = cancionesParaActualizar.size
        }

        // 7. Limpiar datos huérfanos (artistas, álbumes, géneros sin canciones)
        _estadoEscaneo.value = EstadoEscaneo.Escaneando(0, 0, "Limpiando datos huérfanos...")
        val datosLimpiados = cancionDao.limpiarDatosHuerfanos()
        if (datosLimpiados > 0) {
            Log.d(TAG, "🧹 Limpiados $datosLimpiados registros huérfanos")
        }

        // 8. Preparar resultado
        val tiempoTotal = System.currentTimeMillis() - tiempoInicio
        val resultado = EstadoEscaneo.Completado(
            nuevas = cancionesNuevas,
            eliminadas = cancionesEliminadas,
            actualizadas = cancionesActualizadas,
            tiempoMs = tiempoTotal
        )

        _estadoEscaneo.value = resultado
        Log.d(TAG, "✅ Escaneo completado en ${tiempoTotal}ms: +$cancionesNuevas, -$cancionesEliminadas, ~$cancionesActualizadas")

        return resultado
    }

    // ==================== LECTURA DE MEDIASTORE ====================

    private fun obtenerCancionesDeMediaStore(): List<DatosCancionEscaneada> {
        val canciones = mutableListOf<DatosCancionEscaneada>()

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.YEAR,
            MediaStore.Audio.Media.TRACK,
            MediaStore.Audio.Media.DATE_MODIFIED
        )

        val selection = buildString {
            append("${MediaStore.Audio.Media.IS_MUSIC} != 0")
            append(" AND ${MediaStore.Audio.Media.DURATION} >= $MIN_DURATION_MS")
        }

        val sortOrder = "${MediaStore.Audio.Media.DATE_MODIFIED} DESC"

        try {
            context.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                null,
                sortOrder
            )?.use { cursor ->
                // Obtener índices de columnas una sola vez
                val colId = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val colTitulo = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val colArtista = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val colAlbum = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val colAlbumId = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
                val colDuracion = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val colAnio = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.YEAR)
                val colPista = cursor.getColumnIndex(MediaStore.Audio.Media.TRACK)
                val colFechaMod = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_MODIFIED)

                while (cursor.moveToNext()) {
                    try {
                        val id = cursor.getLong(colId)
                        val uri = ContentUris.withAppendedId(
                            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                            id
                        ).toString()

                        val artista = cursor.getString(colArtista)?.let {
                            if (it == "<unknown>" || it.isBlank()) null else it.trim()
                        }

                        val album = cursor.getString(colAlbum)?.let {
                            if (it.isBlank()) null else it.trim()
                        }

                        canciones.add(
                            DatosCancionEscaneada(
                                uri = uri,
                                titulo = cursor.getString(colTitulo)?.trim() ?: "Título Desconocido",
                                artista = artista,
                                album = album,
                                albumId = cursor.getLong(colAlbumId),
                                duracionMs = cursor.getInt(colDuracion),
                                anio = cursor.getInt(colAnio),
                                numeroPista = if (colPista >= 0) {
                                    cursor.getInt(colPista).takeIf { it > 0 }
                                } else null,
                                fechaModificacion = cursor.getLong(colFechaMod)
                            )
                        )
                    } catch (e: Exception) {
                        Log.w(TAG, "⚠️ Error leyendo fila del cursor", e)
                    }
                }

                Log.d(TAG, "📂 Leídas ${canciones.size} canciones de MediaStore")
            } ?: Log.w(TAG, "⚠️ Cursor de MediaStore es null")
        } catch (e: SecurityException) {
            Log.e(TAG, "❌ Sin permisos para acceder a MediaStore", e)
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error consultando MediaStore", e)
            throw e
        }

        return canciones
    }

    // ==================== INSERCIÓN POR BATCHES ====================

    private suspend fun insertarBatchDeCanciones(batch: List<DatosCancionEscaneada>) {
        // Cachés para evitar queries repetidas dentro del batch
        val artistasCache = mutableMapOf<String, ArtistaEntity>()
        val albumesCache = mutableMapOf<String, AlbumEntity>()
        val generoPorDefecto = obtenerOCrearGenero("Género Desconocido")

        val entidades = batch.mapNotNull { datos ->
            try {
                val resultado = parsearCancionInteligente(datos.titulo, datos.artista)

                // Obtener o crear artista (con caché)
                val artista = artistasCache.getOrPut(resultado.artista.lowercase()) {
                    obtenerOCrearArtista(resultado.artista)
                }

                // Obtener o crear álbum (con caché por nombre+artista)
                val albumNombre = datos.album ?: "Álbum Desconocido"
                val albumKey = "${albumNombre.lowercase()}_${artista.idArtista}"
                val album = albumesCache.getOrPut(albumKey) {
                    obtenerOCrearAlbum(albumNombre, artista.idArtista, datos.anio, datos.albumId.toInt())
                }

                CancionEntity(
                    idArtista = artista.idArtista,
                    idAlbum = album.idAlbum,
                    idGenero = generoPorDefecto.idGenero,
                    titulo = resultado.titulo,
                    duracionSegundos = datos.duracionMs / 1000,
                    origen = CancionEntity.ORIGEN_LOCAL,
                    archivoPath = datos.uri,
                    numeroPista = datos.numeroPista,
                    anio = datos.anio.takeIf { it > 0 },
                    fechaAgregado = System.currentTimeMillis(),
                    fechaModificacion = datos.fechaModificacion
                )
            } catch (e: Exception) {
                Log.w(TAG, "⚠️ Error procesando: ${datos.titulo}", e)
                null
            }
        }

        if (entidades.isNotEmpty()) {
            cancionDao.insertarCanciones(entidades)
        }
    }

    // ==================== ACTUALIZACIÓN DE CANCIONES ====================

    private suspend fun actualizarCancionExistente(
        existente: CancionEntity,
        nuevos: DatosCancionEscaneada
    ) {
        try {
            val resultado = parsearCancionInteligente(nuevos.titulo, nuevos.artista)
            val artista = obtenerOCrearArtista(resultado.artista)
            val album = obtenerOCrearAlbum(
                nuevos.album ?: "Álbum Desconocido",
                artista.idArtista,
                nuevos.anio,
                nuevos.albumId.toInt()
            )

            val cancionActualizada = existente.copy(
                titulo = resultado.titulo,
                idArtista = artista.idArtista,
                idAlbum = album.idAlbum,
                duracionSegundos = nuevos.duracionMs / 1000,
                numeroPista = nuevos.numeroPista,
                anio = nuevos.anio.takeIf { it > 0 },
                fechaModificacion = nuevos.fechaModificacion
            )

            cancionDao.actualizarCancion(cancionActualizada)
        } catch (e: Exception) {
            Log.w(TAG, "⚠️ Error actualizando: ${existente.titulo}", e)
        }
    }

    // ==================== PARSEO INTELIGENTE DE METADATOS ====================

    private fun parsearCancionInteligente(tituloCrudo: String, artistaCrudo: String?): ResultadoParseo {
        // Normalizar separadores comunes
        val textoNormalizado = tituloCrudo
            .replace(Regex("""[|\\/]"""), " - ")
            .replace(Regex("""\s+"""), " ")
            .trim()

        // Limpiar artista de metadatos
        val artistaDeMetadatos = artistaCrudo?.takeIf {
            it.isNotBlank() && it != "<unknown>"
        }?.let {
            MusicTitleCleanerUtil.cleanArtistName(it.trim())
        }

        val resultadoExtraccion = extraerArtistaYTitulo(textoNormalizado, artistaDeMetadatos)

        val artistaLimpio = MusicTitleCleanerUtil.cleanArtistName(resultadoExtraccion.artista)
        val tituloLimpio = MusicTitleCleanerUtil.cleanTitle(resultadoExtraccion.titulo)

        return ResultadoParseo(
            artista = capitalizarNombrePropio(artistaLimpio),
            titulo = capitalizarNombrePropio(tituloLimpio),
            versionInfo = resultadoExtraccion.versionInfo
        )
    }

    private fun extraerArtistaYTitulo(texto: String, artistaDeMetadatos: String?): ExtraccionResultado {
        // Patrones de separadores ordenados por prioridad
        val patronesSeparadores = listOf(
            Regex("""\s+-\s+"""),      // " - "
            Regex("""\s+–\s+"""),      // " – " (en dash)
            Regex("""\s+—\s+"""),      // " — " (em dash)
            Regex("""\s+\|\|\s+"""),   // " || "
            Regex("""\s+\|\s+"""),     // " | "
            Regex("""\s+//\s+"""),     // " // "
            Regex("""\s+::\s+""")      // " :: "
        )

        for (patron in patronesSeparadores) {
            if (texto.contains(patron)) {
                val partes = texto.split(patron, limit = 2)
                if (partes.size == 2) {
                    val posibleArtista = partes[0].trim()
                    val posibleTitulo = partes[1].trim()

                    // Calcular confianza de cada lado
                    val confianzaIzquierda = calcularConfianzaArtista(posibleArtista, artistaDeMetadatos)
                    val confianzaDerecha = calcularConfianzaArtista(posibleTitulo, artistaDeMetadatos)

                    return if (confianzaIzquierda > confianzaDerecha || confianzaIzquierda > 0.6) {
                        val parsedTitle = MusicTitleCleanerUtil.parseTitle(posibleTitulo)
                        ExtraccionResultado(
                            artista = posibleArtista,
                            titulo = parsedTitle.mainTitle,
                            versionInfo = parsedTitle.version?.name
                        )
                    } else {
                        val parsedTitle = MusicTitleCleanerUtil.parseTitle(posibleArtista)
                        ExtraccionResultado(
                            artista = posibleTitulo,
                            titulo = parsedTitle.mainTitle,
                            versionInfo = parsedTitle.version?.name
                        )
                    }
                }
            }
        }

        // Si hay artista en metadatos, usarlo directamente
        if (artistaDeMetadatos != null) {
            var tituloLimpio = texto
            patronesSeparadores.forEach { patron ->
                tituloLimpio = patron.replace(tituloLimpio, " ")
            }
            val parsedTitle = MusicTitleCleanerUtil.parseTitle(tituloLimpio.trim())
            return ExtraccionResultado(
                artista = artistaDeMetadatos,
                titulo = parsedTitle.mainTitle,
                versionInfo = parsedTitle.version?.name
            )
        }

        // Fallback: sin artista identificable
        val parsedTitle = MusicTitleCleanerUtil.parseTitle(texto)
        return ExtraccionResultado(
            artista = "Artista Desconocido",
            titulo = parsedTitle.mainTitle,
            versionInfo = parsedTitle.version?.name
        )
    }

    private fun calcularConfianzaArtista(textoCandidata: String, artistaReferencia: String?): Double {
        if (artistaReferencia == null) return 0.3

        val similitud = StringSimilarityUtil.calculateSimilarity(textoCandidata, artistaReferencia)

        return when {
            similitud > 0.8 -> 1.0
            similitud > 0.5 -> 0.7
            similitud > 0.3 -> 0.4
            else -> {
                val similitudTokenizada = StringSimilarityUtil.calculateTokenizedSimilarity(
                    textoCandidata,
                    artistaReferencia
                )
                if (similitudTokenizada > 0.4) 0.6 else 0.2
            }
        }
    }

    private fun capitalizarNombrePropio(texto: String): String {
        if (texto.isBlank()) return texto

        val palabrasMinusculas = setOf(
            "de", "del", "la", "las", "el", "los", "y", "e", "o", "u",
            "a", "vs", "ft", "feat", "con", "en", "por", "para",
            "the", "of", "and", "or", "to", "in", "on", "at", "for"
        )

        return texto.split(" ").mapIndexed { index, palabra ->
            when {
                // Preservar acrónimos (TODO, DJ, etc.)
                palabra.matches(Regex("""^[A-Z0-9$#]+$""")) -> palabra
                // Preservar URLs/dominios
                palabra.contains(".") && !palabra.endsWith(".") -> palabra
                // Primera palabra siempre capitalizada
                index == 0 -> palabra.lowercase().replaceFirstChar { it.uppercase() }
                // Palabras funcionales en minúscula
                palabra.lowercase() in palabrasMinusculas -> palabra.lowercase()
                // Resto capitalizado
                else -> palabra.lowercase().replaceFirstChar { it.uppercase() }
            }
        }.joinToString(" ")
    }

    // ==================== GESTIÓN DE ENTIDADES RELACIONADAS ====================

    private suspend fun obtenerOCrearArtista(nombre: String): ArtistaEntity {
        val nombreNormalizado = nombre.trim()

        return cancionDao.obtenerArtistaPorNombre(nombreNormalizado)
            ?: run {
                val nuevoArtista = ArtistaEntity(
                    nombre = nombreNormalizado,
                    paisOrigen = null,
                    descripcion = null
                )
                cancionDao.insertarArtista(nuevoArtista)
                // Re-obtener para tener el ID generado
                cancionDao.obtenerArtistaPorNombre(nombreNormalizado)
                    ?: throw IllegalStateException("No se pudo crear el artista: $nombreNormalizado")
            }
    }

    private suspend fun obtenerOCrearAlbum(
        titulo: String,
        artistaId: Int,
        anio: Int,
        albumIdMediaStore: Int
    ): AlbumEntity {
        val tituloNormalizado = titulo.trim()

        return cancionDao.obtenerAlbumPorNombreYArtista(tituloNormalizado, artistaId)
            ?: run {
                // Construir y validar URI de carátula
                val uriCaratula = ContentUris.withAppendedId(
                    "content://media/external/audio/albumart".toUri(),
                    albumIdMediaStore.toLong()
                )
                val portadaValida = validarUriPortada(uriCaratula)

                val nuevoAlbum = AlbumEntity(
                    idArtista = artistaId,
                    titulo = tituloNormalizado,
                    anio = anio.takeIf { it > 0 }?.toLong(),
                    portadaPath = if (portadaValida) uriCaratula.toString() else null
                )
                cancionDao.insertarAlbum(nuevoAlbum)

                cancionDao.obtenerAlbumPorNombreYArtista(tituloNormalizado, artistaId)
                    ?: throw IllegalStateException("No se pudo crear el álbum: $tituloNormalizado")
            }
    }

    private suspend fun obtenerOCrearGenero(nombre: String): GeneroEntity {
        val nombreNormalizado = nombre.trim()

        return cancionDao.obtenerGeneroPorNombre(nombreNormalizado)
            ?: run {
                cancionDao.insertarGenero(GeneroEntity(nombre = nombreNormalizado))
                cancionDao.obtenerGeneroPorNombre(nombreNormalizado)
                    ?: throw IllegalStateException("No se pudo crear el género: $nombreNormalizado")
            }
    }

    // ==================== VALIDACIÓN DE PORTADAS ====================

    private fun validarUriPortada(uri: android.net.Uri): Boolean {
        return try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                stream.read() != -1
            } ?: false
        } catch (e: java.io.FileNotFoundException) {
            // Muy común: álbum sin artwork
            false
        } catch (e: SecurityException) {
            Log.w(TAG, "⚠️ Sin permisos para portada: $uri")
            false
        } catch (e: Exception) {
            Log.d(TAG, "⚠️ Portada no disponible: ${e.message}")
            false
        }
    }
}