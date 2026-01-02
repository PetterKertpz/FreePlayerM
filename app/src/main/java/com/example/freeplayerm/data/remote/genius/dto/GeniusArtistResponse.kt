// app/src/main/java/com/example/freeplayerm/data/remote/genius/dto/GeniusArtistResponse.kt
package com.example.freeplayerm.data.remote.genius.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * 🎤 GENIUS ARTIST RESPONSE
 *
 * DTO completo para endpoint /artists/{id} de Genius API
 */
@JsonClass(generateAdapter = true)
data class GeniusArtistResponse(
    @Json(name = "meta") val meta: MetaInfo,
    @Json(name = "response") val response: ArtistResponseData,
)

@JsonClass(generateAdapter = true)
data class ArtistResponseData(@Json(name = "artist") val artist: ArtistDetails)

@JsonClass(generateAdapter = true)
data class ArtistDetails(
    // ==================== IDENTIFICADORES ====================
    @Json(name = "id") val id: String,
    @Json(name = "api_path") val apiPath: String,
    @Json(name = "url") val url: String,

    // ==================== INFORMACIÓN BÁSICA ====================
    @Json(name = "name") val name: String,
    @Json(name = "alternate_names") val alternateNames: List<String>? = null,

    // ==================== BIOGRAFÍA ====================
    @Json(name = "description") val description: Description? = null,
    @Json(name = "description_annotation") val descriptionAnnotation: DescriptionAnnotation? = null,

    // ==================== IMÁGENES ====================
    @Json(name = "image_url") val imageUrl: String? = null,
    @Json(name = "header_image_url") val headerImageUrl: String? = null,
    @Json(name = "avatar") val avatar: AvatarInfo? = null,

    // ==================== REDES SOCIALES ====================
    @Json(name = "facebook_name") val facebookName: String? = null,
    @Json(name = "instagram_name") val instagramName: String? = null,
    @Json(name = "twitter_name") val twitterName: String? = null,

    // ==================== ENLACES EXTERNOS ====================
    @Json(name = "facebook_url") val facebookUrl: String? = null,
    @Json(name = "instagram_url") val instagramUrl: String? = null,
    @Json(name = "twitter_url") val twitterUrl: String? = null,

    // ==================== IDS EXTERNOS ====================
    @Json(name = "spotify_id") val spotifyId: String? = null,
    @Json(name = "apple_music_id") val appleMusicId: String? = null,
    @Json(name = "soundcloud_id") val soundcloudId: String? = null,

    // ==================== ESTADÍSTICAS ====================
    @Json(name = "stats") val stats: ArtistStats? = null,
    @Json(name = "followers_count") val followersCount: Int? = null,
    @Json(name = "iq") val iq: Int? = null,

    // ==================== METADATA ====================
    @Json(name = "is_verified") val isVerified: Boolean? = null,
    @Json(name = "is_meme_verified") val isMemeVerified: Boolean? = null,
    @Json(name = "translation_artist") val translationArtist: Boolean? = null,

    // ==================== CURRENT USER ====================
    @Json(name = "current_user_metadata") val currentUserMetadata: CurrentUserMetadata? = null,

    // ==================== TIMESTAMPS ====================
    @Json(name = "updated_by_human_at") val updatedByHumanAt: Long? = null,
)

@JsonClass(generateAdapter = true)
data class ArtistStats(
    @Json(name = "followers") val followers: Int? = null,
    @Json(name = "verified_annotations_count") val verifiedAnnotationsCount: Int? = null,
    @Json(name = "transcriptions_count") val transcriptionsCount: Int? = null,
    @Json(name = "annotations_count") val annotationsCount: Int? = null,
    @Json(name = "iq_points") val iqPoints: Int? = null,
    @Json(name = "answers_count") val answersCount: Int? = null,
    @Json(name = "questions_count") val questionsCount: Int? = null,
    @Json(name = "comments_count") val commentsCount: Int? = null,
    @Json(name = "contributions_count") val contributionsCount: Int? = null,
    @Json(name = "transcribed_songs") val transcribedSongs: Int? = null,
    @Json(name = "hot_songs") val hotSongs: Int? = null,
)

@JsonClass(generateAdapter = true)
data class AvatarInfo(
    @Json(name = "tiny") val tiny: ImageUrl? = null,
    @Json(name = "thumb") val thumb: ImageUrl? = null,
    @Json(name = "small") val small: ImageUrl? = null,
    @Json(name = "medium") val medium: ImageUrl? = null,
)

