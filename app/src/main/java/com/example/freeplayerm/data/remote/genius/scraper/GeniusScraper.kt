// app/src/main/java/com/example/freeplayerm/data/remote/genius/scraper/GeniusScraper.kt
package com.example.freeplayerm.data.remote.genius.scraper

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 🕷️ GENIUS WEB SCRAPER
 *
 * Scraper especializado para extraer letras de canciones de Genius.com Nota: La API de Genius NO
 * proporciona letras, solo metadata
 *
 * Características:
 * - ✅ Extracción robusta de letras con múltiples selectores fallback
 * - ✅ Limpieza automática de ads y elementos no deseados
 * - ✅ User-Agent rotation para evitar rate limiting
 * - ✅ Validación de páginas de canciones vs discografías
 * - ✅ Extracción opcional de metadata complementaria
 */
@Singleton
class GeniusScraper @Inject constructor(private val okHttpClient: OkHttpClient) {
    private val tag = "GeniusScraper"

    companion object {
        // User agents para rotación
        private val USER_AGENTS =
            listOf(
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
                "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
                "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
            )

        // Selectores CSS para letras (en orden de prioridad)
        private const val SELECTOR_LYRICS_CONTAINER = "div[data-lyrics-container=true]"
        private const val SELECTOR_LYRICS = "div.Lyrics__Container"
        private const val SELECTOR_LYRICS_FALLBACK = "div[class*=Lyrics], div[class*=lyrics]"

        // Selectores CSS para metadata
        private const val SELECTOR_COVER_ART = "img.cover_art-image"
        private const val SELECTOR_SONG_HEADER = "div.SongHeader__Container"
        private const val SELECTOR_SONG_TITLE = "h1.SongHeader__Title"
        private const val SELECTOR_ARTIST_NAME = "a.SongHeader__Artist"
        private const val SELECTOR_ALBUM_INFO = "div.MetadataStats a[href*='/albums/']"
        private const val SELECTOR_RELEASE_DATE = "span.MetadataStats__Value"

        // Elementos a eliminar
        private val ELEMENTS_TO_REMOVE =
            """
            div[class*='Advertisement'],
            div[class*='Ad'],
            div[class*='LyricsPlaceholder'],
            div[class*='LyricsHeader'],
            .embed,
            script,
            style
            """
                .trimIndent()
    }

    /** Datos scrapeados de una página de canción */
    data class ScrapedSongData(
        val lyrics: String? = null,
        val coverArtUrl: String? = null,
        val songTitle: String? = null,
        val artistName: String? = null,
        val albumName: String? = null,
        val releaseDate: String? = null,
        val featuredArtists: List<String> = emptyList(),
        val producers: List<String> = emptyList(),
        val writers: List<String> = emptyList(),
    )

    /**
     * Extrae datos completos de una página de canción
     *
     * @param songUrl URL completa de la canción en Genius
     * @return Datos scrapeados o null si falla
     */
    suspend fun extractCompleteSongData(songUrl: String): ScrapedSongData? =
        withContext(Dispatchers.IO) {
            Log.d(tag, "🎯 Scraping: $songUrl")

            try {
                val document = fetchDocumentWithOkHttp(songUrl) ?: return@withContext null

                return@withContext ScrapedSongData(
                    lyrics = extractLyricsImproved(document),
                    coverArtUrl = extractCoverArtUrl(document),
                    songTitle = extractSongTitle(document),
                    artistName = extractArtistName(document),
                    albumName = extractAlbumName(document),
                    releaseDate = extractReleaseDate(document),
                    featuredArtists = extractFeaturedArtists(document),
                    producers = extractProducers(document),
                    writers = extractWriters(document),
                )
            } catch (e: Exception) {
                Log.e(tag, "❌ Error en scraping: ${e.message}", e)
                null
            }
        }

    /** Extrae solo las letras de una canción (optimizado) */
    suspend fun extractLyricsOnly(songUrl: String): String? =
        withContext(Dispatchers.IO) {
            try {
                val document = fetchDocumentWithOkHttp(songUrl) ?: return@withContext null
                extractLyricsImproved(document)
            } catch (e: Exception) {
                Log.e(tag, "❌ Error extrayendo letras: ${e.message}", e)
                null
            }
        }

    // ==================== EXTRACCIÓN DE LETRAS ====================

    private fun extractLyricsImproved(document: Document): String? {
        Log.d(tag, "📝 Extrayendo letras...")

        // Intentar selectores en orden de prioridad
        var lyricsContainer =
            document.selectFirst(SELECTOR_LYRICS_CONTAINER)
                ?: document.selectFirst(SELECTOR_LYRICS)
                ?: document.selectFirst(SELECTOR_LYRICS_FALLBACK)

        if (lyricsContainer == null) {
            Log.w(tag, "No se encontró contenedor de letras")
            return null
        }

        // Limpiar elementos no deseados
        lyricsContainer.select(ELEMENTS_TO_REMOVE).remove()

        return buildLyricsText(lyricsContainer)
    }

