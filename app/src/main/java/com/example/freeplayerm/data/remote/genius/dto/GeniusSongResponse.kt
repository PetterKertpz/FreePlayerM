// app/src/main/java/com/example/freeplayerm/data/remote/genius/dto/GeniusSongResponse.kt
package com.example.freeplayerm.data.remote.genius.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * 🎵 GENIUS SONG RESPONSE
 *
 * DTO completo para endpoint /songs/{id} de Genius API
 * Captura TODOS los datos disponibles según documentación oficial
 */

@JsonClass(generateAdapter = true)
data class GeniusSongResponse(
    @Json(name = "meta") val meta: MetaInfo,
    @Json(name = "response") val response: SongResponseData
)

@JsonClass(generateAdapter = true)
data class SongResponseData(
    @Json(name = "song") val song: SongDetails
)

@JsonClass(generateAdapter = true)
data class SongDetails(
    // ==================== IDENTIFICADORES ====================
    @Json(name = "id") val id: String,
    @Json(name = "api_path") val apiPath: String,
    @Json(name = "url") val url: String,

    // ==================== INFORMACIÓN BÁSICA ====================
    @Json(name = "title") val title: String,
    @Json(name = "title_with_featured") val titleWithFeatured: String? = null,
    @Json(name = "full_title") val fullTitle: String? = null,

    // ==================== ARTISTAS ====================
    @Json(name = "artist_names") val artistNames: String? = null,
    @Json(name = "primary_artist") val primaryArtist: ArtistInfo,
    @Json(name = "featured_artists") val featuredArtists: List<ArtistInfo>? = null,
    @Json(name = "producer_artists") val producerArtists: List<ArtistInfo>? = null,
    @Json(name = "writer_artists") val writerArtists: List<ArtistInfo>? = null,

    // ==================== ÁLBUM ====================
    @Json(name = "album") val album: AlbumInfo? = null,

    // ==================== FECHAS ====================
    @Json(name = "release_date") val releaseDate: String? = null,
    @Json(name = "release_date_for_display") val releaseDateDisplay: String? = null,
    @Json(name = "release_date_components") val releaseDateComponents: ReleaseDateComponents? = null,

    // ==================== IMÁGENES ====================
    @Json(name = "song_art_image_url") val songArtImageUrl: String? = null,
    @Json(name = "song_art_image_thumbnail_url") val songArtImageThumbnailUrl: String? = null,
    @Json(name = "header_image_url") val headerImageUrl: String? = null,
    @Json(name = "header_image_thumbnail_url") val headerImageThumbnailUrl: String? = null,

    // ==================== ESTADÍSTICAS ====================
    @Json(name = "stats") val stats: SongStats? = null,
    @Json(name = "pageviews") val pageviews: Int? = null,

    // ==================== DESCRIPCIÓN ====================
    @Json(name = "description") val description: Description? = null,
    @Json(name = "description_annotation") val descriptionAnnotation: DescriptionAnnotation? = null,

    // ==================== MEDIA & EXTERNAL LINKS ====================
    @Json(name = "media") val media: List<MediaItem>? = null,
    @Json(name = "apple_music_id") val appleMusicId: String? = null,
    @Json(name = "apple_music_player_url") val appleMusicPlayerUrl: String? = null,
    @Json(name = "spotify_uuid") val spotifyUuid: String? = null,
    @Json(name = "youtube_url") val youtubeUrl: String? = null,
    @Json(name = "soundcloud_url") val soundcloudUrl: String? = null,

    // ==================== METADATA ====================
    @Json(name = "language") val language: String? = null,
    @Json(name = "recording_location") val recordingLocation: String? = null,
    @Json(name = "lyrics_state") val lyricsState: String? = null,

    // ==================== FLAGS ====================
    @Json(name = "featured_video") val featuredVideo: Boolean? = null,
    @Json(name = "hot") val hot: Boolean? = null,
    @Json(name = "lyrics_marked_complete_by") val lyricsMarkedCompleteBy: String? = null,

    // ==================== RELATIONSHIPS ====================
    @Json(name = "song_relationships") val songRelationships: List<SongRelationship>? = null,
    @Json(name = "verified_annotations_by") val verifiedAnnotationsBy: List<UserInfo>? = null,

    // ==================== CUSTOM PERFORMANCES ====================
    @Json(name = "custom_performances") val customPerformances: List<CustomPerformance>? = null,

    // ==================== TIMESTAMPS ====================
    @Json(name = "lyrics_updated_at") val lyricsUpdatedAt: Long? = null,
    @Json(name = "updated_by_human_at") val updatedByHumanAt: Long? = null
)

// ==================== SUPPORTING DATA CLASSES ====================

@JsonClass(generateAdapter = true)
data class ArtistInfo(
    @Json(name = "id") val id: String,
    @Json(name = "name") val name: String,
    @Json(name = "url") val url: String? = null,
    @Json(name = "image_url") val imageUrl: String? = null,
    @Json(name = "header_image_url") val headerImageUrl: String? = null,
    @Json(name = "header_image_thumbnail_url") val headerImageThumbnailUrl: String? = null,
    @Json(name = "is_verified") val isVerified: Boolean? = null,
    @Json(name = "is_meme_verified") val isMemeVerified: Boolean? = null
)

