// app/src/main/java/com/example/freeplayerm/utils/MusicTitleCleaner.kt
package com.example.freeplayerm.utils

import java.text.Normalizer
import java.util.Locale

/**
 * 🎵 MUSIC TITLE CLEANER UTIL v2.0
 *
 * Utilidad especializada para limpiar títulos de canciones y nombres de artistas
 * Parte del Sistema de Purificación de Metadata Musical
 *
 * Características:
 * - Limpieza de patrones de YouTube, Spotify, Apple Music, etc.
 * - Extracción completa de featured artists
 * - Detección de contenido no musical con scoring
 * - Normalización Unicode completa
 * - Capitalización inteligente (Title Case)
 * - Extracción de versiones (Remix, Cover, Live, etc.)
 * - Extracción de año
 *
 * @version 2.0 - Sistema de Purificación de Metadata
 */
object MusicTitleCleaner {
   
   // ==================== PATRONES DE LIMPIEZA ====================
   
   /**
    * Patrones de YouTube y plataformas de video
    */
   private val YOUTUBE_PATTERNS = listOf(
      "- Topic",
      "(Official Video)",
      "(Official Audio)",
      "(Official Music Video)",
      "(Official Lyric Video)",
      "(Official Visualizer)",
      "(Lyric Video)",
      "(Lyrics)",
      "(Audio)",
      "(Video)",
      "(Visualizer)",
      "(Music Video)",
      "(Vertical Video)",
      "(Behind The Scenes)",
      "[Official Video]",
      "[Official Audio]",
      "[Music Video]",
      "[Lyric Video]",
      "[Lyrics]",
      "VEVO",
      "Official",
      "Oficial",
      "(Explicit)",
      "(Clean)",
      "(Censored)"
   )
   
   /**
    * Patrones de plataformas de streaming
    */
   private val STREAMING_PATTERNS = listOf(
      // Spotify
      "- Spotify Singles",
      "- Recorded at Spotify Studios",
      "(Spotify Sessions)",
      "(Spotify Singles)",
      
      // Apple Music
      "(Apple Music Edition)",
      "(Apple Music Live)",
      "- Apple Music",
      
      // Amazon
      "(Amazon Original)",
      "- Amazon Music",
      
      // Deezer
      "(Deezer Session)",
      "- Deezer",
      
      // Tidal
      "(TIDAL Rising)",
      "- TIDAL",
      
      // SoundCloud
      "(SoundCloud Go+)",
      
      // YouTube Music
      "(YouTube Music Sessions)",
      "- YouTube Music"
   )
   
   /**
    * Indicadores de calidad de audio/video
    */
   private val QUALITY_INDICATORS = listOf(
      "HD", "HQ", "4K", "8K", "1080p", "720p", "480p", "360p",
      "High Quality", "High Definition",
      "Remastered", "Remaster",
      "320kbps", "FLAC", "Lossless",
      "Dolby Atmos", "Spatial Audio",
      "Hi-Res", "Hi-Fi"
   )
   
   /**
    * Patrones para detectar featured artists
    */
   private val FEATURED_PATTERNS = listOf(
      // Patrones con paréntesis/corchetes
      """\(feat\.?\s+([^)]+)\)""",
      """\(ft\.?\s+([^)]+)\)""",
      """\(featuring\s+([^)]+)\)""",
      """\(with\s+([^)]+)\)""",
      """\(con\s+([^)]+)\)""",  // Español
      """\[feat\.?\s+([^\]]+)\]""",
      """\[ft\.?\s+([^\]]+)\]""",
      """\[featuring\s+([^\]]+)\]""",
      
      // Patrones sin paréntesis (más riesgosos, al final)
      """\s+feat\.?\s+(.+?)(?:\s*[-–—]|\s*\(|\s*\[|$)""",
      """\s+ft\.?\s+(.+?)(?:\s*[-–—]|\s*\(|\s*\[|$)""",
      """\s+featuring\s+(.+?)(?:\s*[-–—]|\s*\(|\s*\[|$)"""
   ).map { it.toRegex(RegexOption.IGNORE_CASE) }
   
   /**
    * Patrones para separar múltiples artistas
    */
   private val ARTIST_SEPARATORS = listOf(
      " & ",
      " and ",
      " y ",      // Español
      " e ",      // Portugués
      " x ",      // Colaboración moderna
      " X ",
      ", ",
      " vs ",
      " vs. ",
      " / "
   )
   
