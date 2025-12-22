// app/src/main/java/com/example/freeplayerm/data/remote/genius/dto/GeniusSearchResponse.kt
package com.example.freeplayerm.data.remote.genius.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * 🔍 GENIUS SEARCH RESPONSE
 *
 * DTO para respuesta del endpoint /search de Genius API
 * Estructura: { meta: {...}, response: { hits: [...] } }
 */

@JsonClass(generateAdapter = true)
data class GeniusSearchResponse(
    @Json(name = "meta") val meta: MetaInfo? = null,
    @Json(name = "response") val response: SearchResponseData? = null
)

@JsonClass(generateAdapter = true)
data class SearchResponseData(
    @Json(name = "hits") val hits: List<Hit>? = null
)

@JsonClass(generateAdapter = true)
data class Hit(
    @Json(name = "result") val result: SongResult? = null,
    @Json(name = "type") val type: String? = null,
    @Json(name = "index") val index: String? = null
)

@JsonClass(generateAdapter = true)
data class SongResult(
    @Json(name = "id") val id: String,
    @Json(name = "title") val title: String,
    @Json(name = "url") val url: String,
    @Json(name = "song_art_image_thumbnail_url") val songArtImageThumbnailUrl: String? = null,
    @Json(name = "song_art_image_url") val songArtImageUrl: String? = null,
    @Json(name = "header_image_url") val headerImageUrl: String? = null,
    @Json(name = "primary_artist") val primaryArtist: ArtistResult? = null,
    @Json(name = "featured_artists") val featuredArtists: List<ArtistResult>? = null,
    @Json(name = "stats") val stats: SongStatsSimple? = null
)

@JsonClass(generateAdapter = true)
data class ArtistResult(
    @Json(name = "id") val id: String,
    @Json(name = "name") val name: String,
    @Json(name = "url") val url: String? = null,
    @Json(name = "image_url") val imageUrl: String? = null,
    @Json(name = "header_image_url") val headerImageUrl: String? = null
)

@JsonClass(generateAdapter = true)
data class SongStatsSimple(
    @Json(name = "pageviews") val pageviews: Int? = null,
    @Json(name = "hot") val hot: Boolean? = null
)

@JsonClass(generateAdapter = true)
data class MetaInfo(
    @Json(name = "status") val status: Int
)

// ==================== EXTENSION FUNCTIONS ====================

/**
 * Valida que el resultado sea una canción válida
 */
fun SongResult.isValid(): Boolean {
    return id.isNotBlank() &&
            title.isNotBlank() &&
            url.isNotBlank() &&
            primaryArtist != null
}

/**
 * Obtiene la mejor imagen disponible
 */
fun SongResult.getBestImageUrl(): String? {
    return songArtImageUrl ?: headerImageUrl
}

/**
 * Obtiene thumbnail de la imagen
 */
fun SongResult.getThumbnailUrl(): String? {
    return songArtImageThumbnailUrl ?: songArtImageUrl
}

/**
 * Verifica si la canción es popular
 */
fun SongResult.isPopular(): Boolean {
    return stats?.hot == true || (stats?.pageviews ?: 0) > 500_000
}