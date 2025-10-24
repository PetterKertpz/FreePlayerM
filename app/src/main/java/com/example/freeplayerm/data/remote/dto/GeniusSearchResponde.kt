package com.example.freeplayerm.data.remote.dto
import com.squareup.moshi.Json

data class GeniusSearchResponse(
    @Json(name = "response") val response: SearchResponseData?
)

data class SearchResponseData(
    @Json(name = "hits") val hits: List<Hit>?
)

data class Hit(
    @Json(name = "result") val result: SongResult?
)

data class SongResult(
    @Json(name = "id") val id: String,
    @Json(name = "title") val title: String,
    @Json(name = "url") val url: String,
    @Json(name = "song_art_image_thumbnail_url") val song_art_image_thumbnail_url: String?,
    @Json(name = "song_art_image_url") val song_art_image_url: String?,
    @Json(name = "primary_artist") val primary_artist: ArtistResult?
)

data class ArtistResult(
    @Json(name = "id") val id: String,
    @Json(name = "name") val name: String
)