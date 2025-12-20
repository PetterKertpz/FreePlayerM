// app/src/main/java/com/example/freeplayerm/data/remote/GeniusScraper.kt
package com.example.freeplayerm.data.remote

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeniusScraper @Inject constructor(
    private val okHttpClient: OkHttpClient
) {

    private val TAG = "GeniusScraper"

    companion object {
        private val USER_AGENTS = listOf(
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
            "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
        )

        private const val SELECTOR_LYRICS_CONTAINER = "div[data-lyrics-container=true]"
        private const val SELECTOR_LYRICS = "div.Lyrics__Container"
        private const val SELECTOR_SONG_HEADER = "div[data-testid=song-header]"
        private const val SELECTOR_COVER_ART = "div[data-testid=cover-art] img, div.cover_art img"
        private const val SELECTOR_SONG_TITLE = "h1[data-testid=song-title]"
        private const val SELECTOR_ARTIST_NAME = "a[data-testid=artist-name]"
        private const val SELECTOR_ALBUM_INFO = "a[href*=/albums/], div.SongHeader__Album"
        private const val SELECTOR_RELEASE_DATE = "span.ReleaseDate"
        private const val SELECTOR_SONG_METADATA = "div.SongHeader__Metadata"
    }

    data class ScrapedSongData(
        val lyrics: String? = null,
        val coverArtUrl: String? = null,
        val songTitle: String? = null,
        val artistName: String? = null,
        val albumName: String? = null,
        val releaseDate: String? = null,
        val featuredArtists: List<String> = emptyList(),
        val producers: List<String> = emptyList(),
        val writers: List<String> = emptyList()
    )

    suspend fun extractCompleteSongData(songUrl: String): ScrapedSongData? =
        withContext(Dispatchers.IO) {
            Log.d(TAG, "🎯 Iniciando scraping completo para: $songUrl")

            try {
                val document = fetchDocument(songUrl) ?: return@withContext null

                return@withContext ScrapedSongData(
                    lyrics = extractLyricsImproved(document),
                    coverArtUrl = extractCoverArtUrl(document),
                    songTitle = extractSongTitle(document),
                    artistName = extractArtistName(document),
                    albumName = extractAlbumName(document),
                    releaseDate = extractReleaseDate(document),
                    featuredArtists = extractFeaturedArtists(document),
                    producers = extractProducers(document),
                    writers = extractWriters(document)
                )

            } catch (e: Exception) {
                Log.e(TAG, "❌ Error en scraping completo: ${e.message}", e)
                null
            }
        }

    private fun extractLyricsImproved(document: Document): String? {
        Log.d(TAG, "📝 Extrayendo letras...")

        var lyricsContainer = document.selectFirst(SELECTOR_LYRICS_CONTAINER)

        if (lyricsContainer == null) {
            lyricsContainer = document.selectFirst(SELECTOR_LYRICS)
        }

        if (lyricsContainer == null) {
            lyricsContainer = document.selectFirst("div[class*=Lyrics], div[class*=lyrics]")
        }

        if (lyricsContainer == null) {
            Log.w(TAG, "No se encontró contenedor de letras")
            return null
        }

        lyricsContainer.select("""
            div[class*='Advertisement'],
            div[class*='Ad'],
            div[class*='LyricsPlaceholder'],
            div[class*='LyricsHeader'],
            .embed,
            script,
            style
        """.trimIndent()).remove()

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
            .replace(Regex("\\[.*?\\]"), "")
            .replace(Regex("\\s+"), " ")
            .replace(Regex("\n{3,}"), "\n\n")
            .replace(Regex("^\\s+"), "")
            .replace(Regex("\\s+$"), "")
            .trim()
    }

    private fun extractCoverArtUrl(document: Document): String? {
        Log.d(TAG, "🖼️ Extrayendo portada...")

        var imgElement = document.selectFirst(SELECTOR_COVER_ART)

        if (imgElement == null) {
            imgElement = document.selectFirst("meta[property='og:image']")
            return imgElement?.attr("content")?.takeIf { it.isNotBlank() }
        }

        if (imgElement == null) {
            val header = document.selectFirst(SELECTOR_SONG_HEADER)
            imgElement = header?.selectFirst("img")
        }

        return imgElement?.attr("src")?.takeIf { it.isNotBlank() }
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
            ?: document.selectFirst("a[href*=/album/]")?.text()
    }

    private fun extractReleaseDate(document: Document): String? {
        return document.selectFirst(SELECTOR_RELEASE_DATE)?.text()
            ?: document.selectFirst("span[class*='Date']")?.text()
    }

    private fun extractFeaturedArtists(document: Document): List<String> {
        val featuredSection = document.select("div:contains(Featuring)")
        return featuredSection.flatMap { it.select("a[href*=/artists/]") }
            .mapNotNull { it.text().trim() }
            .filter { it.isNotBlank() }
    }

    private fun extractProducers(document: Document): List<String> {
        val producerSection = document.select("""
            div:contains(Producer),
            div:contains(Producers),
            div:contains(Produced)
        """.trimIndent())

        return extractCreditsFromSection(producerSection)
    }

    private fun extractWriters(document: Document): List<String> {
        val writerSection = document.select("""
            div:contains(Writer),
            div:contains(Writers),
            div:contains(Written)
        """.trimIndent())

        return extractCreditsFromSection(writerSection)
    }

    private fun extractCreditsFromSection(section: org.jsoup.select.Elements): List<String> {
        return section.flatMap { it.select("a[href*=/artists/]") }
            .mapNotNull { it.text().trim() }
            .filter { it.isNotBlank() }
            .distinct()
    }

    private suspend fun fetchDocument(url: String): Document? =
        withContext(Dispatchers.IO) {
            try {
                val userAgent = USER_AGENTS.random()
                Log.d(TAG, "🌐 Descargando página con User-Agent: ${userAgent.take(50)}...")

                Jsoup.connect(url)
                    .userAgent(userAgent)
                    .timeout(15000)
                    .followRedirects(true)
                    .ignoreHttpErrors(true)
                    .ignoreContentType(true)
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                    .header("Accept-Language", "en-US,en;q=0.5")
                    .header("Accept-Encoding", "gzip, deflate")
                    .header("Connection", "keep-alive")
                    .get()
                    .takeIf { doc ->
                        val title = doc.title()
                        val isValid = !title.contains("discography", ignoreCase = true) &&
                                !title.contains("album", ignoreCase = true) &&
                                doc.select("div[data-lyrics-container=true], div.Lyrics__Container").isNotEmpty()

                        if (!isValid) {
                            Log.w(TAG, "Página no válida o no es una canción: $title")
                        }
                        isValid
                    }

            } catch (e: IOException) {
                Log.e(TAG, "❌ Error de red al descargar página: ${e.message}")
                null
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error inesperado al descargar página: ${e.message}", e)
                null
            }
        }
}