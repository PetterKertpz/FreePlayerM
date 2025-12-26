package com.example.freeplayerm.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entidad para almacenar moods (estados de ánimo) y características de géneros musicales.
 *
 * Permite clasificar géneros por características emocionales y técnicas:
 * - Moods: energetic, sad, happy, relaxing, aggressive, romantic, etc.
 * - Características técnicas: tempo promedio, energía, valencia (positividad)
 *
 * Relaciones:
 * - N:1 con GeneroEntity (un género puede tener múltiples moods)
 *
 * Casos de uso:
 * - Recomendar música por estado de ánimo
 * - Crear playlists automáticas por mood
 * - Filtrar canciones por características emocionales
 * - Análisis de patrones musicales
 *
 * @property idMood ID único del mood
 * @property idGenero ID del género (foreign key)
 * @property mood Nombre del mood/característica
 * @property intensidad Intensidad del mood en este género (0.0-1.0)
 * @property tipo Tipo de característica (MOOD, ENERGIA, TEMPO, etc.)
 * @property descripcion Descripción del mood
 * @property fechaCreacion Timestamp de creación
 */
@Entity(
    tableName = "genero_mood",
    foreignKeys = [
        ForeignKey(
            entity = GeneroEntity::class,
            parentColumns = ["id_genero"],
            childColumns = ["id_genero"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["id_genero"]),
        Index(value = ["mood"]),
        Index(value = ["tipo"]),
        Index(value = ["intensidad"]),
        Index(value = ["id_genero", "mood"], unique = true) // Un mood por género
    ]
)
data class GeneroMoodEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_mood")
    val idMood: Int = 0,

    // ==================== RELACIÓN CON GÉNERO ====================

    @ColumnInfo(name = "id_genero")
    val idGenero: Int,

    // ==================== INFORMACIÓN DEL MOOD ====================

    /**
     * Nombre del mood o característica
     * Ejemplos: "energetic", "melancholic", "aggressive", "romantic", "chill"
     */
    @ColumnInfo(name = "mood")
    val mood: String,

    /**
     * Intensidad del mood en este género
     * Rango: 0.0 (nada presente) a 1.0 (muy presente)
     *
     * Ejemplo: Rock podría tener "energetic" = 0.9, "sad" = 0.3
     */
    @ColumnInfo(name = "intensidad")
    val intensidad: Float,

    // ==================== TIPO DE CARACTERÍSTICA ====================

    /**
     * Tipo de característica
     * Valores: MOOD, ENERGIA, TEMPO, VALENCIA, ACUSTICA, INSTRUMENTAL, DANCEABILITY
     */
    @ColumnInfo(name = "tipo")
    val tipo: String = "MOOD",

    // ==================== DESCRIPCIÓN ====================

    /**
     * Descripción del mood
     */
    @ColumnInfo(name = "descripcion")
    val descripcion: String? = null,

    /**
     * Tags adicionales relacionados (JSON array)
     * Ejemplo: ["party", "workout", "study"]
     */
    @ColumnInfo(name = "tags_json")
    val tagsJson: String? = null,

    // ==================== METADATOS TÉCNICOS ====================

    /**
     * Rango de tempo promedio para este mood en este género (BPM)
     * JSON: {"min": 120, "max": 140}
     */
    @ColumnInfo(name = "tempo_range_json")
    val tempoRangeJson: String? = null,

    /**
     * Nivel de energía musical (0.0-1.0)
     * Basado en análisis de Spotify/MusicBrainz
     */
    @ColumnInfo(name = "energia")
    val energia: Float? = null,

    /**
     * Valencia (positividad) (0.0-1.0)
     * 0.0 = muy negativo/triste, 1.0 = muy positivo/feliz
     */
    @ColumnInfo(name = "valencia")
    val valencia: Float? = null,

    /**
     * Danceabilidad (0.0-1.0)
     * Qué tan bailable es este mood del género
     */
    @ColumnInfo(name = "danceability")
    val danceability: Float? = null,

    /**
     * Nivel acústico (0.0-1.0)
     * 0.0 = totalmente electrónico, 1.0 = totalmente acústico
     */
    @ColumnInfo(name = "acustica")
    val acustica: Float? = null,

    /**
     * Nivel instrumental (0.0-1.0)
     * 0.0 = muchas voces, 1.0 = totalmente instrumental
     */
    @ColumnInfo(name = "instrumental")
    val instrumental: Float? = null,

    // ==================== CONTEXTO DE USO ====================

    /**
     * Momentos del día recomendados (JSON array)
     * Ejemplo: ["morning", "afternoon", "night", "late_night"]
     */
    @ColumnInfo(name = "momentos_dia_json")
    val momentosDiaJson: String? = null,

    /**
     * Actividades recomendadas (JSON array)
     * Ejemplo: ["workout", "study", "party", "relax", "sleep", "drive"]
     */
    @ColumnInfo(name = "actividades_json")
    val actividadesJson: String? = null,

    // ==================== METADATOS ====================

    /**
     * Fuente de los datos
     * Valores: "SPOTIFY", "MUSICBRAINZ", "MANUAL", "ML_MODEL"
     */
    @ColumnInfo(name = "fuente")
    val fuente: String = "MANUAL",

    /**
     * Confianza en la clasificación (0.0-1.0)
     * Útil si se usa ML para clasificar
     */
    @ColumnInfo(name = "confianza")
    val confianza: Float = 1.0f,

    @ColumnInfo(name = "fecha_creacion")
    val fechaCreacion: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "activo")
    val activo: Boolean = true

) {
    companion object {
        /**
         * Moods disponibles
         */
        object Moods {
            // Estados emocionales
            const val HAPPY = "happy"
            const val SAD = "sad"
            const val ENERGETIC = "energetic"
            const val CALM = "calm"
            const val ANGRY = "angry"
            const val ROMANTIC = "romantic"
            const val MELANCHOLIC = "melancholic"
            const val EUPHORIC = "euphoric"
            const val ANXIOUS = "anxious"
            const val NOSTALGIC = "nostalgic"
            const val MOTIVATIONAL = "motivational"
            const val DREAMY = "dreamy"

            // Características de intensidad
            const val AGGRESSIVE = "aggressive"
            const val GENTLE = "gentle"
            const val INTENSE = "intense"
            const val CHILL = "chill"
            const val DARK = "dark"
            const val BRIGHT = "bright"
            const val EPIC = "epic"
            const val INTIMATE = "intimate"

            // Contextuales
            const val PARTY = "party"
            const val WORKOUT = "workout"
            const val STUDY = "study"
            const val SLEEP = "sleep"
            const val DRIVE = "drive"
            const val FOCUS = "focus"

            /**
             * Obtiene el nombre para mostrar del mood
             */
            fun obtenerNombreDisplay(mood: String): String {
                return when (mood) {
                    HAPPY -> "Feliz"
                    SAD -> "Triste"
                    ENERGETIC -> "Energético"
                    CALM -> "Calmado"
                    ANGRY -> "Enojado"
                    ROMANTIC -> "Romántico"
                    MELANCHOLIC -> "Melancólico"
                    EUPHORIC -> "Eufórico"
                    ANXIOUS -> "Ansioso"
                    NOSTALGIC -> "Nostálgico"
                    MOTIVATIONAL -> "Motivacional"
                    DREAMY -> "Soñador"
                    AGGRESSIVE -> "Agresivo"
                    GENTLE -> "Gentil"
                    INTENSE -> "Intenso"
                    CHILL -> "Relajado"
                    DARK -> "Oscuro"
                    BRIGHT -> "Brillante"
                    EPIC -> "Épico"
                    INTIMATE -> "Íntimo"
                    PARTY -> "Fiesta"
                    WORKOUT -> "Ejercicio"
                    STUDY -> "Estudio"
                    SLEEP -> "Dormir"
                    DRIVE -> "Conducir"
                    FOCUS -> "Enfoque"
                    else -> mood.replaceFirstChar { it.uppercase() }
                }
            }

            /**
             * Obtiene el emoji asociado al mood
             */
            fun obtenerEmoji(mood: String): String {
                return when (mood) {
                    HAPPY -> "😊"
                    SAD -> "😢"
                    ENERGETIC -> "⚡"
                    CALM -> "😌"
                    ANGRY -> "😠"
                    ROMANTIC -> "💕"
                    MELANCHOLIC -> "🌧️"
                    EUPHORIC -> "🎉"
                    ANXIOUS -> "😰"
                    NOSTALGIC -> "🕰️"
                    MOTIVATIONAL -> "💪"
                    DREAMY -> "✨"
                    AGGRESSIVE -> "🔥"
                    GENTLE -> "🌸"
                    INTENSE -> "⚡"
                    CHILL -> "🌊"
                    DARK -> "🌑"
                    BRIGHT -> "☀️"
                    EPIC -> "🏔️"
                    INTIMATE -> "🕯️"
                    PARTY -> "🎊"
                    WORKOUT -> "🏋️"
                    STUDY -> "📚"
                    SLEEP -> "😴"
                    DRIVE -> "🚗"
                    FOCUS -> "🎯"
                    else -> "🎵"
                }
            }

            /**
             * Obtiene color asociado al mood (hex)
             */
            fun obtenerColor(mood: String): String {
                return when (mood) {
                    HAPPY -> "#FFD700"
                    SAD -> "#4682B4"
                    ENERGETIC -> "#FF6347"
                    CALM -> "#87CEEB"
                    ANGRY -> "#DC143C"
                    ROMANTIC -> "#FF69B4"
                    MELANCHOLIC -> "#778899"
                    EUPHORIC -> "#FF1493"
                    NOSTALGIC -> "#D2B48C"
                    MOTIVATIONAL -> "#FF4500"
                    DREAMY -> "#DDA0DD"
                    CHILL -> "#20B2AA"
                    DARK -> "#2F4F4F"
                    BRIGHT -> "#FFFFE0"
                    else -> "#808080"
                }
            }
        }

        /**
         * Tipos de características
         */
        object Tipo {
            const val MOOD = "MOOD" // Estado emocional
            const val ENERGIA = "ENERGIA" // Nivel de energía
            const val TEMPO = "TEMPO" // Características de tempo
            const val VALENCIA = "VALENCIA" // Positividad
            const val ACUSTICA = "ACUSTICA" // Nivel acústico
            const val INSTRUMENTAL = "INSTRUMENTAL" // Nivel instrumental
            const val DANCEABILITY = "DANCEABILITY" // Bailable
        }

        /**
         * Crea un mood básico
         */
        fun crear(
            idGenero: Int,
            mood: String,
            intensidad: Float,
            tipo: String = Tipo.MOOD
        ): GeneroMoodEntity {
            require(intensidad in 0.0f..1.0f) { "Intensidad debe estar entre 0.0 y 1.0" }

            return GeneroMoodEntity(
                idGenero = idGenero,
                mood = mood,
                intensidad = intensidad,
                tipo = tipo,
                descripcion = Moods.obtenerNombreDisplay(mood)
            )
        }
    }

    /**
     * Valida que los valores estén en rangos correctos
     */
    fun esValido(): Boolean {
        return intensidad in 0.0f..1.0f &&
                (energia == null || energia in 0.0f..1.0f) &&
                (valencia == null || valencia in 0.0f..1.0f) &&
                (danceability == null || danceability in 0.0f..1.0f) &&
                (acustica == null || acustica in 0.0f..1.0f) &&
                (instrumental == null || instrumental in 0.0f..1.0f) &&
                (confianza in 0.0f..1.0f)
    }

    /**
     * Indica si es una característica fuerte (intensidad > 0.7)
     */
    fun esFuerte(): Boolean {
        return intensidad >= 0.7f && activo
    }

    /**
     * Indica si es una característica débil (intensidad < 0.3)
     */
    fun esDebil(): Boolean {
        return intensidad < 0.3f
    }

    /**
     * Obtiene el nombre para mostrar
     */
    fun obtenerNombreDisplay(): String {
        return Moods.obtenerNombreDisplay(mood)
    }

    /**
     * Obtiene el emoji
     */
    fun obtenerEmoji(): String {
        return Moods.obtenerEmoji(mood)
    }

    /**
     * Obtiene el color
     */
    fun obtenerColor(): String {
        return Moods.obtenerColor(mood)
    }

    /**
     * Formatea la intensidad como porcentaje
     */
    fun formatearIntensidad(): String {
        return "${(intensidad * 100).toInt()}%"
    }

    /**
     * Determina si coincide con un filtro de intensidad
     */
    fun coincideConFiltro(minimaIntensidad: Float): Boolean {
        return intensidad >= minimaIntensidad && activo
    }
}