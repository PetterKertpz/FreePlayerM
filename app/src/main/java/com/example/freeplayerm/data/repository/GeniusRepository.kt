// app/src/main/java/com/example/freeplayerm/data/repository/GeniusRepository.kt
package com.example.freeplayerm.data.repository

import android.content.Context
import android.util.Log
import com.example.freeplayerm.data.local.entity.CancionEntity
import com.example.freeplayerm.data.local.entity.LetraEntity
import com.example.freeplayerm.data.local.dao.CancionDao
import com.example.freeplayerm.data.local.dao.LetraDao
import com.example.freeplayerm.data.local.entity.relations.CancionConArtista
import com.example.freeplayerm.data.remote.GeniusApiService
import com.example.freeplayerm.data.remote.GeniusScraper
import com.example.freeplayerm.data.remote.GeniusServiceOptimizado
import com.example.freeplayerm.data.remote.ResultadoBusquedaGenius
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
    private var ultimaBusqueda = 0L
    private val intervaloMinimo = 2500L

    private fun preprocesarBusqueda(titulo: String, artista: String?): Pair<String, String> {
        var tituloLimpio = titulo
            .replace("- Topic", "", ignoreCase = true)
            .replace("(Official Video)", "", ignoreCase = true)
            .replace("(Official Audio)", "", ignoreCase = true)
            .replace("\\[.*]".toRegex(), "")
            .replace("\\(.*\\)".toRegex(), "")
            .replace("ft\\.|feat\\.|featuring".toRegex(RegexOption.IGNORE_CASE), "")
            .trim()

        var artistaLimpio = (artista ?: "")
            .replace("- Topic", "", ignoreCase = true)
            .replace("VEVO", "", ignoreCase = true)
            .replace("Official", "", ignoreCase = true)
            .trim()

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

        val exclusiones = listOf(
            "discography", "album", "collection", "calendar", "review",
            "translation", "traducción", "cover", "mix", "remix", "annotated",
            "interview", "unreleased", "demo", "preview", "snippet"
        )

        if (exclusiones.any { tituloResultado.contains(it, ignoreCase = true) }) {
            Log.d(tag, "   ❌ Resultado rechazado por palabra clave: '${resultado.title}'")
            return false
        }

        val similitudTitulo = calcularSimilitudCadena(tituloResultado, titulo)
        if (similitudTitulo < 0.4) {
            Log.d(tag, "   ❌ Similitud de título muy baja: ${"%.2f".format(similitudTitulo)} ('$tituloResultado' vs '$titulo')")
            return false
        }

        artista?.let { artistaOriginal ->
            if (artistaResultado.isNotEmpty()) {
                val similitudArtista = calcularSimilitudCadena(artistaResultado, artistaOriginal)
                if (similitudArtista < 0.3) {
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
            val espera = intervaloMinimo - tiempoDesdeUltimaBusqueda
            Log.d(tag, "⏳ Esperando ${espera}ms antes de la siguiente búsqueda...")
            delay(espera)
        }

        return try {
            val response = apiService.search(query)
            ultimaBusqueda = System.currentTimeMillis()

            if (response.isSuccessful) {
                response.body()
            } else {
                Log.w(tag, "Búsqueda no exitosa: ${response.code()}")
                null
            }
        } catch (e: Exception) {
            Log.e(tag, "Error en búsqueda: ${e.message}")
            null
        }
    }

    /**
     * Sincroniza la información de Genius para una canción cuando se está reproduciendo.
     * Busca y guarda letra y portada si no están disponibles.
     */
    suspend fun sincronizarCancionAlReproducir(cancion: CancionEntity) {
        withContext(Dispatchers.IO) {
            try {
                Log.d(tag, "🎵 Sincronizando canción al reproducir: '${cancion.titulo}'")

                // Verificar si ya tiene letra
                val letraExistente = letraDao.obtenerLetraPorIdCancionSuspending(cancion.idCancion)

                // Si ya tiene letra y portada, no hacer nada
                if (letraExistente != null && !cancion.portadaPath.isNullOrEmpty()) {
                    Log.d(tag, "✅ '${cancion.titulo}' ya tiene letra y portada")
                    return@withContext
                }

                // Obtener el artista de la canción
                val cancionConArtista = cancionDao.obtenerCancionConArtista(cancion.idCancion)
                    .firstOrNull()

                val artista = cancionConArtista?.artistaNombre

                if (artista == null) {
                    Log.d(tag, "⏩ '${cancion.titulo}' no tiene artista asociado")
                    return@withContext
                }

                // Verificar si es contenido musical
                if (!esContenidoMusical(cancion.titulo, artista)) {
                    Log.d(tag, "⏩ '${cancion.titulo}' no es contenido musical")
                    return@withContext
                }

                // Buscar en Genius
                val resultado = buscarEnGeniusCompleto(cancion.titulo, artista)

                if (resultado != null) {
                    // Guardar datos solo si falta letra o portada
                    if (letraExistente == null) {
                        guardarDatosGeniusEnBD(cancion, resultado)
                        Log.d(tag, "✅ Letra sincronizada para '${cancion.titulo}'")
                    } else if (cancion.portadaPath.isNullOrEmpty()) {
                        // Solo guardar portada
                        guardarSoloPortada(cancion, resultado)
                        Log.d(tag, "✅ Portada sincronizada para '${cancion.titulo}'")
                    }
                } else {
                    Log.d(tag, "❌ No se encontró información en Genius para '${cancion.titulo}'")
                }

            } catch (e: Exception) {
                Log.e(tag, "Error sincronizando canción '${cancion.titulo}': ${e.message}", e)
            }
        }
    }

    /**
     * Guarda solo la portada de una canción (cuando ya tiene letra pero no portada)
     */
    private suspend fun guardarSoloPortada(cancion: CancionEntity, resultado: SongResult) {
        withContext(Dispatchers.IO) {
            try {
                // Actualizar Genius ID y URL si no los tiene
                if (cancion.geniusId == null) {
                    cancionDao.actualizarCancion(
                        cancion.copy(
                            geniusId = resultado.id,
                            geniusUrl = resultado.url
                        )
                    )
                }

                // Obtener datos completos para la portada
                val datosCompletos = geniusScraper.extractCompleteSongData(resultado.url)

                datosCompletos?.coverArtUrl?.let { urlPortada ->
                    val nombreArchivo = "cover_${cancion.idCancion}_${System.currentTimeMillis()}.jpg"
                    val rutaLocal = descargarYGuardarImagen(urlPortada, nombreArchivo)

                    rutaLocal?.let { ruta ->
                        cancionDao.actualizarCancion(cancion.copy(portadaPath = ruta))
                        Log.d(tag, "✅ Portada guardada: $ruta")
                    }
                }

            } catch (e: Exception) {
                Log.e(tag, "Error guardando portada: ${e.message}", e)
            }
        }
    }

    suspend fun buscarYGuardarLetras(cancionesConArtista: List<CancionConArtista>) {
        withContext(Dispatchers.IO) {
            Log.d(tag, "🚀 Iniciando búsqueda de letras para ${cancionesConArtista.size} canciones")

            cancionesConArtista.forEachIndexed { index, cancionConArtista ->
                try {
                    val cancion = cancionConArtista.cancion
                    val artista = cancionConArtista.artistaNombre

                    if (artista == null) {
                        Log.d(tag, "[$index/${cancionesConArtista.size}] ⏩ Saltando '${cancion.titulo}' - Sin artista")
                        return@forEachIndexed
                    }

                    if (!esContenidoMusical(cancion.titulo, artista)) {
                        Log.d(tag, "[$index/${cancionesConArtista.size}] ⏩ Saltando contenido no musical: '${cancion.titulo}'")
                        return@forEachIndexed
                    }

                    val letraExistente = letraDao.obtenerLetraPorIdCancionSuspending(cancion.idCancion)
                    if (letraExistente != null) {
                        Log.d(tag, "[$index/${cancionesConArtista.size}] ✅ '${cancion.titulo}' ya tiene letra guardada")
                        return@forEachIndexed
                    }

                    Log.d(tag, "[$index/${cancionesConArtista.size}] 🔍 Buscando: '${cancion.titulo}' - $artista")

                    val resultado = buscarEnGeniusCompleto(cancion.titulo, artista)
                    if (resultado != null) {
                        guardarDatosGeniusEnBD(cancion, resultado)
                        Log.d(tag, "[$index/${cancionesConArtista.size}] ✅ Letra guardada para '${cancion.titulo}'")
                    } else {
                        Log.d(tag, "[$index/${cancionesConArtista.size}] ❌ No se encontró letra para '${cancion.titulo}'")
                    }

                } catch (e: Exception) {
                    Log.e(tag, "Error procesando canción ${index + 1}: ${e.message}", e)
                }
            }

            Log.d(tag, "✅ Búsqueda de letras completada")
        }
    }

    private suspend fun buscarEnGeniusCompleto(titulo: String, artista: String?): SongResult? {
        return withContext(Dispatchers.IO) {
            val (tituloLimpio, artistaLimpio) = preprocesarBusqueda(titulo, artista)

            if (tituloLimpio.isEmpty()) {
                return@withContext null
            }

            val queryPrincipal = buildString {
                append(limpiarTextoBusqueda(tituloLimpio))
                if (artistaLimpio.isNotEmpty()) {
                    append(" ")
                    append(limpiarTextoBusqueda(artistaLimpio))
                }
            }

            Log.d(tag, "🔎 Query principal: '$queryPrincipal'")

            val response = buscarConRateLimit(queryPrincipal)
            val hits = response?.response?.hits ?: emptyList()

            val resultadosFiltrados = hits
                .mapNotNull { it.result }
                .filter { esResultadoValidoMejorado(it, tituloLimpio, artistaLimpio) }

            return@withContext resultadosFiltrados.firstOrNull()
        }
    }

    private suspend fun guardarDatosGeniusEnBD(cancion: CancionEntity, resultado: SongResult) {
        withContext(Dispatchers.IO) {
            try {
                cancionDao.actualizarCancion(
                    cancion.copy(
                        geniusId = resultado.id,
                        geniusUrl = resultado.url
                    )
                )

                val datosCompletos = geniusScraper.extractCompleteSongData(resultado.url)

                datosCompletos?.lyrics?.let { textoLetra ->
                    if (textoLetra.isNotBlank()) {
                        val nuevaLetra = LetraEntity(
                            idCancion = cancion.idCancion,
                            textoLetra = textoLetra,
                            fuente = LetraEntity.FUENTE_GENIUS,
                            urlFuente = resultado.url,
                            verificada = true,
                            fechaAgregado = System.currentTimeMillis()
                        )
                        letraDao.insertarLetra(nuevaLetra)

                        cancionDao.actualizarCancion(cancion.copy(letraDisponible = true))

                        Log.d(tag, "✅ Letra guardada: '${cancion.titulo}'")
                    }
                }

                datosCompletos?.coverArtUrl?.let { urlPortada ->
                    val nombreArchivo = "cover_${cancion.idCancion}_${System.currentTimeMillis()}.jpg"
                    val rutaLocal = descargarYGuardarImagen(urlPortada, nombreArchivo)

                    rutaLocal?.let { ruta ->
                        cancionDao.actualizarCancion(cancion.copy(portadaPath = ruta))
                        Log.d(tag, "✅ Portada guardada: $ruta")
                    }
                }

            } catch (e: Exception) {
                Log.e(tag, "Error guardando datos de Genius: ${e.message}", e)
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
                    Log.e(tag, "Fallo al descargar imagen '$url': ${response.code} ${response.message}")
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

    private fun limpiarTextoBusqueda(texto: String): String {
        return texto.replace(Regex("[^a-zA-Z0-9\\sáéíóúñüÁÉÍÓÚÑÜ]"), " ")
            .replace(Regex("\\s+"), " ").trim()
    }

    companion object {
        private val BLACKLISTED_KEYWORDS = listOf(
            "discography", "album", "collection", "calendar", "review",
            "translation", "traducción", "annotated", "interview"
        )

        private val USER_AGENTS = listOf(
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36",
            "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36"
        )
    }
}

class RateLimiter(private val maxRequests: Int, private val timeWindow: Long) {
    private val requests = mutableListOf<Long>()
    private val lock = ReentrantLock()

    fun tryAcquire(): Boolean {
        lock.withLock {
            val now = System.currentTimeMillis()
            val windowStart = now - timeWindow

            requests.removeAll { it < windowStart }

            return if (requests.size < maxRequests) {
                requests.add(now)
                true
            } else {
                false
            }
        }
    }
}

private fun SongResult.isValid(): Boolean {
    return id.isNotBlank() && title.isNotBlank() && url.isNotBlank() && primary_artist != null
}