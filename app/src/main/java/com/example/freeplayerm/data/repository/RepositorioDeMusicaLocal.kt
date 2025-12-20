// en: app/src/main/java/com/example/freeplayerm/data/repository/RepositorioDeMusicaLocal.kt
package com.example.freeplayerm.data.repository

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import android.util.Log
import androidx.core.net.toUri
import com.example.freeplayerm.data.local.entity.AlbumEntity
import com.example.freeplayerm.data.local.entity.ArtistaEntity
import com.example.freeplayerm.data.local.entity.CancionEntity
import com.example.freeplayerm.data.local.entity.GeneroEntity
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
                        MediaStore.Audio.Media.YEAR,
                        MediaStore.Audio.Media.TRACK
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
                    val pistaCol = c.getColumnIndex(MediaStore.Audio.Media.TRACK)

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
                                val numeroPista = if (pistaCol >= 0) c.getInt(pistaCol) else null

                                val resultadoParseo = parsearCancionInteligente(tituloCrudo, artistaCrudo)

                                val artistaFinal = resultadoParseo.artista
                                val tituloFinal = resultadoParseo.titulo
                                val versionInfo = resultadoParseo.versionInfo

                                Log.d(tag, "'$tituloCrudo' procesada como -> Artista: '$artistaFinal', Título: '$tituloFinal', Versión: $versionInfo")

                                val artista = obtenerOCrearArtista(artistaFinal)
                                val album = obtenerOCrearAlbum(tituloAlbumCrudo, artista.idArtista, anio, albumIdMediaStore)
                                val genero = obtenerOCrearGenero("Género Desconocido")

                                val cancion = CancionEntity(
                                    idArtista = artista.idArtista,
                                    idAlbum = album.idAlbum,
                                    idGenero = genero.idGenero,
                                    titulo = tituloFinal,
                                    duracionSegundos = (duracionMs / 1000).toInt(),
                                    origen = CancionEntity.ORIGEN_LOCAL,
                                    archivoPath = uriContenido,
                                    numeroPista = numeroPista,
                                    anio = if (anio > 0) anio else null,
                                    fechaAgregado = System.currentTimeMillis()
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

    private data class ResultadoParseo(
        val artista: String,
        val titulo: String,
        val versionInfo: String?
    )

    private fun parsearCancionInteligente(tituloCrudo: String, artistaCrudo: String?): ResultadoParseo {
        val textoNormalizado = tituloCrudo
            .replace(Regex("""[|\\/]"""), " - ")
            .replace(Regex("""\s+"""), " ")
            .trim()

        val artistaDeMetadatos = if (!artistaCrudo.isNullOrBlank() && artistaCrudo != "<unknown>") {
            limpiarNombreArtista(artistaCrudo.trim())
        } else {
            null
        }

        val resultadoExtraccion = extraerArtistaYTitulo(textoNormalizado, artistaDeMetadatos)

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
        val patronesSeparadores = listOf(
            Regex("""\s+-\s+"""),
            Regex("""\s+–\s+"""),
            Regex("""\s+—\s+"""),
            Regex("""\s+\|\|\s+"""),
            Regex("""\s+\|\s+"""),
            Regex("""\s+//\s+"""),
            Regex("""\s+::\s+"""),
            Regex("""\s+–\s+""")
        )

        for (patron in patronesSeparadores) {
            if (texto.contains(patron)) {
                val partes = texto.split(patron, limit = 2)
                if (partes.size == 2) {
                    val posibleArtista = partes[0].trim()
                    val posibleTitulo = partes[1].trim()

                    val confianzaIzquierda = calcularConfianzaArtista(posibleArtista, artistaDeMetadatos)
                    val confianzaDerecha = calcularConfianzaArtista(posibleTitulo, artistaDeMetadatos)

                    return if (confianzaIzquierda > confianzaDerecha || confianzaIzquierda > 0.6) {
                        val (tituloLimpio, versionInfo) = separarTituloYVersion(posibleTitulo)
                        ExtraccionResultado(posibleArtista, tituloLimpio, versionInfo)
                    } else {
                        val (tituloLimpio, versionInfo) = separarTituloYVersion(posibleArtista)
                        ExtraccionResultado(posibleTitulo, tituloLimpio, versionInfo)
                    }
                }
            }
        }

        if (artistaDeMetadatos != null) {
            var resto = texto
            patronesSeparadores.forEach { patron ->
                resto = patron.replace(resto, " ")
            }
            val (tituloLimpio, versionInfo) = separarTituloYVersion(resto.trim())
            return ExtraccionResultado(artistaDeMetadatos, tituloLimpio, versionInfo)
        }

        val (tituloLimpio, versionInfo) = separarTituloYVersion(texto)
        return ExtraccionResultado("Artista Desconocido", tituloLimpio, versionInfo)
    }

    private fun calcularConfianzaArtista(textoCandidata: String, artistaReferencia: String?): Double {
        if (artistaReferencia == null) return 0.3

        val similitud = calcularSimilitud(textoCandidata, artistaReferencia)

        if (similitud > 0.8) return 1.0
        if (similitud > 0.5) return 0.7
        if (similitud > 0.3) return 0.4

        val palabrasArtista = artistaReferencia.lowercase().split(Regex("""\W+"""))
        val palabrasTexto = textoCandidata.lowercase().split(Regex("""\W+"""))

        if (palabrasArtista.any { palabra -> palabra in palabrasTexto && palabra.length > 3 }) {
            return 0.6
        }

        return 0.2
    }

    private fun separarTituloYVersion(texto: String): Pair<String, String?> {
        val patronesVersion = listOf(
            Regex("""\s*[(\[].*?(slowed|reverb|sped up|nightcore|revisited).*?[)\]]\s*$""", RegexOption.IGNORE_CASE),
            Regex("""\s*[(\[].*?(remix|mix|version|edit).*?[)\]]\s*$""", RegexOption.IGNORE_CASE),
            Regex("""\s*[(\[].*?(cover|live|acoustic|instrumental).*?[)\]]\s*$""", RegexOption.IGNORE_CASE),
            Regex("""\s*[(\[].*?(remaster|remastered).*?[)\]]\s*$""", RegexOption.IGNORE_CASE),
            Regex("""\s*-\s*(slowed|reverb|sped up|nightcore)\s*$""", RegexOption.IGNORE_CASE),
            Regex("""\s*-\s*(remix|mix|version|edit)\s*$""", RegexOption.IGNORE_CASE)
        )

        for (patron in patronesVersion) {
            val match = patron.find(texto)
            if (match != null) {
                val tituloBase = texto.substring(0, match.range.first).trim()
                val versionInfo = match.value.trim()
                return Pair(tituloBase, versionInfo)
            }
        }

        return Pair(texto, null)
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

        val patronesAEliminar = listOf(
            Regex("""\s*[(\[].*?(official|oficial).*?[)\]]\s*""", RegexOption.IGNORE_CASE),
            Regex("""\s*[(\[].*?video.*?[)\]]\s*""", RegexOption.IGNORE_CASE),
            Regex("""\s*[(\[].*?lyric.*?[)\]]\s*""", RegexOption.IGNORE_CASE),
            Regex("""\s*[(\[].*?audio.*?[)\]]\s*""", RegexOption.IGNORE_CASE),
            Regex("""\s*[(\[].*?visualizer.*?[)\]]\s*""", RegexOption.IGNORE_CASE),
            Regex("""\s*[(\[].*?sub.*?español.*?[)\]]\s*""", RegexOption.IGNORE_CASE),
            Regex("""\s*[(\[].*?sub.*?english.*?[)\]]\s*""", RegexOption.IGNORE_CASE),
            Regex("""\s*[(\[].*?letra.*?[)\]]\s*""", RegexOption.IGNORE_CASE),
            Regex("""\s*[(\[].*?(hd|hq|4k|1080p|720p).*?[)\]]\s*""", RegexOption.IGNORE_CASE),
            Regex("""\s*[(\[].*?kbps.*?[)\]]\s*""", RegexOption.IGNORE_CASE),
            Regex("""y2meta\.app\s*-\s*""", RegexOption.IGNORE_CASE),
            Regex("""-\(mp3convert\.org\)""", RegexOption.IGNORE_CASE),
            Regex("""\s*\[.*?download.*?]\s*""", RegexOption.IGNORE_CASE),
            Regex("""\s*\|\s*\d+\s*minutes?\s*of.*""", RegexOption.IGNORE_CASE),
            Regex("""\s*\|\s*imax.*""", RegexOption.IGNORE_CASE),
            Regex("""\s*//.*"""),
            Regex("""\s*✨.*""")
        )

        patronesAEliminar.forEach { patron ->
            tituloLimpio = patron.replace(tituloLimpio, " ")
        }

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

        val palabrasMinusculas = setOf(
            "de", "del", "la", "las", "el", "los", "y", "e", "o", "u",
            "a", "ante", "bajo", "cabe", "con", "contra", "de", "desde",
            "en", "entre", "hacia", "hasta", "para", "por", "según", "sin",
            "so", "sobre", "tras", "vs", "ft", "feat", "featuring", "con"
        )

        return texto.split(" ").mapIndexed { index, palabra ->
            when {
                palabra.matches(Regex("""^\$?[A-Z0-9]+\$?$""")) -> palabra
                palabra.matches(Regex("""^[a-z]+\.[a-z]+$""")) -> palabra
                palabra.matches(Regex("""^#\w+$""")) -> palabra
                index == 0 -> palabra.lowercase().replaceFirstChar{it.uppercase()}
                palabra.lowercase() in palabrasMinusculas -> palabra.lowercase()
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

    private suspend fun obtenerOCrearArtista(nombre: String): ArtistaEntity {
        val artistaExistente = cancionDao.obtenerArtistaPorNombre(nombre)
        if (artistaExistente != null) {
            return artistaExistente
        }

        cancionDao.insertarArtista(ArtistaEntity(nombre = nombre, paisOrigen = null, descripcion = null))
        return cancionDao.obtenerArtistaPorNombre(nombre)!!
    }

    private suspend fun obtenerOCrearAlbum(titulo: String, artistaId: Int, anio: Int, albumIdMediaStore: Long): AlbumEntity {
        val albumExistente = cancionDao.obtenerAlbumPorNombreYArtista(titulo, artistaId.toLong())
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
        return cancionDao.obtenerAlbumPorNombreYArtista(titulo, artistaId.toLong())!!
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