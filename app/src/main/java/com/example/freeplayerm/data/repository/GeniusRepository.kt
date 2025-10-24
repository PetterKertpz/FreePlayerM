// app/src/main/java/com/example/freeplayerm/data/repository/GeniusRepository.kt
package com.example.freeplayerm.data.repository

import android.content.Context
import android.util.Log
import com.example.freeplayerm.com.example.freeplayerm.data.local.entity.ArtistaEntity
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

    private fun corregirArtistaTituloInvertidos(titulo: String, artista: String?): Pair<String, String?> {
        if (artista == null) return Pair(titulo, artista)

        var tituloCorregido = titulo
        var artistaCorregido = artista

        // Patrones que indican que el "artista" podría ser realmente un título de canción
        val patronesTituloCancion = listOf(
            // 1. Cualquier texto entre paréntesis, corchetes o llaves (indicativo de descriptores como "live", "sub español", etc.)
            Regex("""[(\[{].{2,50}?[)\]}]"""),

            // 2. Palabras comunes asociadas a versiones, traducciones o metadatos
            Regex("""\b(sub|subtitulado|subtitulos|subtitulada|traducci[oó]n|lyrics?|letra|video|audio|oficial|official)\b""", RegexOption.IGNORE_CASE),

            // 3. Modificadores y variantes de estilo o mezcla
            Regex("""\b(remix|mix|edit|bootleg|slowed|reverb|speed(ed)?|bass|bassboosted|mashup|extended|cut|blend|nightcore)\b""", RegexOption.IGNORE_CASE),

            // 4. Versiones o formatos de interpretación
            Regex("""\b(version|versi[oó]n|cover|demo|live|en vivo|acoustic|instrumental|studio|remaster(ed)?|orchestral|karaoke|performance)\b""", RegexOption.IGNORE_CASE),

            // 5. Indicadores de idioma o subtitulación
            Regex("""\b(espa[nñ]ol|english|ingl[eé]s|franc[eé]s|portugu[eé]s|italiano|alem[aá]n|chino|japon[eé]s)\b""", RegexOption.IGNORE_CASE),

            // 6. Elementos comunes de títulos digitales o descripciones multimedia
            Regex("""\b(audio|video|clip|mv|hd|hq|official|visualizer|fanmade|editado|remake)\b""", RegexOption.IGNORE_CASE),

            // 7. Indicadores de colaboración o featuring dentro del campo erróneo
            Regex("""\b(ft|feat|featuring|con|x|&|vs\.?)\b""", RegexOption.IGNORE_CASE),

            // 8. Menciones a álbumes, tracks o números de versión
            Regex("""\b(track|single|album|disc|cd|ep|lp|side\s?[abAB]|bonus)\b""", RegexOption.IGNORE_CASE),

            // 9. Títulos con exceso de puntuación o símbolos (ej. “--”, “//”, “•••”, etc.)
            Regex("""[-_/•~]{2,}"""),

            // 10. Inclusión de años, que suelen acompañar versiones o remasters (ej. "Remix 2020", "Live 1998")
            Regex("""\b(19|20)\d{2}\b""")
        )

        // Patrones que indican que el "título" podría ser realmente un artista
        val patronesNombreArtista = listOf(
            // 1. Palabras asociadas a colectivos, bandas o agrupaciones musicales
            Regex("""\b(band|banda|group|grupo|crew|collective|colectivo|project|proyecto|orchestra|ensemble|choir|quartet|duo|trio|squad)\b""", RegexOption.IGNORE_CASE),

            // 2. Formatos "Nombre Apellido", "Nombre Inicial", o múltiples nombres propios (ej. "John Mayer", "David Guetta", "Carlos Vives")
            Regex("""^[A-ZÁÉÍÓÚÑ][a-záéíóúñ]+(?:\s+[A-ZÁÉÍÓÚÑ][a-záéíóúñ]+){0,3}$"""),

            // 3. Formato "Nombre Artístico" con iniciales o alias (ej. "J Balvin", "A$AP Rocky", "DJ Snake", "MC Kevinho")
            Regex("""^(DJ|MC|Mr\.?|Mrs\.?|Miss|Sir|Saint|San|King|Queen|Prince|Lil|Big|Young|El|La|Los|Las|The)\s?[A-Z0-9$€¥£_.\-]+(?:\s[A-Z0-9$€¥£_.\-]+)*$""", RegexOption.IGNORE_CASE),

            // 4. Alias o seudónimos con mayúsculas estilizadas (ej. "Avicii", "Skrillex", "Beyoncé", "Daft Punk")
            Regex("""^[A-Z0-9$€¥£_.\-]{2,}(?:\s[A-Z0-9$€¥£_.\-]{2,})*$"""),

            // 5. Agrupaciones con conectores comunes (ej. "Simon & Garfunkel", "Selena y Los Dinos", "Guns N' Roses")
            Regex("""\b(&|and|y|with|n'|feat\.?|ft\.?)\b""", RegexOption.IGNORE_CASE),

            // 6. Nombres compuestos con números o identificadores de banda (ej. "Maroon 5", "Blink-182", "U2")
            Regex("""^[A-Z][a-zA-Z]*[-\s]?\d{1,3}$"""),

            // 7. Palabras clave que suelen estar en nombres de artistas o bandas
            Regex("""\b(crew|unit|gang|posse|sound|collective|records|productions|studios|inc|ent|entertainment|label)\b""", RegexOption.IGNORE_CASE),

            // 8. Acrónimos o siglas de 2–5 letras en mayúsculas (ej. "BTS", "ABBA", "NSYNC")
            Regex("""^[A-Z]{2,5}$"""),

            // 9. Artistas con apodos entre comillas o paréntesis (ej. “Marshall (Eminem) Mathers”)
            Regex("""["'\(\[]?[A-Z][a-z]+["'\)\]]?(?:\s*\([A-Z0-9$€¥£_.\-]+\))?"""),

            // 10. Nombres que comienzan con el artículo "The" seguido de una o más palabras (ej. "The Weeknd", "The Beatles")
            Regex("""^The\s[A-Z][a-zA-Z0-9\s'\-]+$""", RegexOption.IGNORE_CASE),

            // 11. Artistas latinos con partículas comunes (ej. “De”, “Del”, “La”, “El”)
            Regex("""^[A-ZÁÉÍÓÚÑ][a-záéíóúñ]+(?:\s(de|del|la|las|el|los)\s[A-ZÁÉÍÓÚÑ][a-záéíóúñ]+)+$""", RegexOption.IGNORE_CASE),

            // 12. Artistas con apellidos compuestos o dobles (ej. “Juan Luis Guerra”, “José José”, “Ricardo Arjona”)
            Regex("""^[A-ZÁÉÍÓÚÑ][a-záéíóúñ]+(\s[A-ZÁÉÍÓÚÑ][a-záéíóúñ]+){1,2}$"""),

            // 13. Detección de estructuras mixtas típicas de seudónimos (ej. “Bad Bunny”, “Kid Cudi”, “Ice Cube”)
            Regex("""^(Bad|Kid|Ice|Young|Lil|Big|Fat|Slim|Don|El|La|Los|Las|King|Queen|Saint)\s[A-Z][a-z]+$""", RegexOption.IGNORE_CASE),

            // 14. Artistas electrónicos con notación simbólica o de caracteres especiales (ej. “Deadmau5”, “Madeon”, “Øfdream”)
            Regex("""^[A-Za-z0-9_\-@$€¥£øØäëïöüáéíóúñç]{3,20}$"""),

            // 15. Inclusión de partículas honoríficas o apodos formales
            Regex("""\b(Sir|Lord|Lady|Doctor|Dr\.|Profesor|Prof\.|DJ|MC)\b""", RegexOption.IGNORE_CASE)
        )

        val artistaPareceCancion = patronesTituloCancion.any { it.containsMatchIn(artista) }
        val tituloPareceArtista = patronesNombreArtista.any { it.containsMatchIn(titulo) }

        val resultado = if (artistaPareceCancion && tituloPareceArtista) {
            Log.d(tag, "🔄 Corrigiendo inversión: '$titulo' <-> '$artista'")
            Pair(artista, titulo) // Intercambiar
        } else {
            Pair(titulo, artista)
        }

        // === NUEVO: LIMPIAR "(sub. Español)" DEL TÍTULO CORREGIDO ===
        val tituloLimpio = resultado.first
            .replace(
                Regex(
                    """\s*[(\[{]# Paréntesis o corchetes de apertura
               [^)\]}]*# Contenido interno
               (sub|subtitulado|subtitulada|traducci[oó]n|traduction|translation|
                español|english|lyrics?|letra|audio|video|oficial|official|
                slowed|reverb|remix|mix|cover|live|acoustic|instrumental)
               [^)\]}]*# Más texto interno
               [)\]}]# Cierre
            """.trimMargin(),
                    setOf(RegexOption.IGNORE_CASE, RegexOption.COMMENTS)
                ),
                ""
            )
            .replace(Regex("""\s{2,}"""), " ") // Limpieza de espacios redundantes
            .trim()

        return Pair(tituloLimpio, resultado.second)
    }

    private fun preprocesarBusqueda(titulo: String, artista: String?): Pair<String, String?> {
        // Limpieza avanzada de ruido textual, anotaciones y descriptores no relevantes
        var tituloLimpio = titulo
            // 1. Remover sufijos frecuentes en títulos de YouTube o catálogos
            .replace(Regex("""(?i)\b(-|\|)?\s*(official\s*(music)?\s*(video|audio|clip)|visualizer|mv|hd|hq|lyrics?|letra|subtitulado|sub\s?espa[nñ]ol|traducci[oó]n|version|ver\.|remix|mix|edit|live|acoustic|instrumental|performance|karaoke)\b"""), "")
            // 2. Eliminar paréntesis, corchetes o llaves con texto interno irrelevante
            .replace(Regex("""[\(\[\{][^)\]\}]{0,80}[\)\]\}]"""), "")
            // 3. Remover colaboraciones, dejando solo el nombre principal
            .replace(Regex("""(?i)\s*(ft\.?|feat\.?|featuring|con|x|y)\s+[A-Z0-9$€¥£_.\- ]+"""), "")
            // 4. Limpieza de conectores o símbolos finales redundantes
            .replace(Regex("""[\-\|\·•~_]+$"""), "")
            // 5. Normalización de caracteres especiales y espacios
            .replace(Regex("""\s{2,}"""), " ")
            .replace(Regex("""[–—]+"""), "-")
            .replace(Regex("""^\s*[-|]+\s*"""), "")
            .trim()

        var artistaLimpio = (artista ?: "")
            // 1. Remover términos comunes de canales o distribuidoras
            .replace(
                Regex(
                    """(?i)\b([-|])?\s*(topic|official|vevo|channel|records?|music|productions?|entertainment|media|studios?|label|network|tv)\b""",
                    RegexOption.IGNORE_CASE
                ),
                ""
            )
            // 2. Eliminar elementos decorativos o símbolos redundantes
            .replace(Regex("""[\-|·•~_]+$"""), "")
            .replace(Regex("""^\s*[-|]+\s*"""), "")
            // 3. Normalizar espacios múltiples y recortar
            .replace(Regex("""\s{2,}"""), " ")
            .trim()

        // === NUEVO: CORREGIR INVERSIÓN ARTISTA/TÍTULO ===
        val (tituloCorregido, artistaCorregido) = corregirArtistaTituloInvertidos(tituloLimpio, artistaLimpio)
        tituloLimpio = tituloCorregido
        artistaLimpio = artistaCorregido.toString()

        // Limpiar subtítulos entre paréntesis del título (después de la corrección)
        tituloLimpio = tituloLimpio.replace(Regex("""\s*\([^)]*sub[^)]*\)""", RegexOption.IGNORE_CASE), "")
            .replace(Regex("""\s*\([^)]*traducción[^)]*\)""", RegexOption.IGNORE_CASE), "")
            .replace(Regex("""\s*\([^)]*español[^)]*\)""", RegexOption.IGNORE_CASE), "")
            .replace(Regex("""\s*\([^)]*lyrics[^)]*\)""", RegexOption.IGNORE_CASE), "")
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
    suspend fun sincronizarCancionAlReproducir(cancionConArtista: CancionConArtista): CancionConArtista? =
        withContext(Dispatchers.IO) {
            val cancion = cancionConArtista.cancion

            // === CORREGIR METADATOS EN LA BASE DE DATOS ===
            val (tituloCorregido, artistaCorregido) = corregirArtistaTituloInvertidos(
                cancion.titulo,
                cancionConArtista.artistaNombre
            )

            var cancionActualizada: CancionEntity? = null
            var artistaActualizado: ArtistaEntity? = null

            // Si hay corrección, actualizar la base de datos
            if (cancion.titulo != tituloCorregido || cancionConArtista.artistaNombre != artistaCorregido) {
                Log.d(tag, "🔄 Aplicando corrección en BD: '${cancion.titulo}' -> '$tituloCorregido'")

                // Actualizar canción si el título cambió
                if (cancion.titulo != tituloCorregido) {
                    cancionActualizada = cancion.copy(titulo = tituloCorregido)
                    cancionDao.actualizarCancion(cancionActualizada)
                }

                // Actualizar artista si tenemos su ID y el nombre cambió
                artistaCorregido?.let { nuevoArtista ->
                    if (cancion.idArtista != null && cancionConArtista.artistaNombre != nuevoArtista) {
                        val artistaActual = cancionDao.obtenerArtistaPorId(cancion.idArtista)
                        artistaActual?.let { artista ->
                            artistaActualizado = artista.copy(nombre = nuevoArtista)
                            cancionDao.actualizarArtista(artistaActualizado)
                            Log.d(tag, "🔄 Artista actualizado: '${artista.nombre}' -> '$nuevoArtista'")
                        }
                    }
                }
            }

            // Verificar si ya tenemos datos completos (con los metadatos corregidos)
            if (tieneDatosCompletos(cancion)) {
                Log.d(tag, "✅ Canción '${tituloCorregido}' ya tiene datos completos, omitiendo búsqueda")
                // Devolver versión corregida incluso si ya tiene datos completos
                return@withContext crearCancionConArtistaCorregida(
                    cancionConArtista,
                    tituloCorregido,
                    artistaCorregido
                )
            }

            // Verificar rate limiting
            if (!rateLimiter.tryAcquire()) {
                Log.w(tag, "⏳ Rate limit alcanzado para: ${tituloCorregido}")
                return@withContext crearCancionConArtistaCorregida(
                    cancionConArtista,
                    tituloCorregido,
                    artistaCorregido
                )
            }

            Log.d(tag, "🎵 Sincronizando al reproducir: '$tituloCorregido'")

            val resultadoBusqueda = buscarCancionEnGenius(cancion.titulo, cancionConArtista.artistaNombre)
            if (resultadoBusqueda == null) {
                Log.w(tag, "❌ No se pudo encontrar '$tituloCorregido' en Genius")
                return@withContext crearCancionConArtistaCorregida(
                    cancionConArtista,
                    tituloCorregido,
                    artistaCorregido
                )
            }

            // Actualizar datos básicos de la canción
            val cancionFinal = (cancionActualizada ?: cancion).copy(
                geniusId = resultadoBusqueda.id,
                geniusUrl = resultadoBusqueda.url
            )
            cancionDao.actualizarCancion(cancionFinal)

            // Descargar letra en segundo plano (no bloqueante)
            withContext(Dispatchers.IO) {
                descargarYGuardarLetraMejorada(resultadoBusqueda.url, cancion.idCancion)

                // Sincronizar artista si es necesario (con nombre corregido)
                resultadoBusqueda.primary_artist?.let { artista ->
                    if (cancion.idArtista != null) {
                        sincronizarDatosArtista(artista.id, cancion.idArtista)
                    }
                }

                // Procesar portada
                procesarPortadaCancion(resultadoBusqueda.url, resultadoBusqueda.song_art_image_url, cancionFinal)
            }

            Log.d(tag, "✅ Sincronización completada para: '$tituloCorregido'")

            // Devolver la versión corregida
            return@withContext crearCancionConArtistaCorregida(
                cancionConArtista,
                tituloCorregido,
                artistaCorregido
            )
        }

    private fun crearCancionConArtistaCorregida(
        original: CancionConArtista,
        tituloCorregido: String,
        artistaCorregido: String?
    ): CancionConArtista {
        return original.copy(
            cancion = original.cancion.copy(titulo = tituloCorregido),
            artistaNombre = artistaCorregido ?: original.artistaNombre
        )
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

            // 2. Preprocesar búsqueda (INCLUYE CORRECCIÓN DE INVERSIÓN)
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

            if (scrapedData != null && !scrapedData.lyrics.isNullOrBlank()) {
                // Guardar letra si existe
                val letraEntity = LetraEntity(idCancion = idCancion, letra = scrapedData.lyrics)
                letraDao.insertarLetra(letraEntity)
                Log.d(tag, "💾 Letra guardada (${scrapedData.lyrics.length} caracteres)")

                // Verificar si la letra parece completa
                if (scrapedData.lyrics.length < 100) {
                    Log.w(tag, "⚠️ Letra muy corta, posiblemente incompleta. Intentando método alternativo...")
                    descargarYGuardarLetraLegacyMejorada(url, idCancion)
                }
            } else {
                Log.w(tag, "⚠️ No se pudieron obtener datos via scraping, usando método antiguo")
                descargarYGuardarLetraLegacyMejorada(url, idCancion)
            }

            // Procesar portada y metadata...
            if (scrapedData != null) {
                if (!scrapedData.coverArtUrl.isNullOrBlank()) {
                    val nombreArchivo = "album_cover_${idCancion}.jpg"
                    val portadaPath = descargarYGuardarImagen(scrapedData.coverArtUrl, nombreArchivo)
                    actualizarInformacionAlbumDesdeScraping(idCancion, scrapedData, portadaPath)
                }
                Log.d(tag, "📊 Metadata obtenida - Artista: ${scrapedData.artistName}, Álbum: ${scrapedData.albumName}")
            }
        }

    /**
     * Método legacy como fallback
     */
    private suspend fun descargarYGuardarLetraLegacyMejorada(url: String, idCancion: Int) =
        withContext(Dispatchers.IO) {
            Log.d(tag, "Descargando letra (método legacy MEJORADO) desde: $url")

            try {
                val userAgent = USER_AGENTS.random()
                val doc: Document = Jsoup.connect(url).userAgent(userAgent).timeout(15000).followRedirects(true).get()

                // MÚLTIPLES ESTRATEGIAS para encontrar letras
                val lyricsContent = StringBuilder()

                // Estrategia 1: Contenedor principal de Genius
                val container = doc.select("div[data-lyrics-container='true']").firstOrNull()
                if (container != null) {
                    Log.d(tag, "✅ Encontrado contenedor principal de letras")
                    container.childNodes().forEach { node ->
                        extractTextWithLineBreaks(node, lyricsContent)
                    }
                }

                // Estrategia 2: Fallback a otros selectores comunes
                if (lyricsContent.isEmpty()) {
                    Log.d(tag, "⚠️ Intentando estrategia alternativa de selectores...")
                    val alternativeContainers = doc.select("div.lyrics, section[data-lyrics], .Lyrics__Container")
                    alternativeContainers.forEach { container ->
                        container.childNodes().forEach { node ->
                            extractTextWithLineBreaks(node, lyricsContent)
                        }
                    }
                }

                // Estrategia 3: Extraer todos los párrafos que parecen letras
                if (lyricsContent.isEmpty()) {
                    Log.d(tag, "⚠️ Intentando extracción por párrafos...")
                    val paragraphs = doc.select("p, div, span").filter { element ->
                        val text = element.text().trim()
                        text.length > 20 && text.contains(Regex("""[a-zA-Z]{3,}""")) &&
                                !text.contains(Regex("""\d+\.\d+|\d+:\d+""")) // Excluir timestamps
                    }
                    paragraphs.forEach { paragraph ->
                        lyricsContent.append(paragraph.text().trim()).append("\n\n")
                    }
                }

                val letraFinal = lyricsContent.toString()
                    .replace(Regex("[ \t]{2,}"), " ")
                    .replace(Regex("\n{3,}"), "\n\n")
                    .trim()

                if (letraFinal.isNotBlank()) {
                    val letraEntity = LetraEntity(idCancion = idCancion, letra = letraFinal)
                    letraDao.insertarLetra(letraEntity)
                    Log.d(tag, "💾 Letra guardada (legacy mejorado) - ${letraFinal.length} caracteres")

                    if (letraFinal.length < 100) {
                        Log.w(tag, "❌ Letra aún muy corta, posible bloqueo anti-scraping")
                    }
                } else {
                    Log.w(tag, "❌ No se pudo extraer ninguna letra")
                }

            } catch (e: Exception) {
                Log.e(tag, "❌ Error en método legacy mejorado: ${e.message}")
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
                if (text.isNotEmpty() && text.length > 1) { // Filtrar texto muy corto
                    if (builder.isNotEmpty() && !builder.endsWith("\n") && !builder.endsWith(" ")) {
                        builder.append(" ")
                    }
                    builder.append(text)
                }
            }
            is Element -> {
                when {
                    node.tagName().equals("br", ignoreCase = true) -> {
                        if (builder.isNotEmpty() && !builder.endsWith("\n")) {
                            builder.append("\n")
                        }
                    }
                    node.tagName().equals("p", ignoreCase = true) ||
                            node.hasClass("ReferentFragmentdesktop__Fragment-") -> {
                        if (builder.isNotEmpty() && !builder.endsWith("\n\n")) {
                            builder.append("\n\n")
                        }
                        node.childNodes().forEach { child -> extractTextWithLineBreaks(child, builder) }
                        if (!builder.endsWith("\n\n")) {
                            builder.append("\n\n")
                        }
                    }
                    node.isBlock -> {
                        if (builder.isNotEmpty() && !builder.endsWith("\n")) {
                            builder.append("\n")
                        }
                        node.childNodes().forEach { child -> extractTextWithLineBreaks(child, builder) }
                        if (!builder.endsWith("\n")) {
                            builder.append("\n")
                        }
                    }
                    else -> {
                        node.childNodes().forEach { child -> extractTextWithLineBreaks(child, builder) }
                    }
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