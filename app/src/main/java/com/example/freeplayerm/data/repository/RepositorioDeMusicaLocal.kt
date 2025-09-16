// en: app/src/main/java/com/example/freeplayerm/data/repository/RepositorioDeMusicaLocal.kt
package com.example.freeplayerm.data.repository

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import android.util.Log
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

        // --- CAMBIO CLAVE #3: USAR 'finally' PARA LIBERAR EL CERROJO ---
        // El bloque 'finally' se ejecuta SIEMPRE al final, tanto si el 'try' tiene éxito
        // como si falla por una excepción. Esto garantiza que nuestro cerrojo siempre se libere.
        try {
            withContext(Dispatchers.IO) {
                val cursor = contexto.contentResolver.query(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    arrayOf(
                        MediaStore.Audio.Media._ID,
                        MediaStore.Audio.Media.TITLE,
                        MediaStore.Audio.Media.ARTIST,
                        MediaStore.Audio.Media.ALBUM,
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
                                // --- CAMBIO #1: LÓGICA DE PROCESAMIENTO INTELIGENTE ---

                                val artistaCrudo = c.getString(artistaCol)
                                val tituloAlbumCrudo = (c.getString(albumCol)?.takeIf { it.isNotBlank() } ?: "Álbum Desconocido").trim()
                                val anio = c.getInt(anioCol)
                                val duracionMs = c.getLong(duracionCol)

                                var artistaFinal: String
                                var tituloFinal: String

                                // Verificamos si el metadato del artista es inválido o genérico.
                                if (artistaCrudo.isNullOrBlank() || artistaCrudo == "<unknown>") {
                                    Log.w(tag, "Metadato de artista ausente para '$tituloCrudo'. Intentando parsear desde el título.")
                                    // Si es inválido, intentamos adivinar desde el título.
                                    val (artistaParseado, tituloParseado) = parsearTituloYArtista(tituloCrudo)
                                    artistaFinal = artistaParseado
                                    tituloFinal = tituloParseado
                                } else {
                                    // Si el metadato es válido, lo usamos.
                                    artistaFinal = artistaCrudo.trim()
                                    tituloFinal = tituloCrudo
                                }

                                val tituloLimpio = limpiarTitulo(tituloFinal)

                                Log.d(tag, "'$tituloCrudo' es nueva. Procesada como -> Artista: '$artistaFinal', Título: '$tituloLimpio'.")

                                val artista = obtenerOCrearArtista(artistaFinal)
                                val album = obtenerOCrearAlbum(tituloAlbumCrudo, artista.idArtista, anio)
                                val genero = obtenerOCrearGenero("Género Desconocido")

                                val cancion = CancionEntity(
                                    idCancion = 0,
                                    idAlbum = album.idAlbum,
                                    idArtista = artista.idArtista,
                                    idGenero = genero.idGenero,
                                    titulo = tituloLimpio, // <-- Usamos el título ya limpio
                                    duracionSegundos = (duracionMs / 1000).toInt(),
                                    portadaUrl = null,
                                    origen = "LOCAL",
                                    archivoPath = uriContenido
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

    private fun parsearTituloYArtista(texto: String): Pair<String, String> {
        val separadores = listOf(" - ", " – ", " — ")
        for (separador in separadores) {
            if (texto.contains(separador)) {
                val partes = texto.split(separador, limit = 2)
                val artista = partes[0].trim()
                val titulo = limpiarTitulo(partes[1].trim()) // <-- Limpiamos el título aquí también
                if (artista.isNotBlank() && titulo.isNotBlank() && artista.length < 40) {
                    return Pair(artista, titulo)
                }
            }
        }
        return Pair("Artista Desconocido", limpiarTitulo(texto))
    }


    private fun limpiarTitulo(texto: String): String {
        var tituloLimpio = texto

        // Lista de expresiones regulares para eliminar patrones comunes
        val patronesAEliminar = listOf(
            Regex("""\s*[\(\[].*?oficial.*?[\)\]]\s*""", RegexOption.IGNORE_CASE), // (Official Video), [official]
            Regex("""\s*[\(\[].*?video.*?[\)\]]\s*""", RegexOption.IGNORE_CASE), // (Video), [Music Video]
            Regex("""\s*[\(\[].*?lyrics.*?[\)\]]\s*""", RegexOption.IGNORE_CASE), // (Lyrics), [Lyric Video]
            Regex("""\s*[\(\[].*?audio.*?[\)\]]\s*""", RegexOption.IGNORE_CASE), // (Official Audio)
            Regex("""\s*[\(\[].*?visualizer.*?[\)\]]\s*""", RegexOption.IGNORE_CASE), // (Visualizer)
            Regex("""\s*[\(\[].*?sub español.*?[\)\]]\s*""", RegexOption.IGNORE_CASE), // (sub español)
            Regex("""\s*[\(\[].*?hd.*?[\)\]]\s*""", RegexOption.IGNORE_CASE), // (HD), (HQ)
            Regex("""\s*[\(\[].*?kbps.*?[\)\]]\s*""", RegexOption.IGNORE_CASE), // (128 kbps), (320kbps)
            Regex("""y2meta\.app\s*-\s*""", RegexOption.IGNORE_CASE), // Prefijo de sitios de descarga
            Regex("""-\(mp3convert\.org\)""", RegexOption.IGNORE_CASE) // Sufijo de sitios de descarga
        )

        patronesAEliminar.forEach { patron ->
            tituloLimpio = patron.replace(tituloLimpio, " ")
        }

        // Finalmente, eliminamos espacios extra y devolvemos el resultado.
        return tituloLimpio.trim()
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

    private suspend fun obtenerOCrearAlbum(titulo: String, artistaId: Int, anio: Int): AlbumEntity {
        val albumExistente = cancionDao.obtenerAlbumPorNombreYArtista(titulo, artistaId)
        if (albumExistente != null) {
            return albumExistente
        }

        cancionDao.insertarAlbum(AlbumEntity(idArtista = artistaId, titulo = titulo, anio = if (anio > 0) anio else null, portadaUrl = null))
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