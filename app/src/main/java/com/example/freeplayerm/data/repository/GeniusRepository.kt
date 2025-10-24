// app/src/main/java/com/example/freeplayerm/data/repository/GeniusRepository.kt
package com.example.freeplayerm.data.repository

import android.content.Context
import android.util.Log
import com.example.freeplayerm.com.example.freeplayerm.data.local.entity.CancionEntity
import com.example.freeplayerm.com.example.freeplayerm.data.local.entity.LetraEntity
import com.example.freeplayerm.data.local.dao.CancionDao
import com.example.freeplayerm.data.local.dao.LetraDao
import com.example.freeplayerm.data.local.entity.relations.CancionConArtista
import com.example.freeplayerm.data.remote.GeniusApiService
import com.example.freeplayerm.data.remote.GeniusScraper
import com.example.freeplayerm.data.remote.GeniusServiceOptimizado
import com.example.freeplayerm.data.remote.dto.GeniusSearchResponse
import com.example.freeplayerm.data.remote.dto.SongResult
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import org.jsoup.nodes.TextNode
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.concurrent.locks.ReentrantLock
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.concurrent.withLock

@Singleton
class GeniusRepository @Inject constructor(
    private val apiService: GeniusApiService,
    private val cancionDao: CancionDao,
    private val letraDao: LetraDao,
    private val okHttpClient: OkHttpClient,
    private val geniusServiceOptimizado: GeniusServiceOptimizado,
    private val geniusScraper: GeniusScraper,
    @ApplicationContext private val context: Context
) {

    private val tag = "GeniusRepository"
    // === NUEVAS FUNCIONES DE UTILIDAD MEJORADAS ===
    private var ultimaBusqueda = 0L
    private val intervaloMinimo = 2500L // 2.5 segundos entre búsquedas

    private fun preprocesarBusqueda(titulo: String, artista: String?): Pair<String, String> {
        var tituloLimpio = titulo
            .replace("- Topic", "", ignoreCase = true)
            .replace("(Official Video)", "", ignoreCase = true)
            .replace("(Official Audio)", "", ignoreCase = true)
            .replace("\\[.*]".toRegex(), "") // Remove brackets content
            .replace("\\(.*\\)".toRegex(), "") // Remove parentheses content
            .replace("ft\\.|feat\\.|featuring".toRegex(RegexOption.IGNORE_CASE), "")
            .trim()

        var artistaLimpio = (artista ?: "")
            .replace("- Topic", "", ignoreCase = true)
            .replace("VEVO", "", ignoreCase = true)
            .replace("Official", "", ignoreCase = true)
            .trim()

        // Detectar y eliminar contenido que no es música
        val patronesNoMusica = listOf(
            "minute.*imax", "trailer", "interview", "behind the scenes",
            "making of", "documentary", "lecture", "speech", "podcast",
            "episode", "chapter", "part \\d".toRegex()
        )

        patronesNoMusica.forEach { patron ->
            when (patron) {
                is String -> {
                    if (tituloLimpio.contains(patron, ignoreCase = true)) {
                        Log.d(tag, "⏩ Saltando contenido no musical: '$titulo'")
                        tituloLimpio = ""
                    }
                }
                is Regex -> {
                    if (patron.containsMatchIn(tituloLimpio.lowercase())) {
                        Log.d(tag, "⏩ Saltando contenido no musical (regex): '$titulo'")
                        tituloLimpio = ""
                    }
                }
            }
        }

        return tituloLimpio to artistaLimpio
    }

    private fun esContenidoMusical(titulo: String, artista: String?): Boolean {
        val textoCompleto = "$titulo ${artista ?: ""}".lowercase()

        val indicadoresNoMusicales = listOf(
            "minute", "hour", "documentary", "interview", "behind the scenes",
            "making of", "trailer", "lecture", "speech", "podcast", "episode",
            "chapter", "part \\d".toRegex(), "\\d+ minutes".toRegex()
        )

        return indicadoresNoMusicales.none { indicador ->
            when (indicador) {
                is String -> textoCompleto.contains(indicador, ignoreCase = true)
                is Regex -> indicador.containsMatchIn(textoCompleto)
                else -> false
            }
        }
    }

    private fun calcularSimilitudCadena(str1: String, str2: String): Double {
        if (str1.isEmpty() || str2.isEmpty()) return 0.0

        val str1Clean = str1.lowercase().replace("[^a-z0-9]".toRegex(), "")
        val str2Clean = str2.lowercase().replace("[^a-z0-9]".toRegex(), "")

        if (str1Clean.isEmpty() || str2Clean.isEmpty()) return 0.0

        val maxLength = maxOf(str1Clean.length, str2Clean.length)
        val distance = levenshteinDistance(str1Clean, str2Clean)

        return 1.0 - (distance.toDouble() / maxLength)
    }

    private fun levenshteinDistance(str1: String, str2: String): Int {
        val dp = Array(str1.length + 1) { IntArray(str2.length + 1) }

        for (i in 0..str1.length) {
            for (j in 0..str2.length) {
                when {
                    i == 0 -> dp[i][j] = j
                    j == 0 -> dp[i][j] = i
                    else -> {
                        dp[i][j] = minOf(
                            dp[i - 1][j - 1] + if (str1[i - 1] == str2[j - 1]) 0 else 1,
                            dp[i - 1][j] + 1,
                            dp[i][j - 1] + 1
                        )
                    }
                }
            }
        }

        return dp[str1.length][str2.length]
    }

    private fun esResultadoValidoMejorado(resultado: SongResult, titulo: String, artista: String?): Boolean {
        val tituloResultado = resultado.title
        val artistaResultado = resultado.primary_artist?.name ?: ""

        if (tituloResultado.isEmpty()) {
            return false
        }

        // Excluir tipos de contenido no deseados (lista expandida)
        val exclusiones = listOf(
            "discography", "album", "collection", "calendar", "review",
            "translation", "traducción", "cover", "mix", "remix", "annotated",
            "interview", "unreleased", "demo", "preview", "snippet"
        )

        if (exclusiones.any { tituloResultado.contains(it, ignoreCase = true) }) {
            Log.d(tag, "   ❌ Resultado rechazado por palabra clave: '${resultado.title}'")
            return false
        }

        // Verificar similitud del título (más estricto)
        val similitudTitulo = calcularSimilitudCadena(tituloResultado, titulo)
        if (similitudTitulo < 0.4) { // Aumentado de 0.3 a 0.4
            Log.d(tag, "   ❌ Similitud de título muy baja: ${"%.2f".format(similitudTitulo)} ('$tituloResultado' vs '$titulo')")
            return false
        }

        // Verificar similitud del artista (cuando está disponible)
        artista?.let { artistaOriginal ->
            if (artistaResultado.isNotEmpty()) {
                val similitudArtista = calcularSimilitudCadena(artistaResultado, artistaOriginal)
                if (similitudArtista < 0.3) { // Aumentado de 0.2 a 0.3
                    Log.d(tag, "   ❌ Similitud de artista muy baja: ${"%.2f".format(similitudArtista)} ('$artistaResultado' vs '$artistaOriginal')")
                    return false
                }
            }
        }

        Log.d(tag, "   ✅ Resultado válido - Título: ${"%.2f".format(similitudTitulo)}, Artista: ${"%.2f".format(calcularSimilitudCadena(artistaResultado, artista ?: ""))}")
        return true
    }

    private suspend fun buscarConRateLimit(query: String): GeniusSearchResponse? {
        val tiempoActual = System.currentTimeMillis()
        val tiempoDesdeUltimaBusqueda = tiempoActual - ultimaBusqueda

        if (tiempoDesdeUltimaBusqueda < intervaloMinimo) {
            val tiempoEspera = intervaloMinimo - tiempoDesdeUltimaBusqueda
            Log.d(tag, "⏳ Esperando ${tiempoEspera}ms por rate limiting")
            delay(tiempoEspera)
        }

        ultimaBusqueda = System.currentTimeMillis()

        try {
            val response = apiService.search(query)
            if (response.isSuccessful) {
                return response.body()
            } else {
                Log.e(tag, "❌ Error en API: ${response.code()} - ${response.message()}")
                return null
            }
        } catch (e: Exception) {
            Log.e(tag, "❌ Excepción en buscarConRateLimit: ${e.message}")
            return null
        }
    }
    private val rateLimiter = RateLimiter(8, 60000) // 8 requests por minuto

    companion object {
        private const val MIN_SIMILARITY_THRESHOLD = 0.7
        private const val ARTIST_SIMILARITY_THRESHOLD = 0.8
        private val BLACKLISTED_KEYWORDS = setOf(
            "discography",
            "album",
            "collection",
            "complete",
            "full album",
            "best of",
            "greatest hits",
            "vol.",
            "volume",
            "mix",
            "remix",
            "cover",
            "version",
            "live",
            "session",
            "instrumental"
        )
        private val USER_AGENTS = listOf(
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36",
            "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36"
        )
    }

    private suspend fun obtenerDatosCompletosViaScraping(songUrl: String): GeniusScraper.ScrapedSongData? =
        withContext(Dispatchers.IO) {
            Log.d(tag, "🚀 Iniciando scraping completo para: $songUrl")

            val scrapedData = geniusScraper.extractCompleteSongData(songUrl)

            if (scrapedData != null) {
                Log.d(tag, "✅ Scraping exitoso:")
                Log.d(tag, "   - Letras: ${scrapedData.lyrics?.length ?: 0} caracteres")
                Log.d(tag, "   - Portada: ${scrapedData.coverArtUrl}")
                Log.d(tag, "   - Artista: ${scrapedData.artistName}")
                Log.d(tag, "   - Álbum: ${scrapedData.albumName}")
            } else {
                Log.w(tag, "❌ Scraping falló para: $songUrl")
            }

            scrapedData
        }

    /**
     * Función principal mejorada con rate limiting y búsqueda inteligente
     */
    suspend fun sincronizarCancionAlReproducir(cancionConArtista: CancionConArtista): Boolean =
        withContext(Dispatchers.IO) {
            val cancion = cancionConArtista.cancion

            // Verificar si ya tenemos datos completos
            if (tieneDatosCompletos(cancion)) {
                Log.d(tag, "✅ Canción '${cancion.titulo}' ya tiene datos completos, omitiendo búsqueda")
                return@withContext true
            }

            // Verificar rate limiting
            if (!rateLimiter.tryAcquire()) {
                Log.w(tag, "⏳ Rate limit alcanzado para: ${cancion.titulo}")
                return@withContext false
            }

            Log.d(tag, "🎵 Sincronizando al reproducir: '${cancion.titulo}'")

            val resultadoBusqueda = buscarCancionEnGenius(cancion.titulo, cancionConArtista.artistaNombre)
            if (resultadoBusqueda == null) {
                Log.w(tag, "❌ No se pudo encontrar '${cancion.titulo}' en Genius")
                return@withContext false
            }

            // Actualizar datos básicos de la canción
            val cancionActualizada = cancion.copy(
                geniusId = resultadoBusqueda.id,
                geniusUrl = resultadoBusqueda.url
            )
            cancionDao.actualizarCancion(cancionActualizada)

            // Descargar letra en segundo plano (no bloqueante)
            withContext(Dispatchers.IO) {
                descargarYGuardarLetraMejorada(resultadoBusqueda.url, cancion.idCancion)

                // Sincronizar artista si es necesario
                resultadoBusqueda.primary_artist?.let { artista ->
                    if (cancion.idArtista != null) {
                        sincronizarDatosArtista(artista.id, cancion.idArtista)
                    }
                }

                // Procesar portada
                procesarPortadaCancion(resultadoBusqueda.url, resultadoBusqueda.song_art_image_url, cancion)
            }

            Log.d(tag, "✅ Sincronización completada para: '${cancion.titulo}'")
            return@withContext true
        }

    /**
     * Verifica si una canción ya tiene todos los datos necesarios
     */
    private suspend fun tieneDatosCompletos(cancion: CancionEntity): Boolean {
        // Verificar que tenga Genius ID y URL
        if (cancion.geniusId == null || cancion.geniusUrl == null) {
            return false
        }

        // Verificar que tenga letra
        val letraExistente = letraDao.obtenerLetraPorIdCancionSuspending(cancion.idCancion)
        if (letraExistente == null) {
            return false
        }

        // Verificar que el artista esté sincronizado (opcional)
        if (cancion.idArtista != null) {
            val artista = cancionDao.obtenerArtistaPorId(cancion.idArtista)
            if (artista?.geniusId == null) {
                return false
            }
        }

        return true
    }

    /**
     * Búsqueda inteligente mejorada con algoritmos de similitud
     */
    private suspend fun buscarCancionEnGenius(titulo: String, artista: String?): SongResult? =
        withContext(Dispatchers.IO) {
            Log.d(tag, "🎯 Iniciando búsqueda MEJORADA para: '$titulo' - '$artista'")

            // 1. Verificar si es contenido musical
            if (!esContenidoMusical(titulo, artista)) {
                Log.w(tag, "⏩ Saltando contenido no musical: '$titulo'")
                return@withContext null
            }

            // 2. Preprocesar búsqueda
            val (tituloLimpio, artistaLimpio) = preprocesarBusqueda(titulo, artista)

            if (tituloLimpio.isEmpty()) {
                Log.w(tag, "⏩ Título vacío después de preprocesamiento: '$titulo'")
                return@withContext null
            }

            Log.d(tag, "🔧 Búsqueda preprocesada: '$tituloLimpio' - '$artistaLimpio'")

            val estrategias = listOf(
                // Estrategia 1: Título + Artista exactos
                { "$tituloLimpio $artistaLimpio" },
                // Estrategia 2: Solo título (para artistas muy conocidos)
                { tituloLimpio },
                // Estrategia 3: Sin palabras comunes
                {
                    val tituloSimple = tituloLimpio.replace(
                        "the |and |feat|ft|with|&".toRegex(RegexOption.IGNORE_CASE), ""
                    ).trim()
                    if (tituloSimple.isNotEmpty()) "$tituloSimple $artistaLimpio" else ""
                }
            )

            for ((index, estrategia) in estrategias.withIndex()) {
                try {
                    val query = estrategia()
                    if (query.isEmpty()) continue

                    Log.d(tag, "🔍 Estrategia ${index + 1}: '$query'")

                    val resultados = buscarConRateLimit(query)
                    val resultadoValido = resultados?.response?.hits
                        ?.mapNotNull { it.result }
                        ?.filter { it.isValid() }
                        ?.firstOrNull { esResultadoValidoMejorado(it, tituloLimpio, artistaLimpio) }

                    if (resultadoValido != null) {
                        Log.d(tag, "✅ Estrategia ${index + 1} exitosa!")
                        return@withContext resultadoValido
                    }

                    // Esperar entre búsquedas para evitar rate limiting
                    delay(1500)

                } catch (e: Exception) {
                    Log.w(tag, "⚠️ Error en estrategia ${index + 1}: ${e.message}")
                    delay(2000)
                }
            }

            Log.w(tag, "❌ Todas las estrategias fallaron para: '$titulo'")
            return@withContext null
        }

    // Estrategias de búsqueda individuales
    private suspend fun busquedaDirecta(titulo: String, artista: String?): SongResult? {
        val query = if (artista != null) "$titulo $artista" else titulo
        return buscarConQuery(query, "directa")
    }

    private suspend fun busquedaSinCaracteresEspeciales(
        titulo: String, artista: String?
    ): SongResult? {
        val tituloLimpio = limpiarParaBusquedaAvanzada(titulo)
        val artistaLimpio = artista?.let { limpiarParaBusquedaAvanzada(it) }
        val query = if (artistaLimpio != null) "$tituloLimpio $artistaLimpio" else tituloLimpio
        return buscarConQuery(query, "sin_especiales")
    }

    private suspend fun busquedaPorArtistaPrimero(titulo: String, artista: String?): SongResult? {
        if (artista == null) return null
        val query = "$artista $titulo"
        return buscarConQuery(query, "artista_primero")
    }

    private suspend fun busquedaPorTituloSolo(titulo: String): SongResult? {
        return buscarConQuery(titulo, "titulo_solo")
    }

    private suspend fun busquedaPorArtistaSolo(artista: String?): SongResult? {
        if (artista == null) return null
        return buscarConQuery(artista, "artista_solo")
    }

    private suspend fun buscarConQuery(query: String, estrategia: String): SongResult? {
        if (!rateLimiter.tryAcquire()) {
            Log.w(tag, "Rate limit alcanzado para estrategia: $estrategia")
            return null
        }

        try {
            Log.d(tag, "   Intentando estrategia '$estrategia': '$query'")
            val searchResponse = buscarConRateLimit(query)

            if (searchResponse == null) {
                Log.d(tag, "   ❌ Error en búsqueda para estrategia: $estrategia")
                return null
            }

            val hits = searchResponse.response?.hits ?: emptyList()

            if (hits.isEmpty()) {
                Log.d(tag, "   ❌ Sin resultados en estrategia: $estrategia")
                return null
            }

            // Filtrar y seleccionar mejor resultado usando la NUEVA función
            val resultadosValidos = hits.mapNotNull { hit ->
                hit.result?.takeIf { it.isValid() }
            }.filter { resultado ->
                // Extraer título y artista de la query para usar la nueva función
                val partes = query.split(" ")
                val titulo = partes.firstOrNull() ?: ""
                val artista = partes.drop(1).joinToString(" ").takeIf { it.isNotEmpty() }
                esResultadoValidoMejorado(resultado, titulo, artista)
            }

            if (resultadosValidos.isEmpty()) {
                Log.d(tag, "   ❌ Sin resultados válidos en estrategia: $estrategia")
                return null
            }

            // Seleccionar el mejor resultado basado en similitud
            val mejorResultado = resultadosValidos.maxByOrNull { resultado ->
                calcularSimilitudCadena(resultado.title, query)
            }

            if (mejorResultado != null) {
                val puntuacion = calcularSimilitudCadena(mejorResultado.title, query)
                Log.d(tag, "   ✅ Resultado encontrado (puntuación: ${"%.2f".format(puntuacion)})")
                return mejorResultado
            }

            return null

        } catch (e: Exception) {
            Log.e(tag, "Error en estrategia $estrategia: ${e.message}")
            return null
        }
    }

    private fun calcularSimilitudJaccardMejorada(s1: String, s2: String): Double {
        val palabras1 = s1.split(Regex("\\s+")).filter { it.length > 2 }.toSet()
        val palabras2 = s2.split(Regex("\\s+")).filter { it.length > 2 }.toSet()

        if (palabras1.isEmpty() || palabras2.isEmpty()) return 0.0

        val intersection = palabras1.intersect(palabras2).size
        val union = palabras1.union(palabras2).size

        return intersection.toDouble() / union.toDouble()
    }

    /**
     * Algoritmo de similitud Jaccard para textos
     */
    private fun calcularSimilitudJaccard(s1: String, s2: String): Double {
        val words1 = s1.split("\\s+".toRegex()).toSet()
        val words2 = s2.split("\\s+".toRegex()).toSet()

        if (words1.isEmpty() || words2.isEmpty()) return 0.0

        val intersection = words1.intersect(words2).size
        val union = words1.union(words2).size

        return intersection.toDouble() / union.toDouble()
    }

    private fun esArtistaSimilar(artista1: String, artista2: String): Boolean {
        val normalizado1 = normalizarTextoArtista(artista1)
        val normalizado2 = normalizarTextoArtista(artista2)
        return calcularSimilitudJaccard(normalizado1, normalizado2) >= ARTIST_SIMILARITY_THRESHOLD
    }

    private fun normalizarTextoArtista(texto: String): String {
        return texto.lowercase().replace(Regex("[^a-z0-9\\s]"), "").replace(Regex("\\s+"), " ")
            .replace("the", "").trim()
    }

    private fun limpiarParaBusquedaAvanzada(texto: String): String {
        return texto.replace(
            Regex("""[^\w\sáéíóúñü]""", RegexOption.IGNORE_CASE),
            ""
        ) // Mantener acentos
            .replace(Regex("""\s+"""), " ").trim()
    }

    /**
     * Procesamiento robusto de portadas
     */
    private suspend fun procesarPortadaCancion(
        songUrl: String?, coverUrlFromApi: String?, cancion: CancionEntity
    ) = withContext(Dispatchers.IO) {
        if (songUrl == null) {
            Log.w(tag, "URL de canción es nula, no se puede procesar portada")
            return@withContext
        }

        var finalCoverUrl = coverUrlFromApi

        // Intentar obtener portada via scraping si no hay de la API
        if (finalCoverUrl.isNullOrBlank()) {
            Log.d(tag, "Intentando obtener portada via scraping para '${cancion.titulo}'...")
            finalCoverUrl = extraerUrlPortadaDesdeHtml(songUrl)
        }

        // Descargar y guardar la imagen
        finalCoverUrl?.let { url ->
            val nombreArchivo = "album_cover_${cancion.idAlbum ?: cancion.idCancion}.jpg"
            val portadaPathLocal = descargarYGuardarImagen(url, nombreArchivo)

            // Actualizar álbum si es necesario
            portadaPathLocal?.let { path ->
                actualizarPortadaAlbum(cancion.idAlbum, path)
            }
        } ?: run {
            Log.w(tag, "No se pudo obtener URL de portada para '${cancion.titulo}'")
        }
    }

    private suspend fun actualizarPortadaAlbum(albumId: Int?, portadaPath: String) =
        withContext(Dispatchers.IO) {
            if (albumId != null) {
                val album = cancionDao.obtenerAlbumPorId(albumId)
                if (album != null && album.portadaPath.isNullOrBlank()) {
                    cancionDao.actualizarAlbum(album.copy(portadaPath = portadaPath))
                    Log.d(tag, "Portada de álbum actualizada para '${album.titulo}'")
                } else if (album != null) {
                    Log.d(tag, "Álbum '${album.titulo}' ya tenía portada, no se sobreescribe.")
                }
            }
        }

    /**
     * Descarga de letras MEJORADA con scraping completo
     */
    private suspend fun descargarYGuardarLetraMejorada(url: String, idCancion: Int) =
        withContext(Dispatchers.IO) {
            val scrapedData = obtenerDatosCompletosViaScraping(url)

            if (scrapedData != null) {
                // Guardar letra si existe
                if (!scrapedData.lyrics.isNullOrBlank()) {
                    val letraEntity = LetraEntity(idCancion = idCancion, letra = scrapedData.lyrics)
                    letraDao.insertarLetra(letraEntity)
                    Log.d(tag, "💾 Letra guardada (${scrapedData.lyrics.length} caracteres)")
                }

                // Descargar y guardar portada si existe - ESTRUCTURA SIMPLIFICADA
                if (!scrapedData.coverArtUrl.isNullOrBlank()) {
                    val nombreArchivo = "album_cover_${idCancion}.jpg"
                    val portadaPath = descargarYGuardarImagen(scrapedData.coverArtUrl, nombreArchivo)

                    // Actualizar información del álbum si tenemos datos
                    actualizarInformacionAlbumDesdeScraping(idCancion, scrapedData, portadaPath)
                }

                // Log adicional de metadata obtenida
                Log.d(tag, "📊 Metadata obtenida - Artista: ${scrapedData.artistName}, Álbum: ${scrapedData.albumName}")
            } else {
                Log.w(tag, "⚠️ No se pudieron obtener datos via scraping, usando método antiguo")
                // Fallback a método antiguo
                descargarYGuardarLetraLegacy(url, idCancion)
            }
        }

    /**
     * Método legacy como fallback
     */
    private suspend fun descargarYGuardarLetraLegacy(url: String, idCancion: Int) =
        withContext(Dispatchers.IO) {
            Log.d(tag, "Descargando letra (método legacy) desde: $url")

            try {
                val userAgent = USER_AGENTS.random()
                val doc: Document = Jsoup.connect(url).userAgent(userAgent).timeout(15000).followRedirects(true).get()

                val container = doc.select("div[data-lyrics-container='true']").firstOrNull()
                if (container == null) {
                    Log.w(tag, "No se encontró contenedor de letras en $url")
                    return@withContext
                }

                // Limpiar elementos no deseados
                container.select("div[class*='LyricsHeader__Container']").remove()
                container.select("div[class*='Advertisement']").remove()
                container.select("div[class*='Ad']").remove()

                val lyricsContent = StringBuilder()
                container.childNodes().forEach { node ->
                    extractTextWithLineBreaks(node, lyricsContent)
                }

                val letraFinal = lyricsContent.toString()
                    .replace(Regex("[ \t]{2,}"), " ")
                    .replace(Regex("\n{3,}"), "\n\n")
                    .trim()

                if (letraFinal.isNotBlank()) {
                    val letraEntity = LetraEntity(idCancion = idCancion, letra = letraFinal)
                    letraDao.insertarLetra(letraEntity)
                    Log.d(tag, "Letra guardada (legacy) para idCancion: $idCancion")
                }

            } catch (e: Exception) {
                Log.e(tag, "Error en método legacy: ${e.message}")
            }
        }

    /**
     * Actualizar información del álbum con datos del scraping
     */
    private suspend fun actualizarInformacionAlbumDesdeScraping(
        idCancion: Int,
        scrapedData: GeniusScraper.ScrapedSongData,
        portadaPath: String?
    ) = withContext(Dispatchers.IO) {
        try {
            val cancion: CancionEntity? = cancionDao.obtenerCancionPorId(idCancion).firstOrNull()
            val albumId = cancion?.idAlbum
            if (albumId != null) {
                // Usamos la función suspend que devuelve directamente AlbumEntity?
                val album = cancionDao.obtenerAlbumPorId(albumId)
                if (album != null) {
                    val albumActualizado = album.copy(
                        titulo = scrapedData.albumName ?: album.titulo,
                        portadaPath = portadaPath ?: album.portadaPath
                    )
                    cancionDao.actualizarAlbum(albumActualizado)
                    Log.d(tag, "🔄 Álbum actualizado: ${albumActualizado.titulo}")
                } else {
                    Log.d(tag, "⚠️ No se encontró álbum con ID: $albumId")
                }
            } else {
                Log.d(tag, "⚠️ La canción no tiene álbum asociado")
            }
        } catch (e: Exception) {
            Log.e(tag, "❌ Error al actualizar álbum desde scraping: ${e.message}", e)
        }
    }

    /**
     * Sincronización de artista mejorada
     */
    private suspend fun sincronizarDatosArtista(geniusArtistId: String, localArtistId: Int) =
        withContext(Dispatchers.IO) {
            val localArtist = cancionDao.obtenerArtistaPorId(localArtistId)

            if (localArtist == null) {
                Log.w(
                    tag,
                    "Intento de sincronizar artista fallido: Artista local con ID $localArtistId no encontrado."
                )
                return@withContext
            }
            if (localArtist.geniusId != null) {
                Log.d(
                    tag,
                    "Artista '${localArtist.nombre}' (ID: $localArtistId) ya está sincronizado con Genius (GeniusID: ${localArtist.geniusId})."
                )
                return@withContext
            }

            Log.d(
                tag,
                "Sincronizando datos del artista desde Genius: ${localArtist.nombre} (GeniusID: $geniusArtistId)"
            )

            try {
                val artistResponse = apiService.getArtist(geniusArtistId)
                if (!artistResponse.isSuccessful) {
                    Log.e(
                        tag,
                        "Error en API GetArtist (${artistResponse.code()}) para GeniusID: $geniusArtistId"
                    )
                    return@withContext
                }

                val artistDetails = artistResponse.body()?.response?.artist
                if (artistDetails != null) {
                    val imagenArtistaPath = if (localArtist.imageUrl.isNullOrBlank()) {
                        artistDetails.image_url?.let { url ->
                            descargarYGuardarImagen(url, "artist_${localArtistId}.jpg")
                        }
                    } else {
                        localArtist.imageUrl
                    }

                    val artistaActualizado = localArtist.copy(
                        geniusId = artistDetails.id,
                        descripcion = artistDetails.description?.plain?.takeIf { it.isNotBlank() }
                            ?: localArtist.descripcion,
                        imageUrl = imagenArtistaPath
                    )
                    cancionDao.actualizarArtista(artistaActualizado)
                    Log.d(
                        tag,
                        "ArtistaEntity actualizada para: ${artistaActualizado.nombre} (ID: $localArtistId)"
                    )
                } else {
                    Log.w(
                        tag,
                        "La respuesta de GetArtist para GeniusID $geniusArtistId no contenía detalles del artista."
                    )
                }
            } catch (e: Exception) {
                Log.e(
                    tag,
                    "Error al sincronizar datos del artista (GeniusID: $geniusArtistId): ${e.message}",
                    e
                )
            }
        }

    private suspend fun extraerUrlPortadaDesdeHtml(songUrl: String): String? =
        withContext(Dispatchers.IO) {
            Log.d(tag, "Extrayendo URL de portada desde HTML: $songUrl")
            try {
                val userAgent = USER_AGENTS.random()
                val doc: Document = Jsoup.connect(songUrl).userAgent(userAgent).timeout(10000).get()

                var imgElement =
                    doc.select("div[class^='SongHeader-desktop__CoverArt-'] img").firstOrNull()

                if (imgElement == null) {
                    imgElement = doc.select(".cover_art img, .song_header img").firstOrNull()
                }

                var imageUrl = imgElement?.attr("src")?.takeIf { it.isNotBlank() }
                if (imageUrl.isNullOrBlank()) {
                    imageUrl = doc.select("meta[property=og:image]").firstOrNull()?.attr("content")
                        ?.takeIf { it.isNotBlank() }
                    if (imageUrl != null) Log.d(
                        tag, "URL de portada encontrada via og:image metadata."
                    )
                }

                if (!imageUrl.isNullOrBlank()) {
                    Log.d(tag, "URL de portada encontrada via scraping: $imageUrl")
                    return@withContext imageUrl
                } else {
                    Log.w(
                        tag,
                        "No se pudo encontrar la URL de la portada mediante scraping en $songUrl"
                    )
                }
            } catch (e: IOException) {
                Log.e(
                    tag,
                    "Error de red/timeout al scrapear HTML para portada ($songUrl): ${e.message}"
                )
            } catch (e: Exception) {
                Log.e(
                    tag,
                    "Error inesperado al scrapear HTML para portada ($songUrl): ${e.message}",
                    e
                )
            }
            return@withContext null
        }

    private fun extractTextWithLineBreaks(node: Node, builder: StringBuilder) {
        when (node) {
            is TextNode -> {
                val text = node.text().replace(Regex("\\s+"), " ").trim()
                if (text.isNotEmpty()) {
                    if (builder.isNotEmpty() && !builder.endsWith("\n") && !builder.endsWith(" ")) {
                        builder.append(" ")
                    }
                    builder.append(text)
                }
            }

            is Element -> {
                if (node.tagName().equals("br", ignoreCase = true)) {
                    if (builder.endsWith(" ")) {
                        builder.setLength(builder.length - 1)
                    }
                    if (!builder.endsWith("\n")) {
                        builder.append("\n")
                    }
                } else if (node.isBlock || node.tagName().equals("p", ignoreCase = true)) {
                    if (builder.isNotEmpty() && !builder.endsWith("\n\n") && !builder.endsWith("\n")) {
                        if (builder.endsWith(" ")) builder.setLength(builder.length - 1)
                        builder.append("\n\n")
                    }
                    node.childNodes().forEach { child -> extractTextWithLineBreaks(child, builder) }
                    if (builder.isNotEmpty() && !builder.endsWith("\n\n")) {
                        if (builder.endsWith(" ")) builder.setLength(builder.length - 1)
                        builder.append(if (builder.endsWith("\n")) "\n" else "\n\n")
                    }
                } else {
                    node.childNodes().forEach { child -> extractTextWithLineBreaks(child, builder) }
                }
            }
        }
    }

    private suspend fun descargarYGuardarImagen(url: String, nombreArchivo: String): String? =
        withContext(Dispatchers.IO) {
            Log.d(tag, "Descargando imagen: $url -> $nombreArchivo")
            var response: okhttp3.Response? = null
            try {
                val request = Request.Builder().url(url).build()
                response = okHttpClient.newCall(request).execute()

                if (!response.isSuccessful) {
                    Log.e(
                        tag,
                        "Fallo al descargar imagen '$url': ${response.code} ${response.message}"
                    )
                    return@withContext null
                }

                val cacheDir = context.cacheDir
                val imagesDir = File(cacheDir, "image_cache")
                if (!imagesDir.exists()) {
                    imagesDir.mkdirs()
                }
                val file = File(imagesDir, nombreArchivo)

                response.body?.byteStream()?.use { inputStream ->
                    FileOutputStream(file).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                } ?: run {
                    Log.e(tag, "El cuerpo de la respuesta estaba vacío para la imagen '$url'")
                    return@withContext null
                }

                Log.d(tag, "Imagen guardada exitosamente en: ${file.absolutePath}")
                return@withContext file.absolutePath

            } catch (e: IOException) {
                Log.e(tag, "Error de IO al descargar/guardar imagen '$url': ${e.message}")
                return@withContext null
            } catch (e: Exception) {
                Log.e(tag, "Error inesperado al descargar/guardar imagen '$url': ${e.message}", e)
                return@withContext null
            } finally {
                response?.close()
            }
        }

    /**
     * Limpieza de texto para búsqueda (versión original mejorada)
     */
    private fun limpiarTextoBusqueda(texto: String): String {
        return texto.replace(Regex("[^a-zA-Z0-9\\sáéíóúñüÁÉÍÓÚÑÜ]"), " ")
            .replace(Regex("\\s+"), " ").trim()
    }

    /**
     * Filtrado original de resultados (para compatibilidad)
     */
    private fun esResultadoValido(
        resultado: SongResult, tituloOriginal: String, artistaOriginal: String?
    ): Boolean {
        val tituloLower = resultado.title.lowercase()

        // Rechazar resultados con palabras clave no deseadas
        if (BLACKLISTED_KEYWORDS.any { keyword ->
                tituloLower.contains(keyword)
            }) {
            Log.d(tag, "Resultado rechazado por palabra clave: '${resultado.title}'")
            return false
        }

        // Verificación de artista (cuando está disponible)
        artistaOriginal?.let { original ->
            val artistaResultado = resultado.primary_artist?.name ?: return@let
            if (!esArtistaSimilar(artistaResultado, original)) {
                Log.d(tag, "Artista no coincide: '$artistaResultado' vs '$original'")
                return false
            }
        }

        return true
    }
}

/**
 * Rate Limiter para controlar llamadas a API
 */
class RateLimiter(private val maxRequests: Int, private val timeWindow: Long) {
    private val requests = mutableListOf<Long>()
    private val lock = ReentrantLock()

    fun tryAcquire(): Boolean {
        lock.withLock {
            val now = System.currentTimeMillis()
            val windowStart = now - timeWindow

            // Limpiar requests antiguos
            requests.removeAll { it < windowStart }

            // Verificar si podemos hacer otra request
            return if (requests.size < maxRequests) {
                requests.add(now)
                true
            } else {
                false
            }
        }
    }
}

// Extensión para validar resultados
private fun SongResult.isValid(): Boolean {
    return id.isNotBlank() && title.isNotBlank() && url.isNotBlank() && primary_artist != null
}