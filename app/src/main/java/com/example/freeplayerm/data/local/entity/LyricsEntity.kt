// en: app/src/main/java/com/example/freeplayerm/data/local/entity/LyricsEntity.kt
package com.example.freeplayerm.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 📝 LETRA ENTITY - OPTIMIZADA v2.0
 *
 * Entidad que representa la letra de una canción Incluye metadata sobre la fuente y fecha de
 * obtención
 *
 * Características:
 * - Relación 1:1 con SongEntity
 * - CASCADE delete: si se borra la canción, se borra la letra
 * - Soporte para múltiples fuentes (Genius, Musixmatch, manual, etc.)
 * - Timestamp de cuándo se obtuvo la letra
 *
 * @version 2.0 - Enhanced & Fixed
 */
@Entity(
    tableName = "letras",
    foreignKeys =
        [
            ForeignKey(
                entity = SongEntity::class,
                parentColumns = ["id_cancion"],
                childColumns = ["id_cancion"],
                onDelete = ForeignKey.CASCADE, // Si se borra la canción, se borra la letra
            )
        ],
    indices =
        [
            Index(value = ["id_cancion"], unique = true), // Una canción solo puede tener una letra
            Index(value = ["fuente"]),
            Index(value = ["fecha_agregado"]),
        ],
)
data class LyricsEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id_letra") val idLetra: Int = 0,
    @ColumnInfo(name = "id_cancion")
    val idCancion: Int, // ID de la canción asociada (Relación 1 a 1)
    @ColumnInfo(name = "texto_letra") val textoLetra: String, // Texto completo de la letra
    @ColumnInfo(name = "fuente")
    val fuente: String = "manual", // Fuente de donde se obtuvo la letra
    @ColumnInfo(name = "fecha_agregado")
    val fechaAgregado: Long = System.currentTimeMillis(), // Timestamp de cuándo se agregó

    /**
     * Idioma de la letra original (ISO 639-1) NOTA: SongEntity.idioma indica el idioma de la
     * canción (puede diferir) Ejemplos: "es", "en", "fr", "pt"
     */
    @ColumnInfo(name = "idioma") val idioma: String? = null,

    /** Si hay traducciones disponibles DEPRECADO: Usar query a LyricsTranslationEntity.count() */
    @Deprecated("Calcular dinámicamente desde LyricsTranslationEntity")
    @ColumnInfo(name = "traduccion_disponible")
    val traduccionDisponible: Boolean = false,

    /** Si tiene timestamps LRC (letra sincronizada) Formato: "[00:12.00] Texto de la línea" */
    @ColumnInfo(name = "sincronizada") val sincronizada: Boolean = false,

    /** Contenido LRC completo si sincronizada = true */
    @ColumnInfo(name = "lrc_content") val lrcContent: String? = null,
    @ColumnInfo(name = "url_fuente")
    val urlFuente: String? = null, // URL de donde se obtuvo (si aplica)
    @ColumnInfo(name = "verificada")
    val verificada: Boolean = false, // Si la letra ha sido verificada/validada
) {
    /** Obtiene un preview de la letra (primeras líneas) */
    fun obtenerPreview(lineas: Int = 4): String {
        return textoLetra.lines().take(lineas).joinToString("\n")
    }

    /** Cuenta el número de líneas */
    fun contarLineas(): Int = textoLetra.lines().size

    /** Cuenta el número de palabras */
    fun contarPalabras(): Int = textoLetra.split(Regex("\\s+")).size

    /** Verifica si la letra es válida (no vacía, mínimo de caracteres) */
    fun esValida(): Boolean = textoLetra.isNotBlank() && textoLetra.length >= 10

    /** Busca una palabra o frase en la letra (case insensitive) */
    fun contiene(query: String): Boolean = textoLetra.contains(query, ignoreCase = true)

    companion object {
        // Constantes para fuentes comunes
        const val FUENTE_MANUAL = "manual"
        const val FUENTE_GENIUS = "genius"
        const val FUENTE_MUSIXMATCH = "musixmatch"
        const val FUENTE_LYRICS_OVH = "lyrics.ovh"
        const val FUENTE_CHARTLYRICS = "chartlyrics"
        const val FUENTE_AZLYRICS = "azlyrics"
        const val FUENTE_IMPORTADA = "importada"

        // Idiomas comunes
        const val IDIOMA_ESPANOL = "es"
        const val IDIOMA_INGLES = "en"
        const val IDIOMA_FRANCES = "fr"
        const val IDIOMA_PORTUGUES = "pt"
        const val IDIOMA_ITALIANO = "it"
    }
}