@JsonClass(generateAdapter = true)
data class ImageUrl(
    @Json(name = "url") val url: String,
    @Json(name = "bounding_box") val boundingBox: BoundingBox? = null,
)

@JsonClass(generateAdapter = true)
data class BoundingBox(@Json(name = "width") val width: Int, @Json(name = "height") val height: Int)

@JsonClass(generateAdapter = true)
data class CurrentUserMetadata(
    @Json(name = "permissions") val permissions: List<String>? = null,
    @Json(name = "excluded_permissions") val excludedPermissions: List<String>? = null,
    @Json(name = "interactions") val interactions: UserInteractions? = null,
)

@JsonClass(generateAdapter = true)
data class UserInteractions(
    @Json(name = "following") val following: Boolean? = null,
    @Json(name = "pyong") val pyong: Boolean? = null,
)

// ==================== EXTENSION FUNCTIONS ====================

/** Obtiene la mejor imagen disponible del artista */
fun ArtistDetails.getBestImageUrl(): String? {
    return imageUrl ?: avatar?.medium?.url ?: avatar?.small?.url ?: avatar?.thumb?.url
}

/** Obtiene la imagen de header/banner */
fun ArtistDetails.getHeaderImageUrl(): String? {
    return headerImageUrl
}

/** Obtiene biografía en texto plano */
fun ArtistDetails.getPlainDescription(): String? {
    return description?.plain
}

/** Obtiene biografía en HTML */
fun ArtistDetails.getHtmlDescription(): String? {
    return description?.html
}

/** Construye mapa de redes sociales */
fun ArtistDetails.getSocialMediaLinks(): Map<String, String> {
    val links = mutableMapOf<String, String>()

    facebookUrl?.let { links["facebook"] = it }
    instagramUrl?.let { links["instagram"] = it }
    twitterUrl?.let { links["twitter"] = it }

    // Construir URLs si solo tenemos usernames
    facebookName
        ?.takeIf { !links.containsKey("facebook") }
        ?.let { links["facebook"] = "https://facebook.com/$it" }
    instagramName
        ?.takeIf { !links.containsKey("instagram") }
        ?.let { links["instagram"] = "https://instagram.com/$it" }
    twitterName
        ?.takeIf { !links.containsKey("twitter") }
        ?.let { links["twitter"] = "https://twitter.com/$it" }

    return links
}

/** Construye mapa de IDs externos de plataformas */
fun ArtistDetails.getExternalPlatformIds(): Map<String, String> {
    val ids = mutableMapOf<String, String>()

    spotifyId?.let { ids["spotify"] = it }
    appleMusicId?.let { ids["apple_music"] = it }
    soundcloudId?.let { ids["soundcloud"] = it }

    return ids
}

/** Verifica si el artista es verificado */
fun ArtistDetails.isVerifiedArtist(): Boolean {
    return isVerified == true || isMemeVerified == true
}

/** Obtiene conteo de seguidores */
fun ArtistDetails.getFollowersCount(): Int {
    return followersCount ?: stats?.followers ?: 0
}

/** Verifica si el artista es popular */
fun ArtistDetails.isPopular(): Boolean {
    val followers = getFollowersCount()
    val hotSongs = stats?.hotSongs ?: 0
    return followers > 10000 || hotSongs > 5
}

/** Obtiene nombres alternativos del artista */
fun ArtistDetails.getAllNames(): List<String> {
    val names = mutableListOf(name)
    alternateNames?.let { names.addAll(it) }
    return names
}

/** Obtiene puntos IQ del artista */
fun ArtistDetails.getIqPoints(): Int {
    return iq ?: stats?.iqPoints ?: 0
}

/** Construye resumen de estadísticas */
fun ArtistDetails.getStatsSummary(): String {
    return buildString {
        append("Followers: ${getFollowersCount()}")
        stats?.let { s ->
            s.annotationsCount?.let { append(", Annotations: $it") }
            s.hotSongs?.let { append(", Hot Songs: $it") }
            s.transcribedSongs?.let { append(", Transcribed: $it") }
        }
    }
}

/** Verifica si el artista sigue al usuario actual */
fun ArtistDetails.isFollowedByCurrentUser(): Boolean {
    return currentUserMetadata?.interactions?.following == true
}