@JsonClass(generateAdapter = true)
data class AlbumInfo(
    @Json(name = "id") val id: String,
    @Json(name = "name") val name: String,
    @Json(name = "url") val url: String? = null,
    @Json(name = "api_path") val apiPath: String? = null,
    @Json(name = "cover_art_url") val coverArtUrl: String? = null,
    @Json(name = "cover_art_thumbnail_url") val coverArtThumbnailUrl: String? = null,
    @Json(name = "artist") val artist: ArtistInfo? = null,
    @Json(name = "release_date") val releaseDate: String? = null,
    @Json(name = "release_date_components") val releaseDateComponents: ReleaseDateComponents? = null
)

@JsonClass(generateAdapter = true)
data class ReleaseDateComponents(
    @Json(name = "year") val year: Int? = null,
    @Json(name = "month") val month: Int? = null,
    @Json(name = "day") val day: Int? = null
)

@JsonClass(generateAdapter = true)
data class SongStats(
    @Json(name = "pageviews") val pageviews: Int? = null,
    @Json(name = "hot") val hot: Boolean? = null,
    @Json(name = "unreviewed_annotations") val unreviewedAnnotations: Int? = null,
    @Json(name = "concurrents") val concurrents: Int? = null,
    @Json(name = "accepted_annotations") val acceptedAnnotations: Int? = null,
    @Json(name = "contributors") val contributors: Int? = null,
    @Json(name = "iq_earners") val iqEarners: Int? = null,
    @Json(name = "transcribers") val transcribers: Int? = null,
    @Json(name = "verified_annotations") val verifiedAnnotations: Int? = null
)

@JsonClass(generateAdapter = true)
data class Description(
    @Json(name = "plain") val plain: String? = null,
    @Json(name = "html") val html: String? = null,
    @Json(name = "markdown") val markdown: String? = null
)

@JsonClass(generateAdapter = true)
data class DescriptionAnnotation(
    @Json(name = "id") val id: String? = null,
    @Json(name = "type") val type: String? = null,
    @Json(name = "annotator_id") val annotatorId: String? = null,
    @Json(name = "annotator_login") val annotatorLogin: String? = null,
    @Json(name = "api_path") val apiPath: String? = null,
    @Json(name = "classification") val classification: String? = null,
    @Json(name = "fragment") val fragment: String? = null,
    @Json(name = "body") val body: Description? = null
)

@JsonClass(generateAdapter = true)
data class MediaItem(
    @Json(name = "provider") val provider: String,
    @Json(name = "type") val type: String,
    @Json(name = "url") val url: String,
    @Json(name = "native_uri") val nativeUri: String? = null
)

@JsonClass(generateAdapter = true)
data class SongRelationship(
    @Json(name = "type") val type: String,
    @Json(name = "songs") val songs: List<SongResult>? = null
)

@JsonClass(generateAdapter = true)
data class CustomPerformance(
    @Json(name = "label") val label: String,
    @Json(name = "artists") val artists: List<ArtistInfo>
)

@JsonClass(generateAdapter = true)
data class UserInfo(
    @Json(name = "id") val id: String,
    @Json(name = "login") val login: String? = null,
    @Json(name = "name") val name: String? = null,
    @Json(name = "iq") val iq: Int? = null
)

// ==================== EXTENSION FUNCTIONS ====================

/**
 * Obtiene la mejor URL de portada disponible
 */
fun SongDetails.getBestCoverArtUrl(): String? {
    return songArtImageUrl
        ?: album?.coverArtUrl
        ?: headerImageUrl
}

/**
 * Obtiene thumbnail de portada
 */
fun SongDetails.getCoverArtThumbnail(): String? {
    return songArtImageThumbnailUrl
        ?: album?.coverArtThumbnailUrl
        ?: headerImageThumbnailUrl
}

/**
 * Obtiene todos los artistas involucrados
 */
fun SongDetails.getAllArtists(): List<ArtistInfo> {
    val artists = mutableListOf(primaryArtist)
    featuredArtists?.let { artists.addAll(it) }
    return artists
}

/**
 * Obtiene lista de productores
 */
fun SongDetails.getProducers(): List<String> {
    return producerArtists?.map { it.name } ?: emptyList()
}

/**
 * Obtiene lista de escritores
 */
fun SongDetails.getWriters(): List<String> {
    return writerArtists?.map { it.name } ?: emptyList()
}

/**
 * Obtiene media links agrupados por plataforma
 */
fun SongDetails.getMediaLinks(): Map<String, String> {
    val links = mutableMapOf<String, String>()

    youtubeUrl?.let { links["youtube"] = it }
    spotifyUuid?.let { links["spotify"] = "spotify:track:$it" }
    soundcloudUrl?.let { links["soundcloud"] = it }
    appleMusicPlayerUrl?.let { links["apple_music"] = it }

    media?.forEach { item ->
        if (!links.containsKey(item.provider)) {
            links[item.provider] = item.url
        }
    }

    return links
}

/**
 * Verifica si la canción es popular/trending
 */
fun SongDetails.isPopular(): Boolean {
    return hot == true || (pageviews ?: 0) > 1_000_000
}

/**
 * Obtiene descripción en texto plano
 */
fun SongDetails.getPlainDescription(): String? {
    return description?.plain
}

/**
 * Construye fecha de lanzamiento estructurada
 */
fun SongDetails.getReleaseDateString(): String? {
    return releaseDateDisplay ?: releaseDate
}