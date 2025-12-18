package com.example.freeplayerm.utils

import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.util.UnstableApi
import com.example.freeplayerm.com.example.freeplayerm.data.local.entity.CancionEntity
import com.example.freeplayerm.data.local.dao.CancionDao
import com.example.freeplayerm.data.local.entity.CancionEntity
import com.example.freeplayerm.data.local.entity.relations.CancionConArtista
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@OptIn(UnstableApi::class)
class MediaItemHelper @Inject constructor(
    private val cancionDao: CancionDao
) {
    private val TAG = "MediaItemHelper"

    /**
     * ✅ NUEVO: Crea un MediaItem con todos los metadatos necesarios para la notificación.
     * Úsalo en tu ViewModel antes de enviar la canción al player.
     */
    fun crearMediaItemDesdeEntidad(
        cancion: CancionConArtista,
        artworkBitmap: Bitmap? = null
    ): MediaItem {

        // 1. Preparar metadatos (Título, Artista, Album, etc.)
        val metadataBuilder = MediaMetadata.Builder()
            .setTitle(cancion.cancion.titulo)
            .setArtist(cancion.artistaNombre ?: "Artista Desconocido")
            .setAlbumTitle(cancion.albumNombre ?: "")
            .setGenre(cancion.generoNombre ?: "")
            .setIsPlayable(true)

        // 2. Agregar Portada (Artwork)
        // Prioridad 1: Bitmap en memoria (si se pasa como argumento)
        if (artworkBitmap != null) {
            try {
                val stream = ByteArrayOutputStream()
                artworkBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                val byteArray = stream.toByteArray()
                metadataBuilder.setArtworkData(byteArray, MediaMetadata.PICTURE_TYPE_FRONT_COVER)
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error comprimiendo artwork: ${e.message}")
            }
        }
        // Prioridad 2: Ruta de archivo (si existe en la entidad)
        else if (!cancion.portadaPath.isNullOrBlank()) {
            metadataBuilder.setArtworkUri(Uri.parse(cancion.portadaPath))
        }

        // 3. Agregar duración (opcional, ayuda a la UI del sistema)
        cancion.cancion.duracionSegundos?.let { segundos ->
            if (segundos > 0) {
                // Convertir a ms si es necesario, ExoPlayer lo maneja mejor automáticamente
                // pero establecerlo aquí ayuda a la metadata estática
                // metadataBuilder.setDurationMs(segundos * 1000L)
            }
        }

        // 4. Construir el MediaItem final
        return MediaItem.Builder()
            .setUri(cancion.cancion.archivoPath)
            .setMediaId(cancion.cancion.idCancion.toString())
            .setMediaMetadata(metadataBuilder.build())
            .build()
    }

    /**
     * Convierte un MediaItem en CancionConArtista para sincronización
     */
    suspend fun mediaItemToCancionConArtista(mediaItem: MediaItem): CancionConArtista? {
        return try {
            Log.d(TAG, "🎵 Convirtiendo MediaItem a CancionConArtista: ${mediaItem.mediaId}")

            if (!esMediaItemValido(mediaItem)) {
                Log.w(TAG, "❌ MediaItem no válido para conversión")
                return null
            }

            val mediaId = mediaItem.mediaId
            val idCancion = mediaId.toIntOrNull() ?: return null
            val usuarioActualId = 1 // ID temporal para testing

            // Buscar en base de datos primero
            val desdeBD = cancionDao.obtenerCancionConArtistaPorId(
                idCancion = idCancion.toInt(),
                usuarioId = usuarioActualId
            )
            if (desdeBD != null) {
                Log.d(TAG, "  Encontrado en BD: ${desdeBD.cancion.titulo}")
                return desdeBD
            }

            // Crear desde metadata si no está en BD
            crearDesdeMetadata(mediaItem, idCancion)

        } catch (e: Exception) {
            Log.e(TAG, "💥 Error convirtiendo MediaItem: ${e.message}")
            null
        }
    }

    /**
     * Crea CancionConArtista desde metadatos
     */
    private fun crearDesdeMetadata(mediaItem: MediaItem, idCancion: Int): CancionConArtista? {
        return try {
            val metadata = mediaItem.mediaMetadata

            val titulo = metadata.title?.toString()?.trim()
            if (titulo.isNullOrBlank()) {
                Log.w(TAG, "❌ Metadata no tiene título válido")
                return null
            }

            val artista = metadata.artist?.toString()?.trim() ?: "Artista Desconocido"
            val album = metadata.albumTitle?.toString()?.trim() ?: ""
            val genero = metadata.genre?.toString()?.trim() ?: ""

            // Aquí estaba el error de Lint, ahora corregido por la anotación de la clase
            val duracionSegundos = metadata.durationMs?.let { (it / 1000).toInt() } ?: 0

            // Crear CancionEntity
            val cancionEntity = CancionEntity(
                idCancion = idCancion,
                titulo = titulo,
                idArtista = null,
                idAlbum = null,
                idGenero = null,
                duracionSegundos = duracionSegundos,
                origen = "LOCAL",
                archivoPath = "",
                geniusId = null,
                geniusUrl = null
            )

            // Crear CancionConArtista
            CancionConArtista(
                cancion = cancionEntity,
                artistaNombre = artista,
                albumNombre = album,
                generoNombre = genero,
                esFavorita = false,
                portadaPath = metadata.artworkUri?.toString() ?: "",
                fechaLanzamiento = metadata.releaseYear?.toString()
            )

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error creando desde metadata: ${e.message}")
            null
        }
    }

    /**
     * Método resiliente - intenta múltiples estrategias
     */
    suspend fun obtenerDatosCancionConResiliencia(mediaItem: MediaItem): CancionConArtista? {
        Log.d(TAG, "🛡️ Iniciando obtención resiliente de datos...")

        // Estrategia 1: Conversión normal
        try {
            val resultadoNormal = mediaItemToCancionConArtista(mediaItem)
            if (resultadoNormal != null) {
                Log.d(TAG, "✅ Estrategia 1 exitosa")
                return resultadoNormal
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Estrategia 1 falló: ${e.message}")
        }

        // Estrategia 2: Datos mínimos
        try {
            val datosBasicos = extraerDatosBusquedaBasicos(mediaItem)
            if (datosBasicos != null) {
                val (titulo, artista) = datosBasicos
                val mediaId = mediaItem.mediaId.toIntOrNull() ?: 0

                Log.d(TAG, "🆘 Usando datos mínimos: '$titulo' - '$artista'")

                val cancionMinima = CancionEntity(
                    idCancion = mediaId,
                    titulo = titulo,
                    idArtista = null,
                    idAlbum = null,
                    idGenero = null,
                    duracionSegundos = 0,
                    origen = "LOCAL",
                    archivoPath = "",
                    geniusId = null,
                    geniusUrl = null
                )

                val resultadoMinimo = CancionConArtista(
                    cancion = cancionMinima,
                    artistaNombre = artista,
                    albumNombre = "",
                    generoNombre = "",
                    esFavorita = false,
                    portadaPath = "",
                    fechaLanzamiento = null
                )

                Log.d(TAG, "✅ Estrategia 2 exitosa (mínima)")
                return resultadoMinimo
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Estrategia 2 falló: ${e.message}")
        }

        Log.w(TAG, "❌ Todas las estrategias fallaron")
        return null
    }

    fun esMediaItemValido(mediaItem: MediaItem): Boolean {
        return try {
            val metadata = mediaItem.mediaMetadata
            val tieneMediaId = !mediaItem.mediaId.isNullOrBlank()
            val tieneTitulo = !metadata.title?.toString().isNullOrBlank()
            val valido = tieneMediaId && tieneTitulo

            if (!valido) {
                Log.w(TAG, "⚠️ MediaItem inválido - MediaId: $tieneMediaId, Título: $tieneTitulo")
            }

            valido
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error validando MediaItem: ${e.message}")
            false
        }
    }

    fun extraerDatosBusquedaBasicos(mediaItem: MediaItem): Pair<String, String>? {
        return try {
            val metadata = mediaItem.mediaMetadata
            val titulo = metadata.title?.toString()?.trim()
            if (titulo.isNullOrBlank()) {
                Log.w(TAG, "❌ No se puede extraer datos: título vacío")
                return null
            }

            val artista = metadata.artist?.toString()?.trim() ?: "Artista Desconocido"

            Log.d(TAG, "🔍 Datos básicos extraídos: '$titulo' - '$artista'")
            Pair(titulo, artista)

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error extrayendo datos básicos: ${e.message}")
            null
        }
    }

    fun debugMediaItem(mediaItem: MediaItem): String {
        return try {
            val metadata = mediaItem.mediaMetadata
            """
            🎵 MediaItem Debug:
            ID: ${mediaItem.mediaId}
            Título: ${metadata.title ?: "N/A"}
            Artista: ${metadata.artist ?: "N/A"} 
            Álbum: ${metadata.albumTitle ?: "N/A"}
            Género: ${metadata.genre ?: "N/A"}
            Duración: ${metadata.durationMs ?: "N/A"} ms
            """.trimIndent()
        } catch (e: Exception) {
            "❌ Error en debug: ${e.message}"
        }
    }
}