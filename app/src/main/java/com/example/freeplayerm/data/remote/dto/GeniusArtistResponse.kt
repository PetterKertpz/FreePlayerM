package com.example.freeplayerm.data.remote.dto
import com.squareup.moshi.Json

data class GeniusArtistResponse(
    @Json(name = "response") val response: ArtistResponseData?
)

data class ArtistResponseData(
    @Json(name = "artist") val artist: ArtistDetails?
)

data class ArtistDetails(
    @Json(name = "id") val id: String,
    @Json(name = "name") val name: String,
    @Json(name = "image_url") val image_url: String?,
    @Json(name = "description") val description: ArtistDescription?
)

data class ArtistDescription(
    @Json(name = "plain") val plain: String?
)