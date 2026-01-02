package com.example.freeplayerm.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entidad para almacenar redes sociales de artistas de forma normalizada.
 *
 * Reemplaza los campos hardcodeados (instagram, twitter, facebook, youtube) de ArtistEntity con una
 * tabla escalable.
 *
 * Relaciones:
 * - N:1 con ArtistEntity (un artista puede tener múltiples redes sociales)
 *
 * Casos de uso:
 * - Mostrar enlaces a redes sociales del artista
 * - Agregar nuevas redes sin modificar la entidad Artista
 * - Estadísticas de seguidores por plataforma
 *
 * @property idRedSocial ID único de la red social
 * @property idArtista ID del artista (foreign key)
 * @property plataforma Nombre de la plataforma (INSTAGRAM, TWITTER, etc.)
 * @property username Username/handle del artista en esa plataforma
 * @property url URL completa al perfil
 * @property verificada Si la cuenta está verificada en la plataforma
 * @property seguidores Cantidad de seguidores (si está disponible)
 * @property activa Si el enlace sigue siendo válido
 * @property principal Si es la red social principal del artista
 * @property fechaVerificacion Última vez que se verificó el enlace
 * @property fechaActualizacionStats Última actualización de estadísticas
 */
@Entity(
    tableName = "redes_sociales_artista",
    foreignKeys =
        [
            ForeignKey(
                entity = ArtistEntity::class,
                parentColumns = ["id_artista"],
                childColumns = ["id_artista"],
                onDelete = ForeignKey.CASCADE,
                onUpdate = ForeignKey.CASCADE,
            )
        ],
    indices =
        [
            Index(value = ["id_artista"]),
            Index(value = ["plataforma"]),
            Index(value = ["id_artista", "plataforma"], unique = true), // Una cuenta por plataforma
            Index(value = ["verificada"]),
            Index(value = ["seguidores"]),
        ],
)
data class ArtistSocialLinksEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id_red_social") val idRedSocial: Int = 0,

    // ==================== RELACIÓN CON ARTISTA ====================

    @ColumnInfo(name = "id_artista") val idArtista: Int,

    // ==================== INFORMACIÓN DE LA PLATAFORMA ====================

    /**
     * Plataforma de red social Valores: INSTAGRAM, TWITTER, FACEBOOK, YOUTUBE, TIKTOK, SPOTIFY,
     * SOUNDCLOUD, APPLE_MUSIC, GENIUS, WEBSITE, etc.
     */
    @ColumnInfo(name = "plataforma") val plataforma: String,

    /** Username o handle del artista Ejemplo: "@badgyal" (puede incluir @ o no) */
    @ColumnInfo(name = "username") val username: String,

    /** URL completa al perfil */
    @ColumnInfo(name = "url") val url: String,

    // ==================== VERIFICACIÓN Y ESTADÍSTICAS ====================

    /** Si la cuenta está verificada (badge azul/dorado) */
    @ColumnInfo(name = "verificada") val verificada: Boolean = false,

    /** Cantidad de seguidores/suscriptores null si no está disponible */
    @ColumnInfo(name = "seguidores") val seguidores: Int? = null,

    /** Cantidad de publicaciones/posts */
    @ColumnInfo(name = "publicaciones") val publicaciones: Int? = null,

    // ==================== CONTROL ====================

    /** Si el enlace sigue siendo válido Se marca como false si se detecta 404 o cuenta eliminada */
    @ColumnInfo(name = "activa") val activa: Boolean = true,

    /** Si es la red social principal del artista Útil para mostrar primero o destacar */
    @ColumnInfo(name = "principal") val principal: Boolean = false,

    /** Orden de visualización (menor = más importante) */
    @ColumnInfo(name = "orden") val orden: Int = 999,

    // ==================== METADATOS ====================

    /** Última vez que se verificó que el enlace funciona */
    @ColumnInfo(name = "fecha_verificacion")
    val fechaVerificacion: Long = System.currentTimeMillis(),

    /** Última actualización de estadísticas (seguidores, publicaciones) */
    @ColumnInfo(name = "fecha_actualizacion_stats") val fechaActualizacionStats: Long? = null,
    @ColumnInfo(name = "fecha_creacion") val fechaCreacion: Long = System.currentTimeMillis(),

    /** Notas adicionales sobre esta red social */
    @ColumnInfo(name = "notas") val notas: String? = null,
) {
    companion object {
        /** Plataformas soportadas con sus configuraciones */
        object Plataforma {
            const val INSTAGRAM = "INSTAGRAM"
            const val TWITTER = "TWITTER"
            const val FACEBOOK = "FACEBOOK"
            const val YOUTUBE = "YOUTUBE"
            const val TIKTOK = "TIKTOK"
            const val SPOTIFY = "SPOTIFY"
            const val APPLE_MUSIC = "APPLE_MUSIC"
            const val SOUNDCLOUD = "SOUNDCLOUD"
            const val GENIUS = "GENIUS"
            const val BANDCAMP = "BANDCAMP"
            const val WEBSITE = "WEBSITE"
            const val WIKIPEDIA = "WIKIPEDIA"
            const val DEEZER = "DEEZER"
            const val AMAZON_MUSIC = "AMAZON_MUSIC"
            const val TWITCH = "TWITCH"
            const val DISCORD = "DISCORD"
            const val THREADS = "THREADS"
            const val BLUESKY = "BLUESKY"

            /** Obtiene el nombre para mostrar de una plataforma */
            fun obtenerNombreDisplay(plataforma: String): String {
                return when (plataforma) {
                    INSTAGRAM -> "Instagram"
                    TWITTER -> "X (Twitter)"
                    FACEBOOK -> "Facebook"
                    YOUTUBE -> "YouTube"
                    TIKTOK -> "TikTok"
                    SPOTIFY -> "Spotify"
                    APPLE_MUSIC -> "Apple Music"
                    SOUNDCLOUD -> "SoundCloud"
                    GENIUS -> "Genius"
                    BANDCAMP -> "Bandcamp"
                    WEBSITE -> "Sitio Web"
                    WIKIPEDIA -> "Wikipedia"
                    DEEZER -> "Deezer"
                    AMAZON_MUSIC -> "Amazon Music"
                    TWITCH -> "Twitch"
                    DISCORD -> "Discord"
                    THREADS -> "Threads"
                    BLUESKY -> "Bluesky"
                    else -> plataforma.lowercase().replaceFirstChar { it.uppercase() }
                }
            }

            /** Obtiene el icono (emoji o nombre de recurso) para una plataforma */
            fun obtenerIcono(plataforma: String): String {
                return when (plataforma) {
                    INSTAGRAM -> "📷"
                    TWITTER -> "🐦"
                    FACEBOOK -> "👥"
                    YOUTUBE -> "▶️"
                    TIKTOK -> "🎵"
                    SPOTIFY -> "🎧"
                    APPLE_MUSIC -> "🍎"
                    SOUNDCLOUD -> "☁️"
                    GENIUS -> "🧠"
                    BANDCAMP -> "🎸"
                    WEBSITE -> "🌐"
                    WIKIPEDIA -> "📖"
                    TWITCH -> "🎮"
                    DISCORD -> "💬"
                    else -> "🔗"
                }
            }

            /** Obtiene el orden de prioridad por defecto */
            fun obtenerOrdenPrioridad(plataforma: String): Int {
                return when (plataforma) {
                    INSTAGRAM -> 1
                    SPOTIFY -> 2
                    YOUTUBE -> 3
                    TWITTER -> 4
                    TIKTOK -> 5
                    FACEBOOK -> 6
                    APPLE_MUSIC -> 7
                    SOUNDCLOUD -> 8
                    GENIUS -> 9
                    WEBSITE -> 10
                    else -> 99
                }
            }

            /** Construye URL base para una plataforma */
            fun construirUrl(plataforma: String, username: String): String {
                val cleanUsername = username.removePrefix("@").trim()
                return when (plataforma) {
                    INSTAGRAM -> "https://instagram.com/$cleanUsername"
                    TWITTER -> "https://twitter.com/$cleanUsername"
                    FACEBOOK -> "https://facebook.com/$cleanUsername"
                    YOUTUBE -> "https://youtube.com/$cleanUsername"
                    TIKTOK -> "https://tiktok.com/@$cleanUsername"
                    SPOTIFY -> "https://open.spotify.com/artist/$cleanUsername"
                    SOUNDCLOUD -> "https://soundcloud.com/$cleanUsername"
                    GENIUS -> "https://genius.com/artists/$cleanUsername"
                    BANDCAMP -> "https://$cleanUsername.bandcamp.com"
                    APPLE_MUSIC -> "https://music.apple.com/artist/$cleanUsername"
                    TWITCH -> "https://twitch.tv/$cleanUsername"
                    THREADS -> "https://threads.net/@$cleanUsername"
                    BLUESKY -> "https://bsky.app/profile/$cleanUsername"
                    else -> username // Asumir que ya es URL completa
                }
            }
        }

        /** Valida que una URL sea correcta para la plataforma */
        fun validarUrl(plataforma: String, url: String): Boolean {
            return when (plataforma) {
                Plataforma.INSTAGRAM -> url.contains("instagram.com")
                Plataforma.TWITTER -> url.contains("twitter.com") || url.contains("x.com")
                Plataforma.FACEBOOK -> url.contains("facebook.com")
                Plataforma.YOUTUBE -> url.contains("youtube.com")
                Plataforma.TIKTOK -> url.contains("tiktok.com")
                Plataforma.SPOTIFY -> url.contains("spotify.com")
                Plataforma.WEBSITE -> url.startsWith("http")
                else -> true // Aceptar otras URLs
            }
        }
    }

    /** Indica si la red social es válida y activa */
    fun esValida(): Boolean {
        return activa && validarUrl(plataforma, url)
    }

    /** Indica si necesita verificación (más de 7 días sin verificar) */
    fun necesitaVerificacion(): Boolean {
        val sieteDias = 7L * 24 * 60 * 60 * 1000
        return System.currentTimeMillis().toInt() - fechaVerificacion > sieteDias
    }

    /** Indica si es una plataforma de streaming */
    fun esPlataformaStreaming(): Boolean {
        return plataforma in
            listOf(
                Plataforma.SPOTIFY,
                Plataforma.APPLE_MUSIC,
                Plataforma.SOUNDCLOUD,
                Plataforma.YOUTUBE,
                Plataforma.DEEZER,
                Plataforma.AMAZON_MUSIC,
            )
    }

    /** Indica si es una red social (vs plataforma de música) */
    fun esRedSocial(): Boolean {
        return plataforma in
            listOf(
                Plataforma.INSTAGRAM,
                Plataforma.TWITTER,
                Plataforma.FACEBOOK,
                Plataforma.TIKTOK,
                Plataforma.THREADS,
                Plataforma.BLUESKY,
            )
    }

    /** Obtiene el nombre para mostrar */
    fun obtenerNombreDisplay(): String {
        return Plataforma.obtenerNombreDisplay(plataforma)
    }

    /** Obtiene el icono */
    fun obtenerIcono(): String {
        return Plataforma.obtenerIcono(plataforma)
    }

    /** Formatea la cantidad de seguidores de forma legible */
    fun formatearSeguidores(): String? {
        return seguidores?.let { count ->
            when {
                count >= 1_000_000 -> "${count / 1_000_000}M"
                count >= 1_000 -> "${count / 1_000}K"
                else -> count.toString()
            }
        }
    }

    /** Copia marcando como verificada */
    fun marcarComoVerificada(): ArtistSocialLinksEntity {
        return copy(fechaVerificacion = System.currentTimeMillis(), activa = true)
    }

    /** Copia actualizando estadísticas */
    fun actualizarEstadisticas(
        nuevosSeguidores: Int?,
        nuevasPublicaciones: Int?,
    ): ArtistSocialLinksEntity {
        return copy(
            seguidores = nuevosSeguidores,
            publicaciones = nuevasPublicaciones,
            fechaActualizacionStats = System.currentTimeMillis(),
        )
    }
}
