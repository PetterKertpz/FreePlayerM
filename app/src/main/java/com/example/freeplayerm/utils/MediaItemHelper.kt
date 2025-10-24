package com.example.freeplayerm.utils

import android.util.Log
import androidx.media3.common.MediaItem
import com.example.freeplayerm.com.example.freeplayerm.data.local.entity.CancionEntity
import com.example.freeplayerm.data.local.dao.CancionDao
import com.example.freeplayerm.data.local.entity.relations.CancionConArtista
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaItemHelper @Inject constructor(
    private val cancionDao: CancionDao
) {
    private val TAG = "MediaItemHelper"

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
                usuarioId = usuarioActualId // ← Aquí va el ID real del usuario
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

            // Crear CancionConArtista - ESTRUCTURA CORRECTA
            CancionConArtista(
                cancion = cancionEntity, // ✅ Propiedad correcta
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
                val mediaId = mediaItem.mediaId?.toIntOrNull() ?: 0

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
                    cancion = cancionMinima, // ✅ Propiedad correcta
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
            ID: ${mediaItem.mediaId ?: "N/A"}
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