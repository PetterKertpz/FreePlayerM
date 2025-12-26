// en: app/src/main/java/com/example/freeplayerm/data/local/entity/CancionEntity.kt
package com.example.freeplayerm.data.local.entity

import android.annotation.SuppressLint
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 🎵 CANCION ENTITY - OPTIMIZADA v2.0
 *
 * Entidad que representa una canción en la base de datos
 * Incluye metadatos, referencias a artista/álbum/género, y estadísticas de reproducción
 *
 * Características:
 * - Foreign keys con CASCADE/SET_NULL apropiados
 * - Índices para búsquedas rápidas
 * - Campos de estadísticas (reproducciones, última reproducción)
 * - Soporte para múltiples orígenes (local/remoto)
 * - Longegración con Genius API
 *
 * @version 2.0 - Enhanced
 */
@Entity(
    tableName = "canciones",
    foreignKeys = [
        ForeignKey(
            entity = ArtistaEntity::class,
            parentColumns = ["id_artista"],
            childColumns = ["id_artista"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = AlbumEntity::class,
            parentColumns = ["id_album"],
            childColumns = ["id_album"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = GeneroEntity::class,
            parentColumns = ["id_genero"],
            childColumns = ["id_genero"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["id_artista"]),
        Index(value = ["id_album"]),
        Index(value = ["id_genero"]),
        Index(value = ["titulo"]),
        Index(value = ["veces_reproducida"]),
        Index(value = ["fecha_agregado"])
    ]
)
data class CancionEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_cancion")
    val idCancion: Int = 0,

    // ==================== RELACIONES ====================
    @ColumnInfo(name = "id_artista")
    val idArtista: Int?,

    @ColumnInfo(name = "id_album")
    val idAlbum: Int?,

    @ColumnInfo(name = "id_genero")
    val idGenero: Int?,

    // ==================== INFORMACIÓN BÁSICA ====================

    @ColumnInfo(name = "titulo")
    val titulo: String,

    @ColumnInfo(name = "duracion_segundos")
    val duracionSegundos: Int,

    @ColumnInfo(name = "numero_pista")
    val numeroPista: Int? = null, // Número de pista en el álbum

    @ColumnInfo(name = "anio")
    val anio: Int? = null, // Año de lanzamiento

    // ==================== ORIGEN Y ARCHIVO ====================

    @ColumnInfo(name = "origen")
    val origen: String, // "LOCAL", "REMOTA", "STREAMING"

    @ColumnInfo(name = "archivo_path")
    val archivoPath: String?, // Ruta local si el origen es "LOCAL"

    @ColumnInfo(name = "url_streaming")
    val urlStreaming: String? = null, // URL para streaming si es remota

    // ==================== GENIUS API ====================

    @ColumnInfo(name = "genius_id")
    val geniusId: String? = null,

    @ColumnInfo(name = "genius_url")
    val geniusUrl: String? = null,

    /**
     * Título completo con artistas featured
     * Mapea desde: SongDetailsEnhanced.titleWithFeatured
     */
    @ColumnInfo(name = "titulo_completo")
    val tituloCompleto: String? = null,

    /**
     * Estado de la letra en Genius
     * Valores: "complete", "incomplete", null
     */
    @ColumnInfo(name = "lyrics_state")
    val lyricsState: String? = null,

    /**
     * Si la canción está "hot" en Genius
     */
    @ColumnInfo(name = "hot")
    val hot: Boolean = false,

    /**
     * Pageviews en Genius (popularidad)
     */
    @ColumnInfo(name = "pageviews")
    val pageviews: Int? = null,

    /**
     * Idioma de la letra
     * Mapea desde: SongDetailsEnhanced.language
     */
    @ColumnInfo(name = "idioma")
    val idioma: String? = null,

    /**
     * Ubicación de grabación
     * Mapea desde: SongDetailsEnhanced.recordingLocation
     */
    @ColumnInfo(name = "ubicacion_grabacion")
    val ubicacionGrabacion: String? = null,

    /**
     * IDs externos (Spotify, Apple Music, YouTube)
     * Almacena JSON: {"spotify": "id", "youtube": "url", ...}
     */
    @ColumnInfo(name = "external_ids_json")
    val externalIdsJson: String? = null, // URL de la página de la canción en Genius

    // ==================== ESTADÍSTICAS DE REPRODUCCIÓN ====================

    @ColumnInfo(name = "veces_reproducida")
    val vecesReproducida: Int = 0, // Contador de reproducciones

    @ColumnInfo(name = "ultima_reproduccion")
    val ultimaReproduccion: Long? = null, // Timestamp de última reproducción

    @ColumnInfo(name = "fecha_agregado")
    val fechaAgregado: Long = System.currentTimeMillis(), // Timestamp de cuándo se agregó

    @ColumnInfo(name = "fecha_modificacion")
    val fechaModificacion: Long? = null,
    // ==================== METADATOS ADICIONALES ====================

    @ColumnInfo(name = "calidad_audio")
    val calidadAudio: String? = null, // "LOW", "MEDIUM", "HIGH", "LOSSLESS"

    @ColumnInfo(name = "bitrate")
    val bitrate: Int? = null, // Bitrate en kbps

    @ColumnInfo(name = "letra_disponible")
    val letraDisponible: Boolean = false, // Indica si tiene letra asociada

    @ColumnInfo(name = "portada_path")
    val portadaPath: String? = null, // Path de portada individual (si difiere del álbum)

) {
    /**
     * Duración formateada en MM:SS
     */
    @SuppressLint("DefaultLocale")
    fun duracionFormateada(): String {
        val minutos = duracionSegundos / 60
        val segundos = duracionSegundos % 60
        return String.format("%02d:%02d", minutos, segundos)
    }

    /**
     * Verifica si la canción es local
     */
    fun esLocal(): Boolean = origen == "LOCAL"

    /**
     * Verifica si la canción es remota/streaming
     */
    fun esRemota(): Boolean = origen == "REMOTA" || origen == "STREAMING"

    /**
     * Verifica si tiene archivo disponible
     */
    fun tieneArchivo(): Boolean = !archivoPath.isNullOrBlank()

    /**
     * Obtiene la ruta de la portada (individual o del álbum)
     */
    fun obtenerPortada(): String? = portadaPath

    companion object {
        // Constantes para origen
        const val ORIGEN_LOCAL = "LOCAL"
        const val ORIGEN_REMOTA = "REMOTA"
        const val ORIGEN_STREAMING = "STREAMING"

        // Constantes para calidad
        const val CALIDAD_LOW = "LOW"
        const val CALIDAD_MEDIUM = "MEDIUM"
        const val CALIDAD_HIGH = "HIGH"
        const val CALIDAD_LOSSLESS = "LOSSLESS"
    }
}