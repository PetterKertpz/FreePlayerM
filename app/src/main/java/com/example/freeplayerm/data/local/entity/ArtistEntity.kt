// en: app/src/main/java/com/example/freeplayerm/data/local/entity/ArtistEntity.kt
package com.example.freeplayerm.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 🎤 ARTISTA ENTITY - OPTIMIZADA v2.0
 *
 * Entidad que representa un artista musical Incluye información biográfica, imágenes y metadata
 *
 * Características:
 * - Índice único en nombre para evitar duplicados
 * - Integración con Genius API
 * - Soporte para múltiples imágenes
 * - Verificación de artistas populares
 * - Estadísticas de reproducción
 *
 * @version 2.0 - Enhanced
 */
@Entity(
    tableName = "artistas",
    indices =
        [
            Index(value = ["nombre"], unique = true),
            Index(value = ["nombre_normalizado"]), // Para búsquedas sin acentos
            Index(value = ["es_popular"]),
            Index(value = ["fecha_agregado"]),
        ],
)
data class ArtistEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id_artista") val idArtista: Int = 0,

    // ==================== INFORMACIÓN BÁSICA ====================

    @ColumnInfo(name = "nombre") val nombre: String,
    @ColumnInfo(name = "nombre_normalizado")
    val nombreNormalizado: String = nombre.lowercase().trim(), // Para búsquedas
    @ColumnInfo(name = "nombre_real")
    val nombreReal: String? = null, // Nombre real del artista (si es diferente del artístico)

    // ==================== INFORMACIÓN GEOGRÁFICA ====================

    @ColumnInfo(name = "pais_origen") val paisOrigen: String? = null,
    @ColumnInfo(name = "ciudad_origen") val ciudadOrigen: String? = null,

    // ==================== BIOGRAFÍA ====================

    @ColumnInfo(name = "descripcion") val descripcion: String? = null,
    @ColumnInfo(name = "biografia") val biografia: String? = null,
    @ColumnInfo(name = "fecha_nacimiento") val fechaNacimiento: Long? = null, // Timestamp
    @ColumnInfo(name = "fecha_inicio_carrera") val fechaInicioCarrera: Long? = null, // Año

    // ==================== IMÁGENES ====================

    @ColumnInfo(name = "image_url") val imageUrl: String? = null,
    @ColumnInfo(name = "image_path_local") val imagePathLocal: String? = null,
    @ColumnInfo(name = "header_image_url")
    val headerImageUrl: String? = null, // Path local si la descargamos
    @ColumnInfo(name = "thumbnail_url") val thumbnailUrl: String? = null, // Miniatura
    @ColumnInfo(name = "banner_url") val bannerUrl: String? = null, // Banner para perfil

    // ==================== GENIUS API ====================

    @ColumnInfo(name = "genius_id") val geniusId: String? = null, // ID de Genius
    @ColumnInfo(name = "genius_url") val geniusUrl: String? = null, // URL del artista en Genius

    // ==================== REDES SOCIALES Y WEB ====================

    @ColumnInfo(name = "sitio_web") val sitioWeb: String? = null,
    @Deprecated("Usar ArtistSocialLinksEntity", ReplaceWith("ArtistSocialLinksEntity"))
    @ColumnInfo(name = "instagram")
    val instagram: String? = null,
    @Deprecated("Usar ArtistSocialLinksEntity", ReplaceWith("ArtistSocialLinksEntity"))
    @ColumnInfo(name = "twitter")
    val twitter: String? = null,
    @Deprecated("Usar ArtistSocialLinksEntity", ReplaceWith("ArtistSocialLinksEntity"))
    @ColumnInfo(name = "facebook")
    val facebook: String? = null,
    @Deprecated("Usar ArtistSocialLinksEntity", ReplaceWith("ArtistSocialLinksEntity"))
    @ColumnInfo(name = "youtube")
    val youtube: String? = null,
    @ColumnInfo(name = "spotify_id") val spotifyId: String? = null,

    // ==================== CLASIFICACIÓN ====================

    @ColumnInfo(name = "generos")
    val generos: String? = null, // Lista de géneros separados por coma
    @ColumnInfo(name = "tipo") val tipo: String = TIPO_SOLISTA, // SOLISTA, BANDA, DUO, GRUPO
    @ColumnInfo(name = "es_verificado")
    val esVerificado: Boolean = false, // Si está verificado oficialmente
    @ColumnInfo(name = "es_popular")
    val esPopular: Boolean = false, // Si es un artista popular (para destacar)

    // ==================== ESTADÍSTICAS ====================

    @ColumnInfo(name = "total_canciones") val totalCanciones: Int = 0,
    @ColumnInfo(name = "total_albumes") val totalAlbumes: Int = 0,
    @ColumnInfo(name = "total_reproducciones") val totalReproducciones: Int = 0,
    @ColumnInfo(name = "veces_favorito")
    val vecesFavorito: Int = 0, // Cuántos usuarios lo tienen como favorito

    // ==================== METADATA ====================

    @ColumnInfo(name = "fecha_agregado") val fechaAgregado: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "ultima_actualizacion")
    val ultimaActualizacion: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "fuente") val fuente: String = FUENTE_LOCAL, // LOCAL, GENIUS, SPOTIFY, etc.
) {
    /** Obtiene la imagen a usar (prioriza local, luego URL) */
    fun obtenerImagen(): String? = imagePathLocal ?: imageUrl

    /** Verifica si tiene imagen */
    fun tieneImagen(): Boolean = !imagePathLocal.isNullOrBlank() || !imageUrl.isNullOrBlank()

    /** Obtiene los géneros como lista */
    fun obtenerGeneros(): List<String> {
        return generos?.split(",")?.map { it.trim() } ?: emptyList()
    }

    /** Verifica si es una banda/grupo */
    fun esBanda(): Boolean = tipo == TIPO_BANDA || tipo == TIPO_GRUPO

    /** Verifica si es solista */
    fun esSolista(): Boolean = tipo == TIPO_SOLISTA

    companion object {
        // Tipos de artista
        const val TIPO_SOLISTA = "SOLISTA"
        const val TIPO_BANDA = "BANDA"
        const val TIPO_DUO = "DUO"
        const val TIPO_GRUPO = "GRUPO"
        const val TIPO_VARIOS = "VARIOS_ARTISTAS"

        // Fuentes
        const val FUENTE_LOCAL = "LOCAL"
        const val FUENTE_GENIUS = "GENIUS"
        const val FUENTE_SPOTIFY = "SPOTIFY"
        const val FUENTE_LASTFM = "LASTFM"
        const val FUENTE_MANUAL = "MANUAL"

        /** Normaliza un nombre de artista para búsquedas */
        fun normalizar(nombre: String): String {
            return nombre
                .lowercase()
                .trim()
                .replace("á", "a")
                .replace("é", "e")
                .replace("í", "i")
                .replace("ó", "o")
                .replace("ú", "u")
                .replace("ñ", "n")
        }
    }
}
