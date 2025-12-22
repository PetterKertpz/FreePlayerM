// app/src/main/java/com/example/freeplayerm/utils/MusicTitleCleanerUtil.kt
package com.example.freeplayerm.utils

/**
 * 🎵 MUSIC TITLE CLEANER UTIL
 *
 * Utilidad especializada para limpiar títulos de canciones y nombres de artistas
 * Elimina patrones comunes de YouTube, plataformas de streaming y metadata extra
 *
 * Características:
 * - Limpieza de patrones de YouTube (Official Video, Audio, etc.)
 * - Eliminación de featured artists del título
 * - Detección de contenido no musical
 * - Normalización de caracteres especiales
 * - Extracción de información estructurada
 *
 * @version 1.0 - Refactorización de código duplicado
 */
object MusicTitleCleanerUtil {

    // ==================== PATRONES DE LIMPIEZA ====================

    private val YOUTUBE_PATTERNS = listOf(
        "- Topic",
        "(Official Video)",
        "(Official Audio)",
        "(Official Music Video)",
        "(Official Lyric Video)",
        "(Lyric Video)",
        "(Audio)",
        "(Video)",
        "VEVO",
        "Official"
    )

    private val REGEX_PATTERNS = listOf(
        "\\[.*?\\]".toRegex(),                      // [Texto entre corchetes]
        "\\(.*?\\)".toRegex(),                      // (Texto entre paréntesis)
        "ft\\.?\\s*[^,]*".toRegex(RegexOption.IGNORE_CASE),  // ft. Artista
        "feat\\.?\\s*[^,]*".toRegex(RegexOption.IGNORE_CASE), // feat. Artista
        "featuring\\s*[^,]*".toRegex(RegexOption.IGNORE_CASE) // featuring Artista
    )

    private val NON_MUSIC_INDICATORS = listOf(
        // Tiempo/Duración
        "minute", "minutes", "hour", "hours",
        "\\d+\\s*min".toRegex(),
        "\\d+\\s*hr".toRegex(),

        // Contenido no musical
        "documentary", "interview", "behind the scenes",
        "making of", "trailer", "teaser",
        "lecture", "speech", "podcast", "talk",
        "episode", "chapter",
        "part\\s*\\d+".toRegex(),

        // IMAX y cine
        "imax", "cinema", "theater",

        // Reviews y análisis
        "review", "reaction", "analysis",

        // Tutoriales
        "tutorial", "how to", "guide"
    )

    private val QUALITY_INDICATORS = listOf(
        "HD", "HQ", "4K", "8K", "1080p", "720p", "480p",
        "High Quality", "High Definition"
    )

    // ==================== FUNCIONES DE LIMPIEZA ====================

    /**
     * Limpia un título de canción eliminando patrones comunes
     * Mantiene solo el título esencial
     */
    fun cleanTitle(title: String): String {
        var clean = title.trim()

        // 1. Eliminar patrones de YouTube
        YOUTUBE_PATTERNS.forEach { pattern ->
            clean = clean.replace(pattern, "", ignoreCase = true)
        }

        // 2. Eliminar indicadores de calidad
        QUALITY_INDICATORS.forEach { indicator ->
            clean = clean.replace(indicator, "", ignoreCase = true)
        }

        // 3. Eliminar featured artists (mantener solo artista principal)
        REGEX_PATTERNS.forEach { regex ->
            clean = regex.replace(clean, "")
        }

        // 4. Limpiar caracteres especiales y espacios múltiples
        clean = clean
            .replace(Regex("\\s+"), " ")
            .replace(Regex("[_-]{2,}"), " ")
            .trim()

        return clean
    }

    /**
     * Limpia un nombre de artista eliminando sufijos comunes
     */
    fun cleanArtistName(artist: String): String {
        var clean = artist.trim()

        // Patrones específicos de artistas
        val artistPatterns = listOf(
            "- Topic",
            "VEVO",
            "Official",
            "Music"
        )

        artistPatterns.forEach { pattern ->
            clean = clean.replace(pattern, "", ignoreCase = true)
        }

        return clean.trim()
    }

    /**
     * Preprocesa búsqueda completa (título + artista)
     * Devuelve par de (título limpio, artista limpio)
     */
    fun preprocessSearch(title: String, artist: String?): Pair<String, String> {
        val cleanedTitle = cleanTitle(title)
        val cleanedArtist = artist?.let { cleanArtistName(it) } ?: ""

        return cleanedTitle to cleanedArtist
    }

    /**
     * Verifica si el contenido es musical
     * Detecta videos, podcasts, documentales, etc.
     */
    fun isMusicalContent(title: String, artist: String? = null): Boolean {
        val fullText = "$title ${artist ?: ""}".lowercase()

        return NON_MUSIC_INDICATORS.none { indicator ->
            when (indicator) {
                is String -> fullText.contains(indicator, ignoreCase = true)
                is Regex -> indicator.containsMatchIn(fullText)
                else -> false
            }
        }
    }

    /**
     * Extrae featured artists del título
     * Devuelve lista de artistas colaboradores
     */
    fun extractFeaturedArtists(title: String): List<String> {
        val featuredArtists = mutableListOf<String>()

        // Patrón para detectar featured artists
        val featPattern = """(?:ft\.?|feat\.?|featuring)\s*([^()\[\]]+)""".toRegex(RegexOption.IGNORE_CASE)

        featPattern.findAll(title).forEach { match ->
            val artists = match.groupValues[1]
                .split("&", ",", "and")
                .map { it.trim() }
                .filter { it.isNotBlank() }

            featuredArtists.addAll(artists)
        }

        return featuredArtists
    }