   /**
    * Indicadores de contenido NO musical (con pesos)
    */
   private val NON_MUSIC_INDICATORS = mapOf(
      // ALTO PESO (definitivamente no música)
      "podcast" to 1.0,
      "episode" to 0.9,
      "audiobook" to 1.0,
      "audiolibro" to 1.0,
      "documentary" to 0.9,
      "documental" to 0.9,
      
      // PESO MEDIO
      "interview" to 0.6,
      "entrevista" to 0.6,
      "lecture" to 0.7,
      "conferencia" to 0.7,
      "speech" to 0.6,
      "discurso" to 0.6,
      "ted talk" to 0.8,
      "behind the scenes" to 0.5,
      "making of" to 0.5,
      "trailer" to 0.7,
      "teaser" to 0.6,
      
      // PESO BAJO (podría ser música)
      "tutorial" to 0.4,
      "how to" to 0.4,
      "guide" to 0.3,
      "review" to 0.4,
      "reaction" to 0.5,
      "analysis" to 0.4,
      "commentary" to 0.4,
      
      // Indicadores de duración larga
      "hour mix" to 0.3,
      "hours mix" to 0.3,
      "full album" to 0.2,  // Podría ser música
      
      // IMAX y cine
      "imax" to 0.7,
      "cinema" to 0.5,
      "theater" to 0.4
   )
   
   /**
    * Indicadores de contenido MUSICAL (bonificaciones)
    */
   private val MUSIC_INDICATORS = mapOf(
      "official" to 0.15,
      "oficial" to 0.15,
      "audio" to 0.10,
      "music" to 0.10,
      "música" to 0.10,
      "song" to 0.10,
      "canción" to 0.10,
      "single" to 0.15,
      "album" to 0.10,
      "álbum" to 0.10,
      "track" to 0.10,
      "remix" to 0.20,
      "cover" to 0.15,
      "acoustic" to 0.15,
      "acústico" to 0.15,
      "live" to 0.10,
      "en vivo" to 0.10,
      "unplugged" to 0.15,
      "session" to 0.10,
      "version" to 0.10,
      "versión" to 0.10
   )
   
   /**
    * Palabras menores para capitalización (no capitalizar)
    */
   private val MINOR_WORDS = setOf(
      // Inglés
      "a", "an", "the", "and", "but", "or", "nor", "for", "yet", "so",
      "at", "by", "in", "of", "on", "to", "up", "as", "if", "vs",
      // Español
      "el", "la", "los", "las", "un", "una", "unos", "unas",
      "de", "del", "al", "en", "con", "por", "para", "sin",
      "y", "o", "ni", "que", "como",
      // Portugués
      "o", "os", "as", "um", "uma", "do", "da", "dos", "das",
      "e", "ou", "em", "com", "para", "sem"
   )
   
   /**
    * Palabras que SIEMPRE van en mayúsculas
    */
   private val ALWAYS_UPPERCASE = setOf(
      "DJ", "MC", "TV", "CD", "DVD", "EP", "LP", "UK", "US", "USA",
      "NYC", "LA", "BBC", "MTV", "VIP", "EDM", "R&B", "AC/DC",
      "OK", "FM", "AM", "HD", "HQ", "AI", "II", "III", "IV", "VI",
      "VII", "VIII", "IX", "XI", "XII", "XXX", "vs"
   )
   
   // ==================== FUNCIONES PRINCIPALES ====================
   
   /**
    * Limpia un título de canción eliminando patrones comunes
    * @param title Título original
    * @param applyTitleCase Si aplicar capitalización inteligente
    * @return Título limpio
    */
   fun cleanTitle(title: String, applyTitleCase: Boolean = false): String {
      var clean = title.trim()
      
      // 1. Eliminar patrones de YouTube
      YOUTUBE_PATTERNS.forEach { pattern ->
         clean = clean.replace(pattern, "", ignoreCase = true)
      }
      
      // 2. Eliminar patrones de streaming
      STREAMING_PATTERNS.forEach { pattern ->
         clean = clean.replace(pattern, "", ignoreCase = true)
      }
      
      // 3. Eliminar indicadores de calidad
      QUALITY_INDICATORS.forEach { indicator ->
         // Solo eliminar si está entre paréntesis/corchetes o al final
         clean = clean.replace("($indicator)", "", ignoreCase = true)
         clean = clean.replace("[$indicator]", "", ignoreCase = true)
         clean = clean.replace(Regex("\\s+$indicator\\s*$", RegexOption.IGNORE_CASE), "")
      }
      
      // 4. Limpiar caracteres especiales y espacios múltiples
      clean = clean
         .replace(Regex("\\s+"), " ")
         .replace(Regex("[_]{2,}"), " ")
         .replace(Regex("[-–—]{2,}"), " - ")
         .replace(Regex("^[-–—\\s]+"), "")  // Al inicio
         .replace(Regex("[-–—\\s]+$"), "")  // Al final
         .trim()
      
      // 5. Aplicar Title Case si se solicita
      if (applyTitleCase) {
         clean = toTitleCase(clean)
      }
      
      return clean
   }
   
