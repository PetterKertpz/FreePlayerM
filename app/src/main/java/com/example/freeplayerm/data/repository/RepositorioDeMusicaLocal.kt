// en: app/src/main/java/com/example/freeplayerm/data/repository/RepositorioDeMusicaLocal.kt
package com.example.freeplayerm.data.repository

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import android.util.Log
import androidx.core.net.toUri
import com.example.freeplayerm.com.example.freeplayerm.data.local.entity.AlbumEntity
import com.example.freeplayerm.com.example.freeplayerm.data.local.entity.ArtistaEntity
import com.example.freeplayerm.com.example.freeplayerm.data.local.entity.CancionEntity
import com.example.freeplayerm.com.example.freeplayerm.data.local.entity.GeneroEntity
import com.example.freeplayerm.data.local.dao.CancionDao
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RepositorioDeMusicaLocal @Inject constructor(
    @param:ApplicationContext private val contexto: Context,
    private val cancionDao: CancionDao
) {
    private val tag = "MusicScanner"
    private val escaneoEnProgreso = AtomicBoolean(false)

    suspend fun escanearYGuardarMusica() {
        if (!escaneoEnProgreso.compareAndSet(false, true)) {
            Log.d(tag, "Se ha intentado iniciar un nuevo escaneo mientras otro estaba en progreso. Se canceló el nuevo.")
            return
        }

        try {
            withContext(Dispatchers.IO) {
                val cursor = contexto.contentResolver.query(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    arrayOf(
                        MediaStore.Audio.Media._ID,
                        MediaStore.Audio.Media.TITLE,
                        MediaStore.Audio.Media.ARTIST,
                        MediaStore.Audio.Media.ALBUM,
                        MediaStore.Audio.Media.ALBUM_ID,
                        MediaStore.Audio.Media.DURATION,
                        MediaStore.Audio.Media.YEAR
                    ),
                    "${MediaStore.Audio.Media.IS_MUSIC} != 0 AND ${MediaStore.Audio.Media.DURATION} >= 30000",
                    null,
                    null
                )

                cursor?.use { c ->
                    Log.d(tag, "Inicio del escaneo. Canciones encontradas en MediaStore: ${c.count}")

                    val idCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                    val tituloCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                    val artistaCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                    val albumCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                    val albumIdCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
                    val duracionCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                    val anioCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.YEAR)

                    while (c.moveToNext()) {
                        val tituloCrudo = (c.getString(tituloCol) ?: "Título Desconocido").trim()
                        try {
                            val idCancionMediaStore = c.getLong(idCol)
                            val uriContenido = ContentUris.withAppendedId(
                                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                                idCancionMediaStore
                            ).toString()

                            if (cancionDao.obtenerCancionPorRuta(uriContenido) == null) {
                                val artistaCrudo = c.getString(artistaCol)
                                val tituloAlbumCrudo = (c.getString(albumCol)?.takeIf { it.isNotBlank() } ?: "Álbum Desconocido").trim()
                                val anio = c.getInt(anioCol)
                                val duracionMs = c.getLong(duracionCol)
                                val albumIdMediaStore = c.getLong(albumIdCol)

                                // NUEVO SISTEMA DE PARSING INTELIGENTE
                                val resultadoParseo = parsearCancionInteligente(tituloCrudo, artistaCrudo)

                                val artistaFinal = resultadoParseo.artista
                                val tituloFinal = resultadoParseo.titulo
                                val versionInfo = resultadoParseo.versionInfo

                                Log.d(tag, "'$tituloCrudo' procesada como -> Artista: '$artistaFinal', Título: '$tituloFinal', Versión: $versionInfo")

                                val artista = obtenerOCrearArtista(artistaFinal)
                                val album = obtenerOCrearAlbum(tituloAlbumCrudo, artista.idArtista, anio, albumIdMediaStore)
                                val genero = obtenerOCrearGenero("Género Desconocido")

                                val cancion = CancionEntity(
                                    idCancion = 0,
                                    idAlbum = album.idAlbum,
                                    idArtista = artista.idArtista,
                                    idGenero = genero.idGenero,
                                    titulo = tituloFinal,
                                    duracionSegundos = (duracionMs / 1000).toInt(),
                                    origen = "LOCAL",
                                    archivoPath = uriContenido,
                                )
                                cancionDao.insertarCancion(cancion)
                            }
                        } catch (e: Exception) {
                            Log.e(tag, "Error al procesar la canción '$tituloCrudo'. Saltando a la siguiente.", e)
                        }
                    }
                    Log.d(tag, "Escaneo finalizado.")
                } ?: Log.w(tag, "El cursor de MediaStore es nulo.")
            }
        } finally {
            escaneoEnProgreso.set(false)
            Log.d(tag, "Cerrojo de escaneo liberado.")
        }
    }

    // NUEVO: SISTEMA COMPLETO DE PARSING INTELIGENTE
    private data class ResultadoParseo(
        val artista: String,
        val titulo: String,
        val versionInfo: String?
    )

    private fun parsearCancionInteligente(tituloCrudo: String, artistaCrudo: String?): ResultadoParseo {
        // Primero, normalizamos el texto
        val textoNormalizado = tituloCrudo
            .replace(Regex("""[|\\/]"""), " - ") // Normalizar separadores
            .replace(Regex("""\s+"""), " ") // Normalizar espacios
            .trim()

        // Verificamos si el artista de metadatos es válido
        val artistaDeMetadatos = if (!artistaCrudo.isNullOrBlank() && artistaCrudo != "<unknown>") {
            limpiarNombreArtista(artistaCrudo.trim())
        } else {
            null
        }

        // Intentamos extraer artista y título del texto
        val resultadoExtraccion = extraerArtistaYTitulo(textoNormalizado, artistaDeMetadatos)

        // Limpiamos y formateamos los resultados
        val artistaLimpio = limpiarNombreArtista(resultadoExtraccion.artista)
        val tituloLimpio = limpiarTituloCancion(resultadoExtraccion.titulo)

        return ResultadoParseo(
            artista = capitalizarNombrePropio(artistaLimpio),
            titulo = capitalizarNombrePropio(tituloLimpio),
            versionInfo = resultadoExtraccion.versionInfo
        )
    }

    private data class ExtraccionResultado(
        val artista: String,
        val titulo: String,
        val versionInfo: String?
    )

    private fun extraerArtistaYTitulo(texto: String, artistaDeMetadatos: String?): ExtraccionResultado {
        // Patrones de separación comunes
        val patronesSeparadores = listOf(
            Regex("""\s+-\s+"""), // " - "
            Regex("""\s+–\s+"""), // " – "
            Regex("""\s+—\s+"""), // " — "
            Regex("""\s+\|\|\s+"""), // " || "
            Regex("""\s+\|\s+"""), // " | "
            Regex("""\s+//\s+"""), // " // "
            Regex("""\s+::\s+"""), // " :: "
            Regex("""\s+–\s+""") // " – " (diferente tipo de guión)
        )

        // Intentar separar por patrones comunes
        for (patron in patronesSeparadores) {
            if (texto.contains(patron)) {
                val partes = texto.split(patron, limit = 2)
                if (partes.size == 2) {
                    val posibleArtista = partes[0].trim()
                    val posibleTitulo = partes[1].trim()

                    // Validar que la separación sea razonable
                    if (esSeparacionValida(posibleArtista, posibleTitulo)) {
                        return procesarPartes(posibleArtista, posibleTitulo, artistaDeMetadatos)
                    }
                }
            }
        }

        // Si no hay separadores claros, usar lógica alternativa
        return cuandoNoHaySeparadorClaro(texto, artistaDeMetadatos)
    }

    private fun esSeparacionValida(artista: String, titulo: String): Boolean {
        // Validar que ninguna parte esté vacía
        if (artista.isBlank() || titulo.isBlank()) return false

        // Validar que el artista no sea demasiado largo para ser un título
        if (artista.length > 50) return false

        // Validar que el título no sea demasiado corto
        if (titulo.length < 2) return false

        return true
    }

    private fun procesarPartes(parte1: String, parte2: String, artistaDeMetadatos: String?): ExtraccionResultado {
        // Determinar cuál parte es artista y cuál es título
        return when {
            // Si tenemos artista de metadatos, usarlo como referencia
            artistaDeMetadatos != null -> {
                val similitud1 = calcularSimilitud(parte1, artistaDeMetadatos)
                val similitud2 = calcularSimilitud(parte2, artistaDeMetadatos)

                if (similitud1 > similitud2 && similitud1 > 0.3) {
                    // parte1 es el artista, parte2 es el título
                    ExtraccionResultado(parte1, extraerTituloDeTexto(parte2), detectarVersion(parte2))
                } else if (similitud2 > 0.3) {
                    // parte2 es el artista, parte1 es el título
                    ExtraccionResultado(parte2, extraerTituloDeTexto(parte1), detectarVersion(parte1))
                } else {
                    // No hay similitud clara, usar heurísticas
                    determinarPorHeuristicas(parte1, parte2, artistaDeMetadatos)
                }
            }
            // Sin artista de metadatos, usar heurísticas
            else -> determinarPorHeuristicas(parte1, parte2, null)
        }
    }

    private fun determinarPorHeuristicas(parte1: String, parte2: String, artistaDeMetadatos: String?): ExtraccionResultado {
        val heuristica1 = calcularProbabilidadArtista(parte1)
        val heuristica2 = calcularProbabilidadArtista(parte2)

        return if (heuristica1 > heuristica2) {
            ExtraccionResultado(parte1, extraerTituloDeTexto(parte2), detectarVersion(parte2))
        } else {
            ExtraccionResultado(parte2, extraerTituloDeTexto(parte1), detectarVersion(parte1))
        }
    }

    private fun calcularProbabilidadArtista(texto: String): Double {
        var probabilidad = 0.0

        // Características de nombres de artista
        if (Regex("""\b(ft|feat|featuring|vs|&|and|con)\b""", RegexOption.IGNORE_CASE).containsMatchIn(texto)) {
            probabilidad += 0.3
        }

        // Patrones de nombres artísticos
        if (Regex("""^\$?[A-Za-z0-9]+\$?$""").containsMatchIn(texto)) {
            probabilidad += 0.2 // Ej: $uicideboy$
        }

        // Longitud típica de artista vs título
        if (texto.length in 2..30) probabilidad += 0.1
        if (texto.length > 50) probabilidad -= 0.2 // Demasiado largo para artista

        // Presencia de palabras comunes en títulos
        if (Regex("""\b(lyric|video|audio|official|remix|cover|slowed|reverb)\b""", RegexOption.IGNORE_CASE).containsMatchIn(texto)) {
            probabilidad -= 0.3
        }

        return probabilidad
    }

    private fun cuandoNoHaySeparadorClaro(texto: String, artistaDeMetadatos: String?): ExtraccionResultado {
        // Si tenemos artista de metadatos, usarlo
        if (artistaDeMetadatos != null) {
            return ExtraccionResultado(
                artista = artistaDeMetadatos,
                titulo = extraerTituloDeTexto(texto),
                versionInfo = detectarVersion(texto)
            )
        }

        // Si no, intentar detectar patrones específicos
        val patronesEspecificos = listOf(
            Regex("""^(.+?)\s*[(\[]\s*(.+?)\s*[)\]]$"""), // "Artista (Título)" o "Título (Artista)"
        )

        for (patron in patronesEspecificos) {
            val match = patron.find(texto)
            if (match != null) {
                val grupo1 = match.groupValues[1].trim()
                val grupo2 = match.groupValues[2].trim()
                return determinarPorHeuristicas(grupo1, grupo2, null)
            }
        }

        // Último recurso: tratar todo como título
        return ExtraccionResultado(
            artista = "Artista Desconocido",
            titulo = extraerTituloDeTexto(texto),
            versionInfo = detectarVersion(texto)
        )
    }

    private fun extraerTituloDeTexto(texto: String): String {
        var titulo = texto

        // Eliminar información de versión
        titulo = eliminarPatronesVersion(titulo)

        // Eliminar extensiones y sufijos comunes
        titulo = eliminarSufijosComunes(titulo)

        return titulo.trim()
    }

    private fun detectarVersion(texto: String): String? {
        val patronesVersion = listOf(
            Regex("""\b(slowed|reverb|sped up|nightcore|revisited)\b""", RegexOption.IGNORE_CASE),
            Regex("""\b(remix|mix|version|edit)\b""", RegexOption.IGNORE_CASE),
            Regex("""\b(cover|live|acoustic|instrumental)\b""", RegexOption.IGNORE_CASE)
        )

        patronesVersion.forEach { patron ->
            val match = patron.find(texto)
            if (match != null) {
                return match.value.lowercase().replaceFirstChar { it.uppercase() }

            }
        }

        return null
    }

    private fun eliminarPatronesVersion(texto: String): String {
        var resultado = texto

        val patronesAEliminar = listOf(
            Regex("""\s*[(\[].*?(slowed|reverb|sped up|nightcore|revisited).*?[)\]]\s*""", RegexOption.IGNORE_CASE),
            Regex("""\s*[(\[].*?(remix|mix|version|edit).*?[)\]]\s*""", RegexOption.IGNORE_CASE),
            Regex("""\s*[(\[].*?(cover|live|acoustic|instrumental).*?[)\]]\s*""", RegexOption.IGNORE_CASE)
        )

        patronesAEliminar.forEach { patron ->
            resultado = patron.replace(resultado, " ")
        }

        return resultado
    }

    private fun limpiarNombreArtista(nombre: String): String {
        var artista = nombre

        // Eliminar sufijos comunes
        val sufijosArtista = listOf(
            Regex("""\s*-?\s*topic\s*$""", RegexOption.IGNORE_CASE),
            Regex("""\s*-?\s*vevo\s*$""", RegexOption.IGNORE_CASE),
            Regex("""\s*-?\s*official\s*$""", RegexOption.IGNORE_CASE),
            Regex("""\s*-?\s*oficial\s*$""", RegexOption.IGNORE_CASE),
            Regex("""\s*-?\s*channel\s*$""", RegexOption.IGNORE_CASE)
        )

        sufijosArtista.forEach { sufijo ->
            artista = sufijo.replace(artista, "")
        }

        return artista.trim()
    }

    private fun limpiarTituloCancion(titulo: String): String {
        var tituloLimpio = titulo

        // Lista completa de expresiones regulares para eliminar
        val patronesAEliminar = listOf(
            // Patrones de metadatos de video/música
            Regex("""\s*[(\[].*?(official|oficial).*?[)\]]\s*""", RegexOption.IGNORE_CASE),
            Regex("""\s*[(\[].*?video.*?[)\]]\s*""", RegexOption.IGNORE_CASE),
            Regex("""\s*[(\[].*?lyric.*?[)\]]\s*""", RegexOption.IGNORE_CASE),
            Regex("""\s*[(\[].*?audio.*?[)\]]\s*""", RegexOption.IGNORE_CASE),
            Regex("""\s*[(\[].*?visualizer.*?[)\]]\s*""", RegexOption.IGNORE_CASE),
            Regex("""\s*[(\[].*?sub.*?español.*?[)\]]\s*""", RegexOption.IGNORE_CASE),
            Regex("""\s*[(\[].*?sub.*?english.*?[)\]]\s*""", RegexOption.IGNORE_CASE),
            Regex("""\s*[(\[].*?letra.*?[)\]]\s*""", RegexOption.IGNORE_CASE),

            // Patrones de calidad/formato
            Regex("""\s*[(\[].*?(hd|hq|4k|1080p|720p).*?[)\]]\s*""", RegexOption.IGNORE_CASE),
            Regex("""\s*[(\[].*?kbps.*?[)\]]\s*""", RegexOption.IGNORE_CASE),

            // Sitios de descarga y conversión
            Regex("""y2meta\.app\s*-\s*""", RegexOption.IGNORE_CASE),
            Regex("""-\(mp3convert\.org\)""", RegexOption.IGNORE_CASE),
            Regex("""\s*\[.*?download.*?]\s*""", RegexOption.IGNORE_CASE),

            // Contenido no musical
            Regex("""\s*\|\s*\d+\s*minutes?\s*of.*""", RegexOption.IGNORE_CASE),
            Regex("""\s*\|\s*imax.*""", RegexOption.IGNORE_CASE),
            Regex("""\s*//.*"""), // Eliminar comentarios después de //
            Regex("""\s*✨.*""") // Eliminar emojis y texto decorativo
        )

        patronesAEliminar.forEach { patron ->
            tituloLimpio = patron.replace(tituloLimpio, " ")
        }

        // Eliminar espacios extra y devolver
        return tituloLimpio.trim().replace(Regex("""\s+"""), " ")
    }

    private fun eliminarSufijosComunes(texto: String): String {
        var resultado = texto

        val sufijos = listOf(
            Regex("""\.(mp3|m4a|wav|flac|aac)$""", RegexOption.IGNORE_CASE),
        )

        sufijos.forEach { sufijo ->
            resultado = sufijo.replace(resultado, "")
        }

        return resultado
    }

    private fun capitalizarNombrePropio(texto: String): String {
        if (texto.isBlank()) return texto

        // Lista de palabras que no deben capitalizarse (excepto si son la primera palabra)
        val palabrasMinusculas = setOf(
            "de", "del", "la", "las", "el", "los", "y", "e", "o", "u",
            "a", "ante", "bajo", "cabe", "con", "contra", "de", "desde",
            "en", "entre", "hacia", "hasta", "para", "por", "según", "sin",
            "so", "sobre", "tras", "vs", "ft", "feat", "featuring", "con"
        )

        return texto.split(" ").mapIndexed { index, palabra ->
            when {
                // Mantener siglas y ciertos patrones en mayúsculas
                palabra.matches(Regex("""^\$?[A-Z0-9]+\$?$""")) -> palabra // Ej: $UICIDEBOY$, BZRP
                palabra.matches(Regex("""^[a-z]+\.[a-z]+$""")) -> palabra // Ej: a.l.e.x
                palabra.matches(Regex("""^#\w+$""")) -> palabra // Ej: #54

                // Primera palabra siempre se capitaliza
                index == 0 -> palabra.lowercase().replaceFirstChar{it.uppercase()}

                // Palabras en la lista de minúsculas
                palabra.lowercase() in palabrasMinusculas -> palabra.lowercase()

                // Demás palabras se capitalizan
                else -> palabra.lowercase().replaceFirstChar{it.uppercase()}
            }
        }.joinToString(" ")
    }

    private fun calcularSimilitud(texto1: String, texto2: String): Double {
        if (texto1.equals(texto2, ignoreCase = true)) return 1.0

        val palabras1 = texto1.lowercase().split(Regex("""\W+""")).toSet()
        val palabras2 = texto2.lowercase().split(Regex("""\W+""")).toSet()

        if (palabras1.isEmpty() || palabras2.isEmpty()) return 0.0

        val interseccion = palabras1.intersect(palabras2).size
        val union = palabras1.union(palabras2).size

        return interseccion.toDouble() / union.toDouble()
    }

    // --- El resto de las funciones 'obtenerOCrear' se mantienen igual ---
    private suspend fun obtenerOCrearArtista(nombre: String): ArtistaEntity {
        val artistaExistente = cancionDao.obtenerArtistaPorNombre(nombre)
        if (artistaExistente != null) {
            return artistaExistente
        }

        cancionDao.insertarArtista(ArtistaEntity(nombre = nombre, paisOrigen = null, descripcion = null))
        return cancionDao.obtenerArtistaPorNombre(nombre)!!
    }

    private suspend fun obtenerOCrearAlbum(titulo: String, artistaId: Int, anio: Int, albumIdMediaStore: Long): AlbumEntity {
        val albumExistente = cancionDao.obtenerAlbumPorNombreYArtista(titulo, artistaId)
        if (albumExistente != null) {
            return albumExistente
        }

        val uriCaratula = ContentUris.withAppendedId(
            "content://media/external/audio/albumart".toUri(),
            albumIdMediaStore
        )

        val nuevoAlbum = AlbumEntity(
            idArtista = artistaId,
            titulo = titulo,
            anio = if (anio > 0) anio else null,
            portadaPath = uriCaratula.toString()
        )

        cancionDao.insertarAlbum(nuevoAlbum)
        return cancionDao.obtenerAlbumPorNombreYArtista(titulo, artistaId)!!
    }

    private suspend fun obtenerOCrearGenero(nombre: String): GeneroEntity {
        val generoExistente = cancionDao.obtenerGeneroPorNombre(nombre)
        if (generoExistente != null) {
            return generoExistente
        }

        cancionDao.insertarGenero(GeneroEntity(nombre = nombre))
        return cancionDao.obtenerGeneroPorNombre(nombre)!!
    }
}