    private fun buildLyricsText(container: Element): String {
        val lyricsBuilder = StringBuilder()

        container.children().forEach { verse ->
            verse.children().forEach { line ->
                line.children().forEach { element ->
                    when (element.tagName()) {
                        "br" -> lyricsBuilder.append("\n")
                        else -> {
                            val text = element.text().trim()
                            if (text.isNotEmpty()) {
                                lyricsBuilder.append(text).append("\n")
                            }
                        }
                    }
                }

                val lineText = line.ownText().trim()
                if (lineText.isNotEmpty()) {
                    lyricsBuilder.append(lineText).append("\n")
                }
            }

            lyricsBuilder.append("\n")
        }

        return cleanLyricsText(lyricsBuilder.toString())
    }

    private fun cleanLyricsText(rawText: String): String {
        return rawText
            .replace(Regex("\\[.*?\\]"), "") // Eliminar anotaciones [Verse 1], etc.
            .replace(Regex("\\s+"), " ") // Normalizar espacios
            .replace(Regex("\n{3,}"), "\n\n") // Max 2 saltos de línea
            .trim()
    }

    // ==================== EXTRACCIÓN DE METADATA ====================

    private fun extractCoverArtUrl(document: Document): String? {
        Log.d(tag, "🖼️ Extrayendo portada...")

        // Intentar selector directo
        var imgElement = document.selectFirst(SELECTOR_COVER_ART)

        // Fallback: meta tag Open Graph
        if (imgElement == null) {
            val metaOg = document.selectFirst("meta[property='og:image']")
            return metaOg?.attr("content")?.takeIf { it.isNotBlank() }
        }

        // Fallback: imagen en header

        return imgElement.attr("src").takeIf { it.isNotBlank() }
    }

    private fun extractSongTitle(document: Document): String? {
        return document.selectFirst(SELECTOR_SONG_TITLE)?.text()
            ?: document.selectFirst("h1")?.text()
    }

    private fun extractArtistName(document: Document): String? {
        return document.selectFirst(SELECTOR_ARTIST_NAME)?.text()
            ?: document.selectFirst("a[href*=/artists/]")?.text()
    }

    private fun extractAlbumName(document: Document): String? {
        return document.selectFirst(SELECTOR_ALBUM_INFO)?.text()
            ?: document.selectFirst("a[href*=/albums/]")?.text()
    }

    private fun extractReleaseDate(document: Document): String? {
        return document.selectFirst(SELECTOR_RELEASE_DATE)?.text()
            ?: document.selectFirst("span[class*='Date']")?.text()
    }

    private fun extractFeaturedArtists(document: Document): List<String> {
        val featuredSection = document.select("div:contains(Featuring)")
        return featuredSection
            .flatMap { it.select("a[href*=/artists/]") }
            .mapNotNull { it.text().trim() }
            .filter { it.isNotBlank() }
    }

    private fun extractProducers(document: Document): List<String> {
        val producerSection =
            document.select(
                """
                div:contains(Producer),
                div:contains(Producers),
                div:contains(Produced)
                """
                    .trimIndent()
            )

        return extractCreditsFromSection(producerSection)
    }

    private fun extractWriters(document: Document): List<String> {
        val writerSection =
            document.select(
                """
                div:contains(Writer),
                div:contains(Writers),
                div:contains(Written)
                """
                    .trimIndent()
            )

        return extractCreditsFromSection(writerSection)
    }

    private fun extractCreditsFromSection(section: org.jsoup.select.Elements): List<String> {
        return section
            .flatMap { it.select("a[href*=/artists/]") }
            .mapNotNull { it.text().trim() }
            .filter { it.isNotBlank() }
            .distinct()
    }

    // ==================== FETCHING ====================

    private suspend fun fetchDocumentWithOkHttp(url: String): Document? =
        withContext(Dispatchers.IO) {
            try {
                val userAgent = USER_AGENTS.random()
                val request =
                    Request.Builder()
                        .url(url)
                        .header("User-Agent", userAgent)
                        .header(
                            "Accept",
                            "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8",
                        )
                        .header("Accept-Language", "en-US,en;q=0.5")
                        .header("Connection", "keep-alive")
                        .build()

                val response = okHttpClient.newCall(request).execute()

                if (!response.isSuccessful) {
                    Log.w(tag, "Request failed: ${response.code}")
                    return@withContext null
                }

                val html = response.body.string()
                response.close()

                val document = Jsoup.parse(html, url)

                // Validar que es una página de canción
                if (!isValidSongPage(document)) {
                    Log.w(tag, "Página inválida: ${document.title()}")
                    return@withContext null
                }

                document
            } catch (e: IOException) {
                Log.e(tag, "❌ Error de red: ${e.message}")
                null
            } catch (e: Exception) {
                Log.e(tag, "❌ Error inesperado: ${e.message}", e)
                null
            }
        }

    /** Valida que la página sea de una canción y no discografía/álbum */
    private fun isValidSongPage(document: Document): Boolean {
        val title = document.title()
        val hasLyrics = document.select("$SELECTOR_LYRICS_CONTAINER, $SELECTOR_LYRICS").isNotEmpty()
        val isDiscographyPage =
            title.contains("discography", ignoreCase = true) ||
                title.contains("albums", ignoreCase = true)

        return hasLyrics && !isDiscographyPage
    }
}
