package com.example.freeplayerm.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.freeplayerm.data.remote.genius.dto.SongDetails


/**
 * 🔗 MEDIA LINKS ENTITY
 *
 * Almacena enlaces externos a plataformas de música/video
 * para canciones, álbumes y artistas.
 *
 * Casos de uso:
 * - Abrir canción en Spotify/YouTube desde la app
 * - Sincronizar con servicios de streaming
 * - Verificar disponibilidad en plataformas
 */
@Entity(
    tableName = "media_links",
    foreignKeys = [
        ForeignKey(
            entity = CancionEntity::class,
            parentColumns = ["id_cancion"],
            childColumns = ["id_cancion"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = AlbumEntity::class,
            parentColumns = ["id_album"],
            childColumns = ["id_album"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ArtistaEntity::class,
            parentColumns = ["id_artista"],
            childColumns = ["id_artista"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["id_cancion"]),
        Index(value = ["id_album"]),
        Index(value = ["id_artista"]),
        Index(value = ["plataforma"]),
        Index(value = ["tipo_recurso", "id_recurso", "plataforma"], unique = true)
    ]
)
data class MediaLinksEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_link")
    val idLink: Int = 0,

    // Relación polimórfica (puede ser canción, álbum o artista)
    @ColumnInfo(name = "tipo_recurso")
    val tipoRecurso: String, // CANCION, ALBUM, ARTISTA

    @ColumnInfo(name = "id_recurso")
    val idRecurso: Int, // ID de la entidad relacionada

    // Compatibilidad con FKs específicas
    @ColumnInfo(name = "id_cancion")
    val idCancion: Int? = null,

    @ColumnInfo(name = "id_album")
    val idAlbum: Int? = null,

    @ColumnInfo(name = "id_artista")
    val idArtista: Int? = null,

    // ==================== INFORMACIÓN DEL LINK ====================

    @ColumnInfo(name = "plataforma")
    val plataforma: String, // SPOTIFY, YOUTUBE, APPLE_MUSIC, etc.

    @ColumnInfo(name = "url")
    val url: String,

    @ColumnInfo(name = "external_id")
    val externalId: String? = null, // ID en la plataforma externa

    @ColumnInfo(name = "tipo_link")
    val tipoLink: String = "STREAMING", // STREAMING, VIDEO, SOCIAL, PURCHASE

    // ==================== METADATOS ====================

    @ColumnInfo(name = "disponible")
    val disponible: Boolean = true,

    @ColumnInfo(name = "fecha_verificacion")
    val fechaVerificacion: Int = System.currentTimeMillis().toInt(),

    @ColumnInfo(name = "fecha_creacion")
    val fechaCreacion: Int = System.currentTimeMillis().toInt()
) {
    companion object {
        const val TIPO_CANCION = "CANCION"
        const val TIPO_ALBUM = "ALBUM"
        const val TIPO_ARTISTA = "ARTISTA"

        const val PLATAFORMA_SPOTIFY = "SPOTIFY"
        const val PLATAFORMA_YOUTUBE = "YOUTUBE"
        const val PLATAFORMA_APPLE_MUSIC = "APPLE_MUSIC"
        const val PLATAFORMA_SOUNDCLOUD = "SOUNDCLOUD"
        const val PLATAFORMA_DEEZER = "DEEZER"

        fun fromGeniusSong(cancionId: Int, songDetails: SongDetails): List<MediaLinksEntity> {
            val links = mutableListOf<MediaLinksEntity>()

            songDetails.youtubeUrl?.let {
                links.add(MediaLinksEntity(
                    tipoRecurso = TIPO_CANCION,
                    idRecurso = cancionId,
                    idCancion = cancionId,
                    plataforma = PLATAFORMA_YOUTUBE,
                    url = it,
                    tipoLink = "VIDEO"
                ))
            }

            songDetails.spotifyUuid?.let {
                links.add(MediaLinksEntity(
                    tipoRecurso = TIPO_CANCION,
                    idRecurso = cancionId,
                    idCancion = cancionId,
                    plataforma = PLATAFORMA_SPOTIFY,
                    url = "https://open.spotify.com/track/$it",
                    externalId = it,
                    tipoLink = "STREAMING"
                ))
            }

            songDetails.appleMusicPlayerUrl?.let {
                links.add(MediaLinksEntity(
                    tipoRecurso = TIPO_CANCION,
                    idRecurso = cancionId,
                    idCancion = cancionId,
                    plataforma = PLATAFORMA_APPLE_MUSIC,
                    url = it,
                    tipoLink = "STREAMING"
                ))
            }

            return links
        }
    }
}