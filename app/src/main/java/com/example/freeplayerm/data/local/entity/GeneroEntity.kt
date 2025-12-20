// en: app/src/main/java/com/example/freeplayerm/data/local/entity/GeneroEntity.kt
package com.example.freeplayerm.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 🎸 GENERO ENTITY - OPTIMIZADA v2.0
 *
 * Entidad que representa un género musical
 * Incluye información descriptiva, icono y estadísticas
 *
 * Características:
 * - Índice único en nombre para evitar duplicados
 * - Color y emoji para visualización
 * - Descripción y género padre para jerarquías
 * - Estadísticas de uso
 *
 * @version 2.0 - Enhanced
 */
@Entity(
    tableName = "generos",
    indices = [
        Index(value = ["nombre"], unique = true),
        Index(value = ["nombre_normalizado"]),
        Index(value = ["total_canciones"]),
        Index(value = ["es_popular"])
    ]
)
data class GeneroEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_genero")
    val idGenero: Int = 0,

    // ==================== INFORMACIÓN BÁSICA ====================

    @ColumnInfo(name = "nombre")
    val nombre: String,

    @ColumnInfo(name = "nombre_normalizado")
    val nombreNormalizado: String = nombre.lowercase().trim(),

    @ColumnInfo(name = "nombre_en_ingles")
    val nombreEnIngles: String? = null, // Para integración con APIs

    @ColumnInfo(name = "descripcion")
    val descripcion: String? = null,

    // ==================== JERARQUÍA ====================

    @ColumnInfo(name = "genero_padre_id")
    val generoPadreId: Int? = null, // Para subgéneros (ej: "Heavy Metal" -> padre: "Rock")

    @ColumnInfo(name = "subgeneros")
    val subgeneros: String? = null, // Lista de subgéneros separados por coma

    // ==================== VISUALIZACIÓN ====================

    @ColumnInfo(name = "color")
    val color: String? = null, // Color en formato hexadecimal (#RRGGBB)

    @ColumnInfo(name = "emoji")
    val emoji: String? = null, // Emoji representativo (🎸, 🎹, 🎤, etc.)

    @ColumnInfo(name = "icono_url")
    val iconoUrl: String? = null,

    @ColumnInfo(name = "icono_path_local")
    val iconoPathLocal: String? = null,

    // ==================== ESTADÍSTICAS ====================

    @ColumnInfo(name = "total_canciones")
    val totalCanciones: Int = 0,

    @ColumnInfo(name = "total_artistas")
    val totalArtistas: Int = 0,

    @ColumnInfo(name = "total_albumes")
    val totalAlbumes: Int = 0,

    @ColumnInfo(name = "total_reproducciones")
    val totalReproducciones: Int = 0,

    // ==================== CLASIFICACIÓN ====================

    @ColumnInfo(name = "es_popular")
    val esPopular: Boolean = false, // Si es un género popular

    @ColumnInfo(name = "decada_origen")
    val decadaOrigen: String? = null, // Década de origen (1950s, 1980s, etc.)

    @ColumnInfo(name = "pais_origen")
    val paisOrigen: String? = null,

    // ==================== METADATA ====================

    @ColumnInfo(name = "fecha_agregado")
    val fechaAgregado: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "ultima_actualizacion")
    val ultimaActualizacion: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "orden_visualizacion")
    val ordenVisualizacion: Int = 0 // Para ordenamiento personalizado
) {
    /**
     * Obtiene el icono a usar (prioriza local, luego URL)
     */
    fun obtenerIcono(): String? = iconoPathLocal ?: iconoUrl

    /**
     * Verifica si tiene icono
     */
    fun tieneIcono(): Boolean = !iconoPathLocal.isNullOrBlank() || !iconoUrl.isNullOrBlank()

    /**
     * Obtiene el texto para mostrar (nombre + emoji si existe)
     */
    fun nombreConEmoji(): String = if (emoji != null) "$emoji $nombre" else nombre

    /**
     * Obtiene los subgéneros como lista
     */
    fun obtenerSubgeneros(): List<String> {
        return subgeneros?.split(",")?.map { it.trim() } ?: emptyList()
    }

    /**
     * Verifica si es un género principal (no tiene padre)
     */
    fun esPrincipal(): Boolean = generoPadreId == null

    /**
     * Verifica si es un subgénero
     */
    fun esSubgenero(): Boolean = generoPadreId != null

    companion object {
        // Géneros musicales principales
        val GENEROS_PRINCIPALES = listOf(
            "Rock", "Pop", "Hip Hop", "Rap", "Jazz", "Blues",
            "Electrónica", "Reggae", "Country", "Folk", "Latina",
            "Clásica", "Metal", "Punk", "Soul", "R&B", "Funk",
            "Indie", "Alternative", "Dance", "House", "Techno"
        )

        // Colores sugeridos por género
        val COLORES_GENERO = mapOf(
            "Rock" to "#E74C3C",
            "Pop" to "#3498DB",
            "Hip Hop" to "#9B59B6",
            "Rap" to "#8E44AD",
            "Jazz" to "#F39C12",
            "Blues" to "#2980B9",
            "Electrónica" to "#1ABC9C",
            "Reggae" to "#27AE60",
            "Country" to "#E67E22",
            "Folk" to "#95A5A6",
            "Latina" to "#E74C3C",
            "Clásica" to "#34495E",
            "Metal" to "#2C3E50",
            "Punk" to "#C0392B",
            "Soul" to "#D35400",
            "R&B" to "#16A085",
            "Indie" to "#7F8C8D"
        )

        // Emojis sugeridos por género
        val EMOJIS_GENERO = mapOf(
            "Rock" to "🎸",
            "Pop" to "🎤",
            "Hip Hop" to "🎧",
            "Rap" to "🎤",
            "Jazz" to "🎷",
            "Blues" to "🎺",
            "Electrónica" to "🎹",
            "Reggae" to "🌴",
            "Country" to "🤠",
            "Folk" to "🎻",
            "Latina" to "💃",
            "Clásica" to "🎼",
            "Metal" to "🤘",
            "Punk" to "💀",
            "Soul" to "✨",
            "R&B" to "🎵",
            "Indie" to "🎨"
        )

        /**
         * Normaliza un nombre de género
         */
        fun normalizar(nombre: String): String {
            return nombre.lowercase().trim()
                .replace("á", "a")
                .replace("é", "e")
                .replace("í", "i")
                .replace("ó", "o")
                .replace("ú", "u")
                .replace("ñ", "n")
        }

        /**
         * Obtiene el color sugerido para un género
         */
        fun obtenerColorSugerido(nombre: String): String? {
            return COLORES_GENERO[nombre]
        }

        /**
         * Obtiene el emoji sugerido para un género
         */
        fun obtenerEmojiSugerido(nombre: String): String? {
            return EMOJIS_GENERO[nombre]
        }
    }
}