    /**
     * Limpia texto para búsqueda (elimina caracteres especiales)
     * Mantiene solo caracteres alfanuméricos y espacios
     */
    fun cleanForSearch(text: String): String {
        return text
            .replace(Regex("[^a-zA-Z0-9\\sáéíóúñüÁÉÍÓÚÑÜ]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    /**
     * Normaliza texto eliminando acentos
     * Útil para comparaciones case-insensitive
     */
    fun normalizeText(text: String): String {
        return text
            .lowercase()
            .replace('á', 'a')
            .replace('é', 'e')
            .replace('í', 'i')
            .replace('ó', 'o')
            .replace('ú', 'u')
            .replace('ñ', 'n')
            .replace('ü', 'u')
    }

    /**
     * Construye query de búsqueda optimizada
     * Combina título y artista de forma inteligente
     */
    fun buildSearchQuery(title: String, artist: String?): String {
        val cleanedTitle = cleanForSearch(cleanTitle(title))
        val cleanedArtist = artist?.let { cleanForSearch(cleanArtistName(it)) }

        return buildString {
            append(cleanedTitle)
            if (!cleanedArtist.isNullOrBlank()) {
                append(" ")
                append(cleanedArtist)
            }
        }.trim()
    }

    /**
     * Detecta si un título contiene remix/cover information
     */
    fun detectVersion(title: String): MusicVersion? {
        val lowerTitle = title.lowercase()

        return when {
            lowerTitle.contains("remix") -> MusicVersion.REMIX
            lowerTitle.contains("cover") -> MusicVersion.COVER
            lowerTitle.contains("acoustic") -> MusicVersion.ACOUSTIC
            lowerTitle.contains("live") -> MusicVersion.LIVE
            lowerTitle.contains("instrumental") -> MusicVersion.INSTRUMENTAL
            lowerTitle.contains("karaoke") -> MusicVersion.KARAOKE
            else -> null
        }
    }

    /**
     * Extrae año del título si está presente
     * Busca patrones como (2023), [2023], 2023
     */
    fun extractYear(title: String): Int? {
        val yearPattern = """[(\[]?(\d{4})[)\]]?""".toRegex()
        val match = yearPattern.find(title)

        return match?.groupValues?.get(1)?.toIntOrNull()?.let { year ->
            if (year in 1900..2100) year else null
        }
    }

    /**
     * Divide título en componentes estructurados
     */
    fun parseTitle(title: String): ParsedTitle {
        val cleanedTitle = cleanTitle(title)
        val featuredArtists = extractFeaturedArtists(title)
        val version = detectVersion(title)
        val year = extractYear(title)

        // Extraer título principal (sin featured artists)
        var mainTitle = cleanedTitle
        featuredArtists.forEach { artist ->
            mainTitle = mainTitle.replace(artist, "", ignoreCase = true)
        }
        mainTitle = mainTitle
            .replace(Regex("""(?:ft\.?|feat\.?|featuring)""", RegexOption.IGNORE_CASE), "")
            .replace(Regex("[(),\\[\\]]"), "")
            .replace(Regex("\\s+"), " ")
            .trim()

        return ParsedTitle(
            mainTitle = mainTitle,
            featuredArtists = featuredArtists,
            version = version,
            year = year,
            originalTitle = title
        )
    }

    /**
     * Valida si un título es válido (no vacío, longitud razonable)
     */
    fun isValidTitle(title: String, minLength: Int = 1, maxLength: Int = 200): Boolean {
        val cleaned = cleanTitle(title)
        return cleaned.length in minLength..maxLength
    }

    // ==================== DATA CLASSES ====================

    enum class MusicVersion {
        REMIX, COVER, ACOUSTIC, LIVE, INSTRUMENTAL, KARAOKE
    }

    data class ParsedTitle(
        val mainTitle: String,
        val featuredArtists: List<String>,
        val version: MusicVersion?,
        val year: Int?,
        val originalTitle: String
    ) {
        /**
         * Reconstruye el título con formato limpio
         */
        fun toCleanString(): String = buildString {
            append(mainTitle)

            if (featuredArtists.isNotEmpty()) {
                append(" (feat. ")
                append(featuredArtists.joinToString(", "))
                append(")")
            }

            version?.let {
                append(" [")
                append(it.name.lowercase().replaceFirstChar { c -> c.uppercase() })
                append("]")
            }

            year?.let {
                append(" ($it)")
            }
        }
    }

    // ==================== UTILIDADES DE DETECCIÓN ====================

    /**
     * Detecta si es un título de YouTube típico
     */
    fun isYouTubeStyleTitle(title: String): Boolean {
        return YOUTUBE_PATTERNS.any { pattern ->
            title.contains(pattern, ignoreCase = true)
        }
    }

    /**
     * Calcula confianza de que es contenido musical (0.0 a 1.0)
     */
    fun calculateMusicConfidence(title: String, artist: String?): Double {
        var confidence = 1.0
        val fullText = "$title ${artist ?: ""}".lowercase()

        // Penalizar por indicadores no musicales
        NON_MUSIC_INDICATORS.forEach { indicator ->
            val found = when (indicator) {
                is String -> fullText.contains(indicator, ignoreCase = true)
                is Regex -> indicator.containsMatchIn(fullText)
                else -> false
            }
            if (found) confidence -= 0.15
        }

        // Bonus si tiene indicadores musicales
        val musicIndicators = listOf("official", "audio", "music", "song")
        musicIndicators.forEach { indicator ->
            if (fullText.contains(indicator)) confidence += 0.05
        }

        return confidence.coerceIn(0.0, 1.0)
    }
}