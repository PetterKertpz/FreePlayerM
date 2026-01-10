// en: app/src/main/java/com/example/freeplayerm/data/repository/LocalMusicRepository.kt
package com.example.freeplayerm.data.repository

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.provider.MediaStore
import android.util.Log
import androidx.core.net.toUri
import com.example.freeplayerm.data.local.dao.AlbumDao
import com.example.freeplayerm.data.local.dao.ArtistDao
import com.example.freeplayerm.data.local.dao.GenreDao
import com.example.freeplayerm.data.local.dao.SongDao
import com.example.freeplayerm.data.local.entity.SongEntity
import com.example.freeplayerm.utils.MusicTitleCleaner
import com.example.freeplayerm.utils.PermissionHelper
import com.example.freeplayerm.utils.ScanNotificationHelper
import com.example.freeplayerm.utils.StringSimilarity
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Collections
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.coroutineContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout

@Singleton
class LocalMusicRepository
@Inject
constructor(
   @ApplicationContext private val context: Context,
   private val songDao: SongDao,
   private val artistDao: ArtistDao,
   private val albumDao: AlbumDao,
   private val genreDao: GenreDao,
   private val notificationHelper: ScanNotificationHelper,
) {
   companion object {
      private const val TAG = "MusicScanner"
      private const val BATCH_SIZE = 50
      private const val MIN_DURATION_MS = 10_000
      private const val PROGRESS_UPDATE_INTERVAL = 10
      private const val MAX_CACHE_SIZE = 500
      private const val SCAN_TIMEOUT_MS = 5 * 60 * 1000L
   }

   private val escaneoMutex = Mutex()
   private val _estadoEscaneo = MutableStateFlow<EstadoEscaneo>(EstadoEscaneo.Inactivo)
   val estadoEscaneo: StateFlow<EstadoEscaneo> = _estadoEscaneo.asStateFlow()

   // Caché persistente durante escaneo
   // Caché LRU con límite de tamaño para evitar OOM
   private val artistasCache =
      Collections.synchronizedMap(
         object : LinkedHashMap<String, Int>(128, 0.75f, true) {
            override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, Int>?) =
               size > MAX_CACHE_SIZE
         }
      )
   private val albumesCache =
      Collections.synchronizedMap(
         object : LinkedHashMap<String, Int>(128, 0.75f, true) {
            override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, Int>?) =
               size > MAX_CACHE_SIZE
         }
      )
   private val generosCache =
      Collections.synchronizedMap(
         object : LinkedHashMap<String, Int>(64, 0.75f, true) {
            override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, Int>?) =
               size > MAX_CACHE_SIZE
         }
      )

   sealed class EstadoEscaneo {
      data object Inactivo : EstadoEscaneo()

      data class Escaneando(
         val fase: FaseEscaneo,
         val progreso: Int,
         val total: Int,
         val mensaje: String = "Escaneando...",
      ) : EstadoEscaneo()

      data class Completado(
         val nuevas: Int,
         val eliminadas: Int,
         val actualizadas: Int,
         val tiempoMs: Long,
      ) : EstadoEscaneo()

      data class Error(val mensaje: String, val excepcion: Throwable? = null) : EstadoEscaneo()
   }

   enum class FaseEscaneo(val descripcion: String, val peso: Float) {
      LEYENDO_MEDIASTORE("Leyendo dispositivo", 0.20f),
      OBTENIENDO_GENEROS("Obteniendo géneros", 0.10f),
      ANALIZANDO_CAMBIOS("Analizando cambios", 0.15f),
      ELIMINANDO_HUERFANAS("Limpiando eliminadas", 0.05f),
      INSERTANDO_NUEVAS("Guardando nuevas", 0.30f),
      ACTUALIZANDO_EXISTENTES("Actualizando", 0.15f),
      LIMPIANDO_DATOS("Limpieza final", 0.05f);

      // Calcula progreso global considerando peso de cada fase
      fun progresoGlobal(progresoFase: Float, fasesCompletadas: List<FaseEscaneo>): Float {
         val pesoCompletado = fasesCompletadas.sumOf { it.peso.toDouble() }.toFloat()
         return pesoCompletado + (peso * progresoFase)
      }
   }

   private data class DatosCancionEscaneada(
      val uri: String,
      val titulo: String,
      val artista: String?,
      val album: String?,
      val albumId: Long,
      val duracionMs: Int,
      val anio: Int,
      val numeroPista: Int?,
      val fechaModificacion: Long,
      val genero: String?,
      val tamanioBytes: Long?,
      val mimeType: String?,
      val hashRapido: String,
   )

   private data class ResultadoParseo(
      val artista: String,
      val titulo: String,
      val versionInfo: String?,
   )

   suspend fun escanearYGuardarMusica(forceFullScan: Boolean = false): EstadoEscaneo.Completado? {

      // Verificar permisos antes de iniciar
      if (!PermissionHelper.hasStoragePermissions(context)) {
         Log.e(TAG, "Permisos de almacenamiento no otorgados")
         _estadoEscaneo.value =
            EstadoEscaneo.Error(
               mensaje = "Permisos de almacenamiento requeridos",
               excepcion = SecurityException("READ_MEDIA_AUDIO/READ_EXTERNAL_STORAGE no otorgado"),
            )
         notificationHelper.mostrarNotificacionError("Permisos de almacenamiento requeridos")
         return null
      }
      if (!escaneoMutex.tryLock()) {
         Log.d(TAG, "Escaneo ya en progreso, ignorando")
         return null
      }
      notificationHelper.mostrarNotificacionInicio()
      val tiempoInicio = System.currentTimeMillis()

      return try {
         withContext(Dispatchers.IO) {
               withTimeout(SCAN_TIMEOUT_MS) { // ✅ Agregar timeout
                  ejecutarEscaneo(tiempoInicio, forceFullScan)
               }
            }
            .also { resultado ->
               notificationHelper.mostrarNotificacionCompletada(
                  nuevas = resultado.nuevas,
                  eliminadas = resultado.eliminadas,
                  actualizadas = resultado.actualizadas,
                  tiempoMs = resultado.tiempoMs,
               )
            }
      } catch (e: TimeoutCancellationException) {
         Log.e(TAG, "Escaneo cancelado por timeout (${SCAN_TIMEOUT_MS}ms)")
         _estadoEscaneo.value = EstadoEscaneo.Error("Timeout: escaneo tomó demasiado tiempo", e)
         notificationHelper.mostrarNotificacionError("El escaneo tomó demasiado tiempo")
         null
      } catch (e: CancellationException) {
         Log.d(TAG, "Escaneo cancelado")
         _estadoEscaneo.value = EstadoEscaneo.Inactivo
         notificationHelper.cancelarNotificacion()
         notificationHelper.mostrarNotificacionError("Permisos insuficientes")
         null
      } catch (e: Exception) {
         Log.e(TAG, "Error crítico durante el escaneo", e)
         _estadoEscaneo.value = EstadoEscaneo.Error("Error: ${e.localizedMessage}", e)
         notificationHelper.mostrarNotificacionError(e.localizedMessage ?: "Error desconocido")
         null
      } finally {
         limpiarCaches()
         escaneoMutex.unlock()
      }
   }

   fun escaneoEnProgreso(): Boolean = escaneoMutex.isLocked

   fun reiniciarEstado() {
      if (!escaneoMutex.isLocked) {
         _estadoEscaneo.value = EstadoEscaneo.Inactivo
      }
   }

   private suspend fun ejecutarEscaneo(
      tiempoInicio: Long,
      forceFullScan: Boolean,
   ): EstadoEscaneo.Completado {
      var cancionesNuevas = 0
      var cancionesEliminadas = 0
      var cancionesActualizadas = 0
      val fasesCompletadas = mutableListOf<FaseEscaneo>()

      Log.d(TAG, "Iniciando escaneo (forzado=$forceFullScan)...")

      // FASE 1: Leer MediaStore
      reportarFase(FaseEscaneo.LEYENDO_MEDIASTORE, 0, 1, fasesCompletadas)
      val cancionesEnDispositivo = obtenerCancionesDeMediaStore()
      fasesCompletadas.add(FaseEscaneo.LEYENDO_MEDIASTORE)

      // FASE 2: Obtener géneros (ya optimizado)
      reportarFase(FaseEscaneo.OBTENIENDO_GENEROS, 0, 1, fasesCompletadas)
      val generosPorCancion = obtenerGenerosDeMediaStore()
      val cancionesConGenero =
         cancionesEnDispositivo.map { cancion ->
            cancion.copy(genero = generosPorCancion[cancion.uri])
         }
      fasesCompletadas.add(FaseEscaneo.OBTENIENDO_GENEROS)

      // FASE 3: Analizar cambios
      reportarFase(FaseEscaneo.ANALIZANDO_CAMBIOS, 0, cancionesConGenero.size, fasesCompletadas)

      val cancionesEnBD =
         if (forceFullScan) {
            emptyList()
         } else {
            songDao.obtenerInfoParaEscaneo()
         }

      Log.d(TAG, "MediaStore: ${cancionesConGenero.size} | BD: ${cancionesEnBD.size}")

      // Crear índices O(1)
      val urisEnDispositivo = cancionesConGenero.associateBy { it.uri }
      val cancionesPorUri = cancionesEnBD.associateBy { it.archivoPath }

      // Clasificar canciones
      val cancionesParaInsertar = mutableListOf<DatosCancionEscaneada>()
      val cancionesParaActualizar = mutableListOf<Pair<Int, DatosCancionEscaneada>>()

      cancionesConGenero.forEachIndexed { index, datos ->
         currentCoroutineContext().ensureActive()

         val existente = cancionesPorUri[datos.uri]
         when {
            existente == null -> cancionesParaInsertar.add(datos)
            forceFullScan -> cancionesParaActualizar.add(existente.idCancion to datos)
            // Comparar por hash si está disponible, sino por fecha
            existente.hashArchivo != null && existente.hashArchivo != datos.hashRapido -> {
               cancionesParaActualizar.add(existente.idCancion to datos)
            }
            existente.hashArchivo == null &&
               existente.fechaModificacion != datos.fechaModificacion -> {
               cancionesParaActualizar.add(existente.idCancion to datos)
            }
         }

         if (index % PROGRESS_UPDATE_INTERVAL == 0) {
            reportarFase(
               FaseEscaneo.ANALIZANDO_CAMBIOS,
               index + 1,
               cancionesConGenero.size,
               fasesCompletadas,
            )
         }
      }
      fasesCompletadas.add(FaseEscaneo.ANALIZANDO_CAMBIOS)

      // FASE 4: Detectar y eliminar huérfanas
      reportarFase(FaseEscaneo.ELIMINANDO_HUERFANAS, 0, 1, fasesCompletadas)
      coroutineContext.ensureActive()

      val pathsAEliminar =
         cancionesEnBD.filter { it.archivoPath !in urisEnDispositivo }.map { it.archivoPath }

      if (pathsAEliminar.isNotEmpty()) {
         // Eliminar en batches para evitar query muy larga
         pathsAEliminar.chunked(BATCH_SIZE).forEach { batch ->
            songDao.eliminarCancionesPorPaths(batch)
         }
         cancionesEliminadas = pathsAEliminar.size
         Log.d(TAG, "Eliminadas $cancionesEliminadas canciones huérfanas")
      }
      fasesCompletadas.add(FaseEscaneo.ELIMINANDO_HUERFANAS)

      // FASE 5: Insertar nuevas por batches
      if (cancionesParaInsertar.isNotEmpty()) {
         Log.d(TAG, "Insertando ${cancionesParaInsertar.size} canciones nuevas...")

         cancionesParaInsertar.chunked(BATCH_SIZE).forEachIndexed { batchIndex, batch ->
            currentCoroutineContext().ensureActive()
            val progreso = minOf((batchIndex + 1) * BATCH_SIZE, cancionesParaInsertar.size)
            reportarFase(
               FaseEscaneo.INSERTANDO_NUEVAS,
               progreso,
               cancionesParaInsertar.size,
               fasesCompletadas,
            )
            insertarBatchConTransaccion(batch)
         }
         cancionesNuevas = cancionesParaInsertar.size
      }
      fasesCompletadas.add(FaseEscaneo.INSERTANDO_NUEVAS)

      // FASE 6: Actualizar modificadas
      if (cancionesParaActualizar.isNotEmpty()) {
         Log.d(TAG, "Actualizando ${cancionesParaActualizar.size} canciones...")

         cancionesParaActualizar.forEachIndexed { index, (idCancion, datos) ->
            coroutineContext.ensureActive()
            if (index % PROGRESS_UPDATE_INTERVAL == 0) {
               reportarFase(
                  FaseEscaneo.ACTUALIZANDO_EXISTENTES,
                  index + 1,
                  cancionesParaActualizar.size,
                  fasesCompletadas,
               )
            }
            actualizarCancion(idCancion, datos)
         }
         cancionesActualizadas = cancionesParaActualizar.size
      }
      fasesCompletadas.add(FaseEscaneo.ACTUALIZANDO_EXISTENTES)

      // FASE 7: Limpiar huérfanos
      coroutineContext.ensureActive()
      reportarFase(FaseEscaneo.LIMPIANDO_DATOS, 0, 1, fasesCompletadas)
      val datosLimpiados = songDao.limpiarDatosHuerfanos()
      if (datosLimpiados > 0) {
         Log.d(TAG, "Limpiados $datosLimpiados registros huérfanos")
      }
      fasesCompletadas.add(FaseEscaneo.LIMPIANDO_DATOS)

      val tiempoTotal = System.currentTimeMillis() - tiempoInicio
      val resultado =
         EstadoEscaneo.Completado(
            nuevas = cancionesNuevas,
            eliminadas = cancionesEliminadas,
            actualizadas = cancionesActualizadas,
            tiempoMs = tiempoTotal,
         )

      _estadoEscaneo.value = resultado
      Log.d(
         TAG,
         "Escaneo completado en ${tiempoTotal}ms: +$cancionesNuevas, -$cancionesEliminadas, ~$cancionesActualizadas",
      )

      return resultado
   }
   
   private fun reportarFase(
      fase: FaseEscaneo,
      progreso: Int,
      total: Int,
      fasesCompletadas: List<FaseEscaneo>,
   ) {
      val progresoFase = if (total > 0) progreso.toFloat() / total else 1f
      val progresoGlobal = fase.progresoGlobal(progresoFase, fasesCompletadas)
      val progresoInt = (progresoGlobal * 100).toInt()
      val mensaje = "${fase.descripcion}... ${if (total > 0) "$progreso/$total" else ""}".trim()
      
      _estadoEscaneo.value = EstadoEscaneo.Escaneando(
         fase = fase,
         progreso = progresoInt,
         total = 100,
         mensaje = mensaje,
      )
      
      // Actualizar notificación cada 10% o cada fase
      if (progresoInt % 10 == 0 || progreso == 0) {
         notificationHelper.actualizarProgreso(
            progreso = progresoInt,
            total = 100,
            mensaje = if (total > 0) "$progreso/$total" else "Procesando...",
            fase = fase.descripcion
         )
      }
   }

   private fun obtenerCancionesDeMediaStore(): List<DatosCancionEscaneada> {
      val canciones = mutableListOf<DatosCancionEscaneada>()

      val projection =
         arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.YEAR,
            MediaStore.Audio.Media.TRACK,
            MediaStore.Audio.Media.DATE_MODIFIED,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.MIME_TYPE,
         )

      val selection =
         "${MediaStore.Audio.Media.IS_MUSIC} != 0 AND ${MediaStore.Audio.Media.DURATION} >= $MIN_DURATION_MS"
      val sortOrder = "${MediaStore.Audio.Media.DATE_MODIFIED} DESC"

      // Escanear EXTERNAL e INTERNAL
      val uris =
         listOf(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            MediaStore.Audio.Media.INTERNAL_CONTENT_URI,
         )

      uris.forEach { contentUri ->
         try {
            context.contentResolver
               .query(contentUri, projection, selection, null, sortOrder)
               ?.use { cursor -> canciones.addAll(procesarCursor(cursor, contentUri)) }
         } catch (e: Exception) {
            Log.w(TAG, "Error leyendo $contentUri: ${e.message}")
         }
      }
      return canciones
   }

   private fun procesarCursor(
      cursor: Cursor,
      baseUri: android.net.Uri,
   ): List<DatosCancionEscaneada> {
      val canciones = mutableListOf<DatosCancionEscaneada>()

      val colId = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
      val colTitulo = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
      val colArtista = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
      val colAlbum = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
      val colAlbumId = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
      val colDuracion = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
      val colAnio = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.YEAR)
      val colPista = cursor.getColumnIndex(MediaStore.Audio.Media.TRACK)
      val colFechaMod = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_MODIFIED)
      val colSize = cursor.getColumnIndex(MediaStore.Audio.Media.SIZE)
      val colMime = cursor.getColumnIndex(MediaStore.Audio.Media.MIME_TYPE)

      var contador = 0
      while (cursor.moveToNext()) {
         // ✅ Cancelación cooperativa cada 50 items
         if (++contador % 50 == 0 && Thread.currentThread().isInterrupted) {
            Log.d(TAG, "Procesamiento de cursor interrumpido")
            break
         }

         try {
            val id = cursor.getLong(colId)
            val uri = ContentUris.withAppendedId(baseUri, id).toString()
            val fechaMod = cursor.getLong(colFechaMod)
            val tamanio = if (colSize >= 0) cursor.getLong(colSize) else null
            val artista =
               cursor.getString(colArtista)?.let {
                  if (it == "<unknown>" || it.isBlank()) null else it.trim()
               }
            val album = cursor.getString(colAlbum)?.let { if (it.isBlank()) null else it.trim() }

            canciones.add(
               DatosCancionEscaneada(
                  uri = uri,
                  titulo = cursor.getString(colTitulo)?.trim() ?: "Titulo Desconocido",
                  artista = artista,
                  album = album,
                  albumId = cursor.getLong(colAlbumId),
                  duracionMs = cursor.getInt(colDuracion),
                  anio = cursor.getInt(colAnio),
                  numeroPista =
                     if (colPista >= 0) cursor.getInt(colPista).takeIf { it > 0 } else null,
                  fechaModificacion = fechaMod,
                  genero = null,
                  tamanioBytes = tamanio,
                  mimeType = if (colMime >= 0) cursor.getString(colMime) else null,
                  hashRapido = calcularHashRapido(uri, tamanio, fechaMod),
               )
            )
         } catch (e: Exception) {
            Log.w(TAG, "Error leyendo fila del cursor", e)
         }
      }

      return canciones
   }

   private fun obtenerGenerosDeMediaStore(): Map<String, String> {
      val generosPorCancion = mutableMapOf<String, String>()

      try {
         // Paso 1: Obtener todos los géneros en una sola query
         val generosMap = mutableMapOf<Long, String>()
         val genresUri = MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI

         context.contentResolver
            .query(
               genresUri,
               arrayOf(MediaStore.Audio.Genres._ID, MediaStore.Audio.Genres.NAME),
               null,
               null,
               null,
            )
            ?.use { cursor ->
               val colId = cursor.getColumnIndexOrThrow(MediaStore.Audio.Genres._ID)
               val colName = cursor.getColumnIndexOrThrow(MediaStore.Audio.Genres.NAME)

               while (cursor.moveToNext()) {
                  val id = cursor.getLong(colId)
                  val name = cursor.getString(colName)
                  if (!name.isNullOrBlank()) {
                     generosMap[id] = name
                  }
               }
            }

         if (generosMap.isEmpty()) return emptyMap()

         // Paso 2: Obtener miembros de todos los géneros en batch
         generosMap.forEach { (genreId, genreName) ->
            try {
               val membersUri = MediaStore.Audio.Genres.Members.getContentUri("external", genreId)
               context.contentResolver
                  .query(membersUri, arrayOf(MediaStore.Audio.Genres.Members._ID), null, null, null)
                  ?.use { membersCursor ->
                     val colMemberId =
                        membersCursor.getColumnIndexOrThrow(MediaStore.Audio.Genres.Members._ID)
                     while (membersCursor.moveToNext()) {
                        val audioId = membersCursor.getLong(colMemberId)
                        val audioUri =
                           ContentUris.withAppendedId(
                                 MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                                 audioId,
                              )
                              .toString()
                        // Solo guardar si no existe (primer género encontrado tiene prioridad)
                        if (!generosPorCancion.containsKey(audioUri)) {
                           generosPorCancion[audioUri] = genreName
                        }
                     }
                  }
            } catch (e: Exception) {
               Log.w(TAG, "Error leyendo miembros del género $genreName: ${e.message}")
            }
         }
      } catch (e: Exception) {
         Log.w(TAG, "Error obteniendo géneros: ${e.message}")
      }

      return generosPorCancion
   }

   private suspend fun insertarBatchConTransaccion(batch: List<DatosCancionEscaneada>) {
      try {
         // ✅ Procesar y agregar en una sola transacción
         songDao.insertarCancionesEnTransaccion(
            batch.mapNotNull { datos ->
               try {
                  crearSongEntity(datos)
               } catch (e: Exception) {
                  Log.w(TAG, "Error procesando: ${datos.titulo}", e)
                  null
               }
            }
         )
      } catch (e: Exception) {
         Log.e(TAG, "Error en transacción de batch", e)
         // ✅ Reintentar con sub-batches más pequeños
         val subBatchSize = BATCH_SIZE / 2
         batch.chunked(subBatchSize).forEach { subBatch ->
            try {
               songDao.insertarCancionesEnTransaccion(
                  subBatch.mapNotNull { datos ->
                     try {
                        crearSongEntity(datos)
                     } catch (e2: Exception) {
                        Log.w(TAG, "Error en sub-batch: ${datos.titulo}", e2)
                        null
                     }
                  }
               )
            } catch (e3: Exception) {
               Log.e(TAG, "Error en sub-batch, insertando individualmente", e3)
               // Último recurso: uno a uno
               subBatch.forEach { datos ->
                  try {
                     songDao.upsertCancion(crearSongEntity(datos))
                  } catch (e4: Exception) {
                     Log.w(TAG, "Error insertando individual: ${datos.titulo}", e4)
                  }
               }
            }
         }
      }
   }

   private suspend fun actualizarCancion(idCancion: Int, datos: DatosCancionEscaneada) {
      try {
         val resultado = parsearCancionInteligente(datos.titulo, datos.artista)
         val artistaId = obtenerOCrearArtistaId(resultado.artista)
         val albumId =
            obtenerOCrearAlbumId(
               datos.album ?: "Album Desconocido",
               artistaId,
               datos.anio,
               datos.albumId,
               datos.uri,
            )
         val generoId = obtenerOCrearGeneroId(datos.genero ?: "Genero Desconocido")

         songDao.actualizarMetadatosEscaneo(
            idCancion = idCancion,
            titulo = resultado.titulo,
            idArtista = artistaId,
            idAlbum = albumId,
            idGenero = generoId,
            duracion = datos.duracionMs / 1000,
            numeroPista = datos.numeroPista,
            anio = datos.anio.takeIf { it > 0 },
            fechaModificacion = datos.fechaModificacion,
            tamanioBytes = datos.tamanioBytes,
            mimeType = datos.mimeType,
         )
      } catch (e: Exception) {
         Log.w(TAG, "Error actualizando cancion $idCancion", e)
      }
   }

   private suspend fun crearSongEntity(datos: DatosCancionEscaneada): SongEntity {
      val resultado = parsearCancionInteligente(datos.titulo, datos.artista)
      val artistaId = obtenerOCrearArtistaId(resultado.artista)
      val albumId =
         obtenerOCrearAlbumId(
            datos.album ?: "Album Desconocido",
            artistaId,
            datos.anio,
            datos.albumId,
            datos.uri,
         )
      val generoId = obtenerOCrearGeneroId(datos.genero ?: "Genero Desconocido")

      return SongEntity(
         idArtista = artistaId,
         idAlbum = albumId,
         idGenero = generoId,
         titulo = resultado.titulo,
         duracionSegundos = datos.duracionMs / 1000,
         origen = SongEntity.ORIGEN_LOCAL,
         archivoPath = datos.uri,
         numeroPista = datos.numeroPista,
         anio = datos.anio.takeIf { it > 0 },
         fechaAgregado = System.currentTimeMillis(),
         fechaModificacion = datos.fechaModificacion,
         tamanioBytes = datos.tamanioBytes,
         mimeType = datos.mimeType,
         hashArchivo = datos.hashRapido,
      )
   }

   // Funciones de obtener/crear con caché
   private suspend fun obtenerOCrearArtistaId(nombre: String): Int {
      val nombreKey = nombre.lowercase().trim()
      artistasCache[nombreKey]?.let {
         return it
      }

      val artista = artistDao.obtenerOCrearArtista(nombre)
      artistasCache[nombreKey] = artista.idArtista
      return artista.idArtista
   }

   private suspend fun obtenerOCrearAlbumId(
      titulo: String,
      artistaId: Int,
      anio: Int,
      albumIdMediaStore: Long,
      audioUri: String?,
   ): Int {
      val albumKey = "${titulo.lowercase()}_$artistaId"
      albumesCache[albumKey]?.let {
         return it
      }

      val album = albumDao.obtenerOCrearAlbum(titulo, artistaId, anio.toLong().takeIf { it > 0 })

      // Actualizar portada si no tiene
      if (album.portadaPath == null) {
         val portada = obtenerPortadaAlbum(albumIdMediaStore.toInt(), audioUri, titulo)
         if (portada != null) {
            albumDao.actualizarInformacionBasica(
               album.idAlbum,
               album.titulo,
               album.descripcion,
               portada,
            )
         }
      }

      albumesCache[albumKey] = album.idAlbum
      return album.idAlbum
   }

   private suspend fun obtenerOCrearGeneroId(nombre: String): Int {
      val nombreKey = nombre.lowercase().trim()
      generosCache[nombreKey]?.let {
         return it
      }

      val genero = genreDao.obtenerOCrearGenero(nombre)
      generosCache[nombreKey] = genero.idGenero
      return genero.idGenero
   }

   private fun limpiarCaches() {
      artistasCache.clear()
      albumesCache.clear()
      generosCache.clear()
   }

   private fun parsearCancionInteligente(
      tituloCrudo: String,
      artistaCrudo: String?,
   ): ResultadoParseo {
      val textoNormalizado =
         tituloCrudo.replace(Regex("""[|\\/]"""), " - ").replace(Regex("""\s+"""), " ").trim()

      val artistaDeMetadatos =
         artistaCrudo
            ?.takeIf { it.isNotBlank() && it != "<unknown>" }
            ?.let { MusicTitleCleaner.cleanArtistName(it.trim()) }

      val (artista, titulo, version) = extraerArtistaYTitulo(textoNormalizado, artistaDeMetadatos)

      return ResultadoParseo(
         artista = capitalizarNombrePropio(MusicTitleCleaner.cleanArtistName(artista)),
         titulo = capitalizarNombrePropio(MusicTitleCleaner.cleanTitle(titulo)),
         versionInfo = version,
      )
   }

   private fun extraerArtistaYTitulo(
      texto: String,
      artistaDeMetadatos: String?,
   ): Triple<String, String, String?> {
      val patronesSeparadores =
         listOf(
            Regex("""\s+-\s+"""),
            Regex("""\s+–\s+"""),
            Regex("""\s+—\s+"""),
            Regex("""\s+\|\|\s+"""),
            Regex("""\s+\|\s+"""),
         )

      for (patron in patronesSeparadores) {
         if (texto.contains(patron)) {
            val partes = texto.split(patron, limit = 2)
            if (partes.size == 2) {
               val izquierda = partes[0].trim()
               val derecha = partes[1].trim()

               val confianzaIzq = calcularConfianzaArtista(izquierda, artistaDeMetadatos)
               val confianzaDer = calcularConfianzaArtista(derecha, artistaDeMetadatos)

               return if (confianzaIzq > confianzaDer || confianzaIzq > 0.6) {
                  val parsed = MusicTitleCleaner.parseTitle(derecha)
                  Triple(izquierda, parsed.mainTitle, parsed.version?.name)
               } else {
                  val parsed = MusicTitleCleaner.parseTitle(izquierda)
                  Triple(derecha, parsed.mainTitle, parsed.version?.name)
               }
            }
         }
      }

      if (artistaDeMetadatos != null) {
         val parsed = MusicTitleCleaner.parseTitle(texto)
         return Triple(artistaDeMetadatos, parsed.mainTitle, parsed.version?.name)
      }

      val parsed = MusicTitleCleaner.parseTitle(texto)
      return Triple("Artista Desconocido", parsed.mainTitle, parsed.version?.name)
   }

   private fun calcularConfianzaArtista(texto: String, referencia: String?): Double {
      if (referencia == null) return 0.3
      val similitud = StringSimilarity.calculateSimilarity(texto, referencia)
      return when {
         similitud > 0.8 -> 1.0
         similitud > 0.5 -> 0.7
         similitud > 0.3 -> 0.4
         else -> {
            val tokenizada = StringSimilarity.calculateTokenizedSimilarity(texto, referencia)
            if (tokenizada > 0.4) 0.6 else 0.2
         }
      }
   }

   private fun capitalizarNombrePropio(texto: String): String {
      if (texto.isBlank()) return texto
      val palabrasMenores =
         setOf(
            "de",
            "del",
            "la",
            "las",
            "el",
            "los",
            "y",
            "e",
            "o",
            "the",
            "of",
            "and",
            "or",
            "to",
            "in",
            "on",
            "at",
            "for",
         )
      return texto
         .split(" ")
         .mapIndexed { index, palabra ->
            when {
               palabra.matches(Regex("""^[A-Z0-9$#]+$""")) -> palabra
               palabra.contains(".") && !palabra.endsWith(".") -> palabra
               index == 0 -> palabra.lowercase().replaceFirstChar { it.uppercase() }
               palabra.lowercase() in palabrasMenores -> palabra.lowercase()
               else -> palabra.lowercase().replaceFirstChar { it.uppercase() }
            }
         }
         .joinToString(" ")
   }

   private fun obtenerPortadaAlbum(
      albumIdMediaStore: Int,
      audioUri: String?,
      albumTitulo: String,
   ): String? {
      if (albumIdMediaStore > 0) {
         val albumArtUri =
            ContentUris.withAppendedId(
               "content://media/external/audio/albumart".toUri(),
               albumIdMediaStore.toLong(),
            )
         if (validarUriAccesible(albumArtUri)) {
            return albumArtUri.toString()
         }
      }
      return null
   }

   private fun validarUriAccesible(uri: android.net.Uri): Boolean {
      return try {
         context.contentResolver.openFileDescriptor(uri, "r")?.use { true } ?: false
      } catch (e: Exception) {
         false
      }
   }

   fun EstadoEscaneo.Completado.toResultadoEscaneo() = this

   private fun calcularHashRapido(uri: String, tamanio: Long?, fechaMod: Long): String {
      // ✅ Usar hashCode proper y combinar con XOR para mejor distribución
      val h1 = uri.hashCode()
      val h2 = (tamanio ?: 0L).hashCode()
      val h3 = fechaMod.hashCode()
      return ((h1.toLong() shl 32) xor (h2.toLong() shl 16) xor h3.toLong()).toString(16)
   }
}
