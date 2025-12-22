package com.example.freeplayerm.utils

import android.graphics.Bitmap
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.util.UnstableApi
import androidx.core.net.toUri
import com.example.freeplayerm.data.local.entity.CancionEntity
import com.example.freeplayerm.data.local.dao.CancionDao
import com.example.freeplayerm.data.local.entity.relations.CancionConArtista
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ⚡ MEDIA ITEM HELPER - OPTIMIZADO v3.0
 *
 * Utilidad para convertir entre MediaItem (Media3) y CancionConArtista (BD)
 *
 * Características:
 * - Conversión bidireccional completa
 * - Manejo robusto de metadatos
 * - Soporte para artwork (bitmap y URI)
 * - Validación y logging detallado
 * - Estrategias de fallback
 *
 * @author Android Media Manager
 * @version 3.0 - Production Ready
 */
@Singleton
@OptIn(UnstableApi::class)
class MediaItemHelper @Inject constructor(
    private val cancionDao: CancionDao
) {

    companion object {
        private const val TAG = "MediaItemHelper"
        private const val USUARIO_DEFAULT = 1
    }

    // ==================== CONVERSIÓN: CancionConArtista → MediaItem ====================

    /**
     * Crea un MediaItem desde CancionConArtista con todos los metadatos
     *
     * Este es el método principal que deberías usar en el ViewModel
     *
     * @param cancion La canción con todos sus datos
     * @param artworkBitmap Opcional: bitmap de portada en memoria
     * @return MediaItem listo para ExoPlayer
     */
    fun crearMediaItem(
        cancion: CancionConArtista,
        artworkBitmap: Bitmap? = null
    ): MediaItem {
        Log.d(TAG, "🎵 Creando MediaItem: ${cancion.cancion.titulo}")

        val metadata = construirMetadata(cancion, artworkBitmap)

        return MediaItem.Builder()
            .setUri(cancion.cancion.archivoPath.orEmpty())
            .setMediaId(cancion.cancion.idCancion.toString())
            .setMediaMetadata(metadata)
            .build()
            .also {
                Log.d(TAG, "✅ MediaItem creado: ID=${it.mediaId}")
            }
    }

    /**
     * Crea MediaItem desde CancionEntity simple (sin artista)
     */
    fun crearMediaItemDesdeEntity(
        cancion: CancionEntity,
        artworkBitmap: Bitmap? = null
    ): MediaItem {
        Log.d(TAG, "🎵 Creando MediaItem desde Entity: ${cancion.titulo}")

        val metadata = MediaMetadata.Builder()
            .setTitle(cancion.titulo)
            .setArtist("Artista Desconocido")
            .setIsPlayable(true)
            .apply {
                if (cancion.duracionSegundos > 0) {
                    // Media3 prefiere milisegundos
                }

                if (artworkBitmap != null) {
                    agregarArtworkBitmap(this, artworkBitmap)
                }
            }
            .build()

        return MediaItem.Builder()
            .setUri(cancion.archivoPath.orEmpty())
            .setMediaId(cancion.idCancion.toString())
            .setMediaMetadata(metadata)
            .build()
    }

    /**
     * Crea múltiples MediaItems en lote (más eficiente)
     */
    fun crearMediaItems(
        canciones: List<CancionConArtista>,
        artworkMap: Map<Int, Bitmap> = emptyMap()
    ): List<MediaItem> {
        Log.d(TAG, "📦 Creando ${canciones.size} MediaItems en lote")

        return canciones.map { cancion ->
            val artwork = artworkMap[cancion.cancion.idCancion]
            crearMediaItem(cancion, artwork)
        }
    }

    /**
     * Construye metadata completo desde CancionConArtista
     */
    private fun construirMetadata(
        cancion: CancionConArtista,
        artworkBitmap: Bitmap?
    ): MediaMetadata {
        return MediaMetadata.Builder()
            .setTitle(cancion.cancion.titulo)
            .setArtist(cancion.artistaNombre ?: "Artista Desconocido")
            .setAlbumTitle(cancion.albumNombre ?: "")
            .setGenre(cancion.generoNombre ?: "")
            .setIsPlayable(true)
            .apply {
                // Año de lanzamiento
                cancion.fechaLanzamiento?.let { anio ->
                    val anioInt = anio.toIntOrNull()
                    if (anioInt != null) {
                        setReleaseYear(anioInt)
                    }
                }

                // Duración
                if (cancion.cancion.duracionSegundos > 0) {
                    // Media3 maneja la duración automáticamente del archivo
                    // pero podemos establecerla aquí para metadata estática
                }

                // Artwork - Prioridad: Bitmap > URI
                when {
                    artworkBitmap != null -> {
                        agregarArtworkBitmap(this, artworkBitmap)
                    }
                    !cancion.portadaPath.isNullOrBlank() -> {
                        setArtworkUri(cancion.portadaPath.toUri())
                    }
                }
            }
            .build()
    }

    /**
     * Agrega artwork desde Bitmap a MediaMetadata
     */
    private fun agregarArtworkBitmap(
        builder: MediaMetadata.Builder,
        bitmap: Bitmap
    ) {
        try {
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream)
            val byteArray = stream.toByteArray()
            builder.setArtworkData(byteArray, MediaMetadata.PICTURE_TYPE_FRONT_COVER)
            Log.d(TAG, "✅ Artwork agregado (${byteArray.size} bytes)")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error agregando artwork", e)
        }
    }

    // ==================== CONVERSIÓN: MediaItem → CancionConArtista ====================

    /**
     * Convierte MediaItem a CancionConArtista
     *
     * Intenta obtener datos de BD primero, si no existe crea desde metadata
     */
    suspend fun mediaItemACancionConArtista(
        mediaItem: MediaItem,
        usuarioId: Int = USUARIO_DEFAULT
    ): CancionConArtista? {
        Log.d(TAG, "🔄 Convirtiendo MediaItem: ${mediaItem.mediaId}")

        return try {
            if (!esMediaItemValido(mediaItem)) {
                Log.w(TAG, "❌ MediaItem no válido")
                return null
            }

            val idCancion = mediaItem.mediaId.toLongOrNull()
            if (idCancion == null) {
                Log.w(TAG, "❌ MediaId no es numérico: ${mediaItem.mediaId}")
                return crearDesdeMetadata(mediaItem, 0)
            }

            // Intentar obtener de BD
            val desdeBD = cancionDao.obtenerCancionConArtistaPorId(idCancion.toInt(), usuarioId)
            if (desdeBD != null) {
                Log.d(TAG, "✅ Encontrado en BD: ${desdeBD.cancion.titulo}")
                return desdeBD
            }

            // Crear desde metadata si no está en BD
            Log.d(TAG, "⚠️ No encontrado en BD, creando desde metadata")
            crearDesdeMetadata(mediaItem, idCancion.toInt())

        } catch (e: Exception) {
            Log.e(TAG, "💥 Error en conversión", e)
            null
        }
    }

    /**
     * Crea CancionConArtista desde metadata de MediaItem
     */
    private fun crearDesdeMetadata(
        mediaItem: MediaItem,
        idCancion: Int
    ): CancionConArtista? {
        return try {
            val metadata = mediaItem.mediaMetadata

            val titulo = metadata.title?.toString()?.trim()
            if (titulo.isNullOrBlank()) {
                Log.w(TAG, "❌ Metadata sin título válido")
                return null
            }

            val artista = metadata.artist?.toString()?.trim() ?: "Artista Desconocido"
            val album = metadata.albumTitle?.toString()?.trim() ?: ""
            val genero = metadata.genre?.toString()?.trim() ?: ""
            val duracionMs = metadata.durationMs ?: 0L
            val duracionSegundos = (duracionMs / 1000).toInt()

            val cancionEntity = CancionEntity(
                idCancion = idCancion,
                titulo = titulo,
                idArtista = null,
                idAlbum = null,
                idGenero = null,
                duracionSegundos = duracionSegundos,
                origen = "EXTERNAL",
                archivoPath = mediaItem.localConfiguration?.uri?.toString(),
                geniusId = null,
                geniusUrl = null
            )

            val resultado = CancionConArtista(
                cancion = cancionEntity,
                artistaNombre = artista,
                albumNombre = album,
                generoNombre = genero,
                esFavorita = false,
                portadaPath = metadata.artworkUri?.toString(),
                fechaLanzamiento = metadata.releaseYear.toString()
            )

            Log.d(TAG, "✅ CancionConArtista creado desde metadata")
            resultado

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error creando desde metadata", e)
            null
        }
    }

    /**
     * Conversión con estrategias de fallback
     */
    suspend fun obtenerConResiliencia(
        mediaItem: MediaItem,
        usuarioId: Int = USUARIO_DEFAULT
    ): CancionConArtista? {
        Log.d(TAG, "🛡️ Iniciando conversión resiliente...")

        // Estrategia 1: Conversión normal
        try {
            val resultado = mediaItemACancionConArtista(mediaItem, usuarioId)
            if (resultado != null) {
                Log.d(TAG, "✅ Estrategia 1 exitosa")
                return resultado
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Estrategia 1 falló", e)
        }

        // Estrategia 2: Datos mínimos
        try {
            val datosMinimos = extraerDatosBasicos(mediaItem)
            if (datosMinimos != null) {
                Log.d(TAG, "🆘 Usando datos mínimos")
                return crearCancionMinima(mediaItem, datosMinimos)
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Estrategia 2 falló", e)
        }

        Log.w(TAG, "❌ Todas las estrategias fallaron")
        return null
    }

    /**
     * Crea CancionConArtista con datos mínimos
     */
    private fun crearCancionMinima(
        mediaItem: MediaItem,
        datosBasicos: Pair<String, String>
    ): CancionConArtista {
        val (titulo, artista) = datosBasicos
        val idCancion = mediaItem.mediaId.toLongOrNull() ?: 0L

        val cancionEntity = CancionEntity(
            idCancion = idCancion.toInt(),
            titulo = titulo,
            idArtista = null,
            idAlbum = null,
            idGenero = null,
            duracionSegundos = 0,
            origen = "UNKNOWN",
            archivoPath = mediaItem.localConfiguration?.uri?.toString(),
            geniusId = null,
            geniusUrl = null
        )

        return CancionConArtista(
            cancion = cancionEntity,
            artistaNombre = artista,
            albumNombre = "",
            generoNombre = "",
            esFavorita = false,
            portadaPath = null,
            fechaLanzamiento = null
        )
    }

    // ==================== VALIDACIÓN Y EXTRACCIÓN ====================

    /**
     * Valida si un MediaItem tiene los datos mínimos necesarios
     */
    fun esMediaItemValido(mediaItem: MediaItem): Boolean {
        return try {
            val metadata = mediaItem.mediaMetadata
            val tieneMediaId = mediaItem.mediaId.isNotBlank()
            val tieneTitulo = !metadata.title?.toString().isNullOrBlank()
            val tieneUri = mediaItem.localConfiguration?.uri != null

            val valido = tieneMediaId && tieneTitulo

            if (!valido) {
                Log.w(TAG, "⚠️ MediaItem inválido - " +
                        "MediaId: $tieneMediaId, " +
                        "Título: $tieneTitulo, " +
                        "URI: $tieneUri"
                )
            }

            valido
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error validando MediaItem", e)
            false
        }
    }

    /**
     * Extrae título y artista mínimos
     */
    fun extraerDatosBasicos(mediaItem: MediaItem): Pair<String, String>? {
        return try {
            val metadata = mediaItem.mediaMetadata
            val titulo = metadata.title?.toString()?.trim()

            if (titulo.isNullOrBlank()) {
                Log.w(TAG, "❌ No se puede extraer: título vacío")
                return null
            }

            val artista = metadata.artist?.toString()?.trim() ?: "Artista Desconocido"

            Log.d(TAG, "🔍 Datos básicos: '$titulo' - '$artista'")
            Pair(titulo, artista)

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error extrayendo datos", e)
            null
        }
    }

    /**
     * Extrae ID de canción desde MediaItem
     */
    fun extraerIdCancion(mediaItem: MediaItem): Int? {
        return try {
            mediaItem.mediaId.toLongOrNull()
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error extrayendo ID", e)
            null
        }
    }

    /**
     * Verifica si un MediaItem está en la base de datos
     */
    suspend fun existeEnBaseDatos(
        mediaItem: MediaItem,
        usuarioId: Int = USUARIO_DEFAULT
    ): Boolean {
        return try {
            val idCancion = extraerIdCancion(mediaItem) ?: return false
            cancionDao.obtenerCancionConArtistaPorId(idCancion.toInt(), usuarioId) != null
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error verificando existencia", e)
            false
        }
    }

    // ==================== UTILIDADES ====================

    /**
     * Compara dos MediaItems para ver si son la misma canción
     */
    fun sonIguales(item1: MediaItem, item2: MediaItem): Boolean {
        return item1.mediaId == item2.mediaId
    }

    /**
     * Obtiene duración en segundos desde MediaItem
     */
    fun obtenerDuracionSegundos(mediaItem: MediaItem): Int {
        return try {
            val duracionMs = mediaItem.mediaMetadata.durationMs ?: 0L
            (duracionMs / 1000).toInt()
        } catch (e: Exception) {
            0
        }
    }

    /**
     * Debug: Imprime información completa del MediaItem
     */
    fun debugMediaItem(mediaItem: MediaItem): String {
        return try {
            val metadata = mediaItem.mediaMetadata
            val uri = mediaItem.localConfiguration?.uri

            buildString {
                appendLine("🎵 MediaItem Debug:")
                appendLine("  ID: ${mediaItem.mediaId}")
                appendLine("  Título: ${metadata.title ?: "N/A"}")
                appendLine("  Artista: ${metadata.artist ?: "N/A"}")
                appendLine("  Álbum: ${metadata.albumTitle ?: "N/A"}")
                appendLine("  Género: ${metadata.genre ?: "N/A"}")
                appendLine("  Duración: ${metadata.durationMs ?: "N/A"} ms")
                appendLine("  Año: ${metadata.releaseYear ?: "N/A"}")
                appendLine("  URI: ${uri ?: "N/A"}")
                appendLine("  Artwork URI: ${metadata.artworkUri ?: "N/A"}")
                appendLine("  Es playable: ${metadata.isPlayable ?: false}")
            }
        } catch (e: Exception) {
            "❌ Error en debug: ${e.message}"
        }
    }

    /**
     * Crea MediaItem de prueba para testing
     */
    fun crearMediaItemDePrueba(
        id: Int = 1,
        titulo: String = "Test Song",
        artista: String = "Test Artist"
    ): MediaItem {
        val metadata = MediaMetadata.Builder()
            .setTitle(titulo)
            .setArtist(artista)
            .setIsPlayable(true)
            .build()

        return MediaItem.Builder()
            .setMediaId(id.toString())
            .setMediaMetadata(metadata)
            .setUri("content://test/$id")
            .build()
    }

    /**
     * Actualiza metadata de un MediaItem existente
     */
    fun actualizarMetadata(
        mediaItem: MediaItem,
        titulo: String? = null,
        artista: String? = null,
        album: String? = null
    ): MediaItem {
        val metadataActual = mediaItem.mediaMetadata
        val metadataNuevo = metadataActual.buildUpon()
            .apply {
                titulo?.let { setTitle(it) }
                artista?.let { setArtist(it) }
                album?.let { setAlbumTitle(it) }
            }
            .build()

        return mediaItem.buildUpon()
            .setMediaMetadata(metadataNuevo)
            .build()
    }

    // ==================== MÉTODOS LEGACY (DEPRECATED) ====================

    /**
     * @deprecated Usar crearMediaItem() en su lugar
     */
    @Deprecated(
        message = "Usar crearMediaItem() en su lugar",
        replaceWith = ReplaceWith("crearMediaItem(cancion, artworkBitmap)")
    )
    fun crearMediaItemDesdeEntidad(
        cancion: CancionConArtista,
        artworkBitmap: Bitmap? = null
    ): MediaItem {
        return crearMediaItem(cancion, artworkBitmap)
    }

    /**
     * @deprecated Usar mediaItemACancionConArtista() en su lugar
     */
    @Deprecated(
        message = "Usar mediaItemACancionConArtista() en su lugar",
        replaceWith = ReplaceWith("mediaItemACancionConArtista(mediaItem)")
    )
    suspend fun mediaItemToCancionConArtista(mediaItem: MediaItem): CancionConArtista? {
        return mediaItemACancionConArtista(mediaItem)
    }

    /**
     * @deprecated Usar obtenerConResiliencia() en su lugar
     */
    @Deprecated(
        message = "Usar obtenerConResiliencia() en su lugar",
        replaceWith = ReplaceWith("obtenerConResiliencia(mediaItem)")
    )
    suspend fun obtenerDatosCancionConResiliencia(mediaItem: MediaItem): CancionConArtista? {
        return obtenerConResiliencia(mediaItem)
    }

    /**
     * @deprecated Usar extraerDatosBasicos() en su lugar
     */
    @Deprecated(
        message = "Usar extraerDatosBasicos() en su lugar",
        replaceWith = ReplaceWith("extraerDatosBasicos(mediaItem)")
    )
    fun extraerDatosBusquedaBasicos(mediaItem: MediaItem): Pair<String, String>? {
        return extraerDatosBasicos(mediaItem)
    }
}