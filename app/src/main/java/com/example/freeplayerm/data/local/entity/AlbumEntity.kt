// en: app/src/main/java/com/example/freeplayerm/data/local/entity/AlbumEntity.kt
package com.example.freeplayerm.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 💿 ALBUM ENTITY - OPTIMIZADA v2.0
 *
 * Entidad que representa un álbum musical
 * Incluye artwork, metadata y estadísticas
 *
 * Características:
 * - Foreign key a artista con CASCADE delete
 * - Índice único en (titulo + id_artista) para evitar duplicados
 * - Soporte para diferentes tipos (álbum, EP, single, compilación)
 * - Estadísticas de reproducción
 * - Metadata completa
 *
 * @version 2.0 - Enhanced
 */
@Entity(
    tableName = "albumes",
    foreignKeys = [
        ForeignKey(
            entity = ArtistaEntity::class,
            parentColumns = ["id_artista"],
            childColumns = ["id_artista"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["titulo", "id_artista"], unique = true), // Evita álbumes duplicados del mismo artista
        Index(value = ["id_artista"]),
        Index(value = ["anio"]),
        Index(value = ["tipo"]),
        Index(value = ["fecha_agregado"])
    ]
)
data class AlbumEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_album")
    val idAlbum: Int = 0,

    @ColumnInfo(name = "id_artista")
    val idArtista: Int,

    // ==================== INFORMACIÓN BÁSICA ====================

    @ColumnInfo(name = "titulo")
    val titulo: String,

    @ColumnInfo(name = "titulo_normalizado")
    val tituloNormalizado: String = titulo.lowercase().trim(),

    @ColumnInfo(name = "subtitulo")
    val subtitulo: String? = null, // Para ediciones especiales, etc.

    // ==================== ARTWORK ====================

    @ColumnInfo(name = "portada_path")
    val portadaPath: String? = null, // Path local de la portada

    @ColumnInfo(name = "portada_url")
    val portadaUrl: String? = null, // URL de la portada

    @ColumnInfo(name = "portada_thumbnail")
    val portadaThumbnail: String? = null, // Miniatura

    // ==================== FECHA Y LANZAMIENTO ====================

    @ColumnInfo(name = "anio")
    val anio: Int?,

    @ColumnInfo(name = "fecha_lanzamiento")
    val fechaLanzamiento: Long? = null, // Timestamp de fecha exacta de lanzamiento

    @ColumnInfo(name = "fecha_agregado")
    val fechaAgregado: Long = System.currentTimeMillis(),

    // ==================== CLASIFICACIÓN ====================

    @ColumnInfo(name = "tipo")
    val tipo: String = TIPO_ALBUM, // ALBUM, EP, SINGLE, COMPILATION, LIVE

    @ColumnInfo(name = "genero_principal")
    val generoPrincipal: String? = null,

    @ColumnInfo(name = "generos")
    val generos: String? = null, // Lista separada por comas

    // ==================== INFORMACIÓN TÉCNICA ====================

    @ColumnInfo(name = "discografica")
    val discografica: String? = null, // Sello discográfico

    @ColumnInfo(name = "productor")
    val productor: String? = null,

    @ColumnInfo(name = "pais_produccion")
    val paisProduccion: String? = null,

    // ==================== ESTADÍSTICAS ====================

    @ColumnInfo(name = "total_canciones")
    val totalCanciones: Int = 0,

    @ColumnInfo(name = "duracion_total_segundos")
    val duracionTotalSegundos: Long = 0,

    @ColumnInfo(name = "total_reproducciones")
    val totalReproducciones: Int = 0,

    @ColumnInfo(name = "veces_favorito")
    val vecesFavorito: Int = 0,

    // ==================== METADATA ====================

    @ColumnInfo(name = "descripcion")
    val descripcion: String? = null,

    @ColumnInfo(name = "es_compilacion")
    val esCompilacion: Boolean = false,

    @ColumnInfo(name = "es_en_vivo")
    val esEnVivo: Boolean = false,

    @ColumnInfo(name = "es_edicion_especial")
    val esEdicionEspecial: Boolean = false,

    @ColumnInfo(name = "es_deluxe")
    val esDeluxe: Boolean = false,

    @ColumnInfo(name = "numero_discos")
    val numeroDiscos: Int = 1,

    // ==================== ENLACES ====================

    @ColumnInfo(name = "genius_id")
    val geniusId: String? = null,

    @ColumnInfo(name = "genius_url")
    val geniusUrl: String? = null,

    @ColumnInfo(name = "spotify_id")
    val spotifyId: String? = null,

    @ColumnInfo(name = "apple_music_id")
    val appleMusicId: String? = null,

    // ==================== CALIFICACIÓN ====================

    @ColumnInfo(name = "calificacion_promedio")
    val calificacionPromedio: Float = 0f, // 0-5 estrellas

    @ColumnInfo(name = "total_calificaciones")
    val totalCalificaciones: Int = 0
) {
    /**
     * Obtiene la portada a usar (prioriza local, luego URL)
     */
    fun obtenerPortada(): String? = portadaPath ?: portadaUrl

    /**
     * Verifica si tiene portada
     */
    fun tienePortada(): Boolean = !portadaPath.isNullOrBlank() || !portadaUrl.isNullOrBlank()

    /**
     * Obtiene la miniatura o portada regular
     */
    fun obtenerThumbnail(): String? = portadaThumbnail ?: obtenerPortada()

    /**
     * Duración formateada
     */
    fun duracionFormateada(): String {
        val horas = duracionTotalSegundos / 3600
        val minutos = (duracionTotalSegundos % 3600) / 60
        val segundos = duracionTotalSegundos % 60

        return if (horas > 0) {
            String.format("%dh %dm", horas, minutos)
        } else {
            String.format("%dm %ds", minutos, segundos)
        }
    }

    /**
     * Obtiene el título completo con subtítulo si existe
     */
    fun tituloCompleto(): String {
        return if (subtitulo != null) {
            "$titulo - $subtitulo"
        } else {
            titulo
        }
    }

    /**
     * Verifica si es un EP
     */
    fun esEP(): Boolean = tipo == TIPO_EP

    /**
     * Verifica si es un single
     */
    fun esSingle(): Boolean = tipo == TIPO_SINGLE

    /**
     * Obtiene géneros como lista
     */
    fun obtenerGeneros(): List<String> {
        return generos?.split(",")?.map { it.trim() } ?: emptyList()
    }

    companion object {
        // Tipos de álbum
        const val TIPO_ALBUM = "ALBUM"
        const val TIPO_EP = "EP"
        const val TIPO_SINGLE = "SINGLE"
        const val TIPO_COMPILATION = "COMPILATION"
        const val TIPO_LIVE = "LIVE"
        const val TIPO_SOUNDTRACK = "SOUNDTRACK"
        const val TIPO_MIXTAPE = "MIXTAPE"
        const val TIPO_DEMO = "DEMO"

        /**
         * Determina el tipo de álbum basado en cantidad de canciones
         */
        fun determinarTipo(totalCanciones: Int, duracionMinutos: Int): String {
            return when {
                totalCanciones == 1 -> TIPO_SINGLE
                totalCanciones <= 6 && duracionMinutos < 30 -> TIPO_EP
                else -> TIPO_ALBUM
            }
        }
    }
}