   /**
    * Limpia un nombre de artista eliminando sufijos comunes
    */
   fun cleanArtistName(artist: String, applyTitleCase: Boolean = false): String {
      var clean = artist.trim()
      
      // Patrones específicos de artistas
      val artistPatterns = listOf(
         "- Topic",
         " - Topic",
         "VEVO",
         " VEVO",
         "Official",
         " Official",
         "Music",
         " Music",
         "(Official)",
         "[Official]"
      )
      
      artistPatterns.forEach { pattern ->
         clean = clean.replace(pattern, "", ignoreCase = true)
      }
      
      clean = clean
         .replace(Regex("\\s+"), " ")
         .trim()
      
      if (applyTitleCase) {
         clean = toTitleCase(clean)
      }
      
      return clean
   }
   
   /**
    * Extrae featured artists del título
    * @return Lista de artistas colaboradores encontrados
    */
   fun extractFeaturedArtists(title: String): List<String> {
      val featuredArtists = mutableListOf<String>()
      
      for (pattern in FEATURED_PATTERNS) {
         pattern.findAll(title).forEach { match ->
            val artistsGroup = match.groupValues.getOrNull(1) ?: return@forEach
            
            // Separar múltiples artistas
            var artists = listOf(artistsGroup)
            for (separator in ARTIST_SEPARATORS) {
               artists = artists.flatMap { it.split(separator) }
            }
            
            artists
               .map { it.trim() }
               .filter { it.isNotBlank() && it.length >= 2 }
               .filter { !isCommonWord(it) }
               .forEach { featuredArtists.add(it) }
         }
      }
      
      return featuredArtists.distinct()
   }
   
   /**
    * Separa múltiples artistas de un string
    * "Drake & 21 Savage" -> ["Drake", "21 Savage"]
    */
   fun splitArtists(artistString: String): List<String> {
      var artists = listOf(artistString)
      
      for (separator in ARTIST_SEPARATORS) {
         artists = artists.flatMap { it.split(separator) }
      }
      
      return artists
         .map { cleanArtistName(it) }
         .filter { it.isNotBlank() && it.length >= 2 }
         .distinct()
   }
   
   /**
    * Detecta la versión/tipo de la canción
    */
   fun detectVersion(title: String): MusicVersion? {
      val lowerTitle = title.lowercase()
      
      return when {
         // Orden de prioridad (más específico primero)
         lowerTitle.contains("acoustic version") ||
               lowerTitle.contains("versión acústica") ||
               lowerTitle.contains("unplugged") -> MusicVersion.ACOUSTIC
         
         lowerTitle.contains("live") ||
               lowerTitle.contains("en vivo") ||
               lowerTitle.contains("en directo") ||
               lowerTitle.contains("concert") -> MusicVersion.LIVE
         
         lowerTitle.contains("remix") ||
               lowerTitle.contains("rmx") -> MusicVersion.REMIX
         
         lowerTitle.contains("cover") ||
               lowerTitle.contains("versión") && !lowerTitle.contains("original") -> MusicVersion.COVER
         
         lowerTitle.contains("instrumental") ||
               lowerTitle.contains("inst.") -> MusicVersion.INSTRUMENTAL
         
         lowerTitle.contains("karaoke") -> MusicVersion.KARAOKE
         
         lowerTitle.contains("demo") -> MusicVersion.DEMO
         
         lowerTitle.contains("radio edit") ||
               lowerTitle.contains("radio version") -> MusicVersion.RADIO_EDIT
         
         lowerTitle.contains("extended") ||
               lowerTitle.contains("extended mix") -> MusicVersion.EXTENDED
         
         lowerTitle.contains("slowed") ||
               lowerTitle.contains("reverb") -> MusicVersion.SLOWED_REVERB
         
         lowerTitle.contains("sped up") ||
               lowerTitle.contains("nightcore") -> MusicVersion.SPED_UP
         
         else -> null
      }
   }
   
