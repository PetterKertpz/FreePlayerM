// en: app/src/main/java/com/example/freeplayerm/data/local/entity/relations/SongWithArtist.kt
package com.example.freeplayerm.data.local.entity.relations

import androidx.room.DatabaseView
import androidx.room.Embedded
import com.example.freeplayerm.data.local.entity.SongEntity

/**
 * 🎵 CANCION CON ARTISTA - DATA CLASS v2.0
 *
 * Clase que representa una canción con toda su información relacionada Incluye datos del artista,
 * álbum, género y estado de favorito
 *
 * Esta clase es el resultado de queries con JOINs múltiples y proporciona todos los datos
 * necesarios para mostrar una canción en la UI sin hacer queries adicionales
 *
 * @version 2.0 - Enhanced
 */
@DatabaseView(
    """
    SELECT 
        c.*,
        a.nombre AS artistaNombre,
        al.titulo AS albumNombre,
        COALESCE(al.portada_path, c.portada_path) AS portadaPath,
        al.anio AS fechaLanzamiento,
        g.nombre AS generoNombre,
        EXISTS(SELECT 1 FROM favoritos f WHERE f.id_cancion = c.id_cancion) AS esFavorita
    FROM canciones c
    LEFT JOIN artistas a ON c.id_artista = a.id_artista
    LEFT JOIN albumes al ON c.id_album = al.id_album
    LEFT JOIN generos g ON c.id_genero = g.id_genero
    """
)
data class SongWithArtist(
    @Embedded val cancion: SongEntity,
    val artistaNombre: String? = "Artista Desconocido",
    val albumNombre: String? = "Álbum Desconocido",
    val portadaPath: String? = null,
    val fechaLanzamiento: String? = null,
    val generoNombre: String? = "Sin Género",
    val esFavorita: Boolean = false,
) {
    /** Obtiene el ID de la canción */
    val id: Int
        get() = cancion.idCancion

    /** Obtiene el título de la canción */
    val titulo: String
        get() = cancion.titulo

    /** Obtiene la duración en segundos */
    val duracionSegundos: Int
        get() = cancion.duracionSegundos

    /** Obtiene la duración formateada */
    fun duracionFormateada(): String = cancion.duracionFormateada()

    /** Obtiene el nombre del artista o "Artista Desconocido" */
    fun obtenerArtista(): String = artistaNombre ?: "Artista Desconocido"

    /** Obtiene el nombre del álbum o "Álbum Desconocido" */
    fun obtenerAlbum(): String = albumNombre ?: "Álbum Desconocido"

    /** Obtiene el género o "Sin Género" */
    fun obtenerGenero(): String = generoNombre ?: "Sin Género"

    /** Obtiene la portada (prioriza portada del álbum, luego la de la canción) */
    fun obtenerPortada(): String? = portadaPath ?: cancion.portadaPath

    /** Verifica si tiene portada disponible */
    fun tienePortada(): Boolean = obtenerPortada() != null

    /** Obtiene el texto completo para búsqueda Combina título, artista, álbum y género */
    fun textoCompleto(): String {
        return buildString {
            append(titulo)
            artistaNombre?.let { append(" $it") }
            albumNombre?.let { append(" $it") }
            generoNombre?.let { append(" $it") }
        }
    }

    /** Obtiene una descripción de una línea Formato: "Artista - Álbum" */
    fun descripcionCorta(): String {
        return buildString {
            append(obtenerArtista())
            if (albumNombre != null) {
                append(" • $albumNombre")
            }
        }
    }

    /** Obtiene una descripción completa Formato: "Título\nArtista\nÁlbum (Año) - Género" */
    fun descripcionCompleta(): String {
        return buildString {
            appendLine(titulo)
            appendLine(obtenerArtista())
            append(obtenerAlbum())
            fechaLanzamiento?.let { append(" ($it)") }
            if (generoNombre != null) {
                append(" • $generoNombre")
            }
        }
    }

    /** Verifica si la canción es local */
    fun esLocal(): Boolean = cancion.esLocal()

    /** Verifica si la canción es remota */
    fun esRemota(): Boolean = cancion.esRemota()

    /** Obtiene las estadísticas de reproducción */
    fun vecesReproducida(): Int = cancion.vecesReproducida

    /** Verifica si tiene letra disponible */
    fun tieneLetra(): Boolean = cancion.letraDisponible

    /** Verifica si ha sido reproducida alguna vez */
    fun fueReproducida(): Boolean = cancion.vecesReproducida > 0

    /** Obtiene la última vez que fue reproducida */
    fun ultimaReproduccion(): Long? = cancion.ultimaReproduccion

    /** Verifica si fue agregada recientemente (últimos 7 días) */
    fun esReciente(): Boolean {
        val diasEnMillis = 7 * 24 * 60 * 60 * 1000L
        return (System.currentTimeMillis() - cancion.fechaAgregado) < diasEnMillis
    }

    /** Crea una copia con el estado de favorito actualizado */
    fun conFavoritoActualizado(nuevoEstado: Boolean): SongWithArtist {
        return copy(esFavorita = nuevoEstado)
    }

    /** Convierte a un formato simple para compartir */
    fun paraCompartir(): String {
        return buildString {
            append("🎵 $titulo")
            artistaNombre?.let { appendLine("\n👤 $it") }
            albumNombre?.let { appendLine("💿 $it") }
            generoNombre?.let { appendLine("🎸 $it") }
        }
    }

    companion object {
        /** Crea una instancia para preview/testing */
        fun preview(
            titulo: String = "Canción de Ejemplo",
            artista: String = "Artista de Ejemplo",
            album: String = "Álbum de Ejemplo",
            genero: String = "Rock",
            duracionSegundos: Int = 180,
            esFavorita: Boolean = false,
        ): SongWithArtist {
            val songEntity =
                SongEntity(
                    idCancion = 1,
                    titulo = titulo,
                    duracionSegundos = duracionSegundos,
                    origen = SongEntity.ORIGEN_LOCAL,
                    archivoPath = "/path/to/song.mp3",
                    idArtista = null, // ✅ Nullable, usar null
                    idAlbum = null, // ✅ Nullable, usar null
                    idGenero = null, // ✅ Nullable, usar null
                )

            return SongWithArtist(
                cancion = songEntity,
                artistaNombre = artista,
                albumNombre = album,
                generoNombre = genero,
                portadaPath = null,
                fechaLanzamiento = "2024",
                esFavorita = esFavorita,
            )
        }
    }
}