   /**
    * Extrae año del título si está presente
    * Busca patrones como (2023), [2023], 2023
    */
   fun extractYear(title: String): Int? {
      // Patrones de año con paréntesis/corchetes primero (más confiables)
      val patterns = listOf(
         """\((\d{4})\)""".toRegex(),
         """\[(\d{4})\]""".toRegex(),
         """\b(19\d{2}|20[0-2]\d)\b""".toRegex()  // 1900-2029
      )
      
      for (pattern in patterns) {
         val match = pattern.find(title)
         match?.groupValues?.get(1)?.toIntOrNull()?.let { year ->
            if (year in 1900..2100) return year
         }
      }
      
      return null
   }
   
   // ==================== DETECCIÓN DE CONTENIDO MUSICAL ====================
   
   /**
    * Verifica si el contenido es musical
    * @param threshold Umbral mínimo de confianza (default: 0.6)
    * @return true si la confianza musical >= threshold
    */
   fun isMusicalContent(
      title: String,
      artist: String? = null,
      threshold: Double = 0.6
   ): Boolean {
      return calculateMusicConfidence(title, artist) >= threshold
   }
   
   /**
    * Calcula confianza de que es contenido musical (0.0 a 1.0)
    *
    * Algoritmo:
    * - Base: 0.5 (neutral)
    * - Penaliza por indicadores no musicales
    * - Bonifica por indicadores musicales
    * - Ajusta por duración implícita
    */
   fun calculateMusicConfidence(title: String, artist: String? = null): Double {
      var confidence = 0.5
      val fullText = normalizeForAnalysis("$title ${artist ?: ""}")
      
      // Penalizar por indicadores no musicales
      for ((indicator, weight) in NON_MUSIC_INDICATORS) {
         if (fullText.contains(indicator.lowercase())) {
            confidence -= weight * 0.5  // Escalar el peso
         }
      }
      
      // Bonificar por indicadores musicales
      for ((indicator, weight) in MUSIC_INDICATORS) {
         if (fullText.contains(indicator.lowercase())) {
            confidence += weight
         }
      }
      
      // Penalizar por patrones de duración larga en título
      val durationPattern = """(\d+)\s*(hour|hr|hora)""".toRegex(RegexOption.IGNORE_CASE)
      if (durationPattern.containsMatchIn(title)) {
         confidence -= 0.3
      }
      
      // Penalizar por patrones de episodio/capítulo
      val episodePattern = """(episode|ep\.?|chapter|cap\.?)\s*\d+""".toRegex(RegexOption.IGNORE_CASE)
      if (episodePattern.containsMatchIn(title)) {
         confidence -= 0.4
      }
      
      return confidence.coerceIn(0.0, 1.0)
   }
   
   // ==================== CAPITALIZACIÓN INTELIGENTE ====================
   
   /**
    * Convierte texto a Title Case inteligente
    * Respeta palabras menores, acrónimos y casos especiales
    */
   fun toTitleCase(text: String): String {
      if (text.isBlank()) return text
      
      val words = text.split(Regex("\\s+"))
      
      return words.mapIndexed { index, word ->
         when {
            // Palabra vacía
            word.isBlank() -> word
            
            // Siempre mayúsculas (acrónimos)
            ALWAYS_UPPERCASE.any { it.equals(word, ignoreCase = true) } -> {
               ALWAYS_UPPERCASE.first { it.equals(word, ignoreCase = true) }
            }
            
            // Primera o última palabra: siempre capitalizar
            index == 0 || index == words.lastIndex -> capitalizeWord(word)
            
            // Palabras menores: mantener en minúsculas
            MINOR_WORDS.contains(word.lowercase()) -> word.lowercase()
            
            // Palabra normal: capitalizar
            else -> capitalizeWord(word)
         }
      }.joinToString(" ")
   }
   
   /**
    * Capitaliza una palabra respetando caracteres especiales
    */
   private fun capitalizeWord(word: String): String {
      if (word.isEmpty()) return word
      
      // Manejar palabras con apóstrofes (don't -> Don't)
      if (word.contains("'")) {
         val parts = word.split("'")
         return parts.mapIndexed { i, part ->
            if (i == 0) part.replaceFirstChar { it.uppercase() }
            else part.lowercase()
         }.joinToString("'")
      }
      
      // Manejar palabras con guiones (self-made -> Self-Made)
      if (word.contains("-")) {
         return word.split("-").joinToString("-") {
            it.replaceFirstChar { c -> c.uppercase() }
         }
      }
      
      return word.replaceFirstChar { it.uppercase() }
   }
   
   // ==================== NORMALIZACIÓN ====================
   
   /**
    * Normaliza texto eliminando acentos usando Unicode NFD
    */
   fun normalizeText(text: String): String {
      return Normalizer.normalize(text, Normalizer.Form.NFD)
         .replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "")
         .lowercase()
   }
   
   /**
    * Normaliza texto para análisis (lowercase + sin acentos)
    */
   private fun normalizeForAnalysis(text: String): String {
      return normalizeText(text)
         .replace(Regex("[^a-z0-9\\s]"), " ")
         .replace(Regex("\\s+"), " ")
         .trim()
   }
   
   /**
    * Limpia texto para búsqueda
    */
   fun cleanForSearch(text: String): String {
      return normalizeText(text)
         .replace(Regex("[^a-z0-9\\s]"), " ")
         .replace(Regex("\\s+"), " ")
         .trim()
   }
   
   /**
    * Construye query de búsqueda optimizada
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
   
   // ==================== PARSING ESTRUCTURADO ====================
   
   /**
    * Divide título en componentes estructurados
    */
   fun parseTitle(title: String): ParsedTitle {
      val featuredArtists = extractFeaturedArtists(title)
      val version = detectVersion(title)
      val year = extractYear(title)
      
      // Limpiar título removiendo featured artists
      var mainTitle = title
      
      // Remover patrones de featured artists
      FEATURED_PATTERNS.forEach { pattern ->
         mainTitle = pattern.replace(mainTitle, "")
      }
      
      // Limpiar el resultado
      mainTitle = cleanTitle(mainTitle)
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
    * Preprocesa búsqueda completa
    */
   fun preprocessSearch(title: String, artist: String?): Pair<String, String> {
      val cleanedTitle = cleanTitle(title)
      val cleanedArtist = artist?.let { cleanArtistName(it) } ?: ""
      return cleanedTitle to cleanedArtist
   }
   
   // ==================== VALIDACIÓN ====================
   
   /**
    * Valida si un título es válido
    */
   fun isValidTitle(
      title: String,
      minLength: Int = 1,
      maxLength: Int = 200
   ): Boolean {
      val cleaned = cleanTitle(title)
      return cleaned.length in minLength..maxLength && cleaned.isNotBlank()
   }
   
   /**
    * Detecta si es un título estilo YouTube
    */
   fun isYouTubeStyleTitle(title: String): Boolean {
      return YOUTUBE_PATTERNS.any { pattern ->
         title.contains(pattern, ignoreCase = true)
      }
   }
   
   // ==================== UTILIDADES PRIVADAS ====================
   
   private fun isCommonWord(word: String): Boolean {
      val lower = word.lowercase()
      return MINOR_WORDS.contains(lower) ||
            lower.length < 2 ||
            lower.all { it.isDigit() }
   }
   
   // ==================== DATA CLASSES Y ENUMS ====================
   
   enum class MusicVersion {
      REMIX,
      COVER,
      ACOUSTIC,
      LIVE,
      INSTRUMENTAL,
      KARAOKE,
      DEMO,
      RADIO_EDIT,
      EXTENDED,
      SLOWED_REVERB,
      SPED_UP;
      
      fun toDisplayString(): String = name.lowercase()
         .replace("_", " ")
         .split(" ")
         .joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
   }
   
   data class ParsedTitle(
      val mainTitle: String,
      val featuredArtists: List<String>,
      val version: MusicVersion?,
      val year: Int?,
      val originalTitle: String
   ) {
      /**
       * Reconstruye el título con formato limpio y estandarizado
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
            append(it.toDisplayString())
            append("]")
         }
      }
      
      /**
       * Genera string para búsqueda (sin decoradores)
       */
      fun toSearchString(): String = buildString {
         append(mainTitle)
         if (featuredArtists.isNotEmpty()) {
            append(" ")
            append(featuredArtists.joinToString(" "))
         }
      }
   }
}