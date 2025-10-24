package com.example.freeplayerm.data.remote.dto
import com.squareup.moshi.Json

data class GeniusSongResponse(
    @Json(name = "response") val response: SongResponseData?
)

data class SongResponseData(
    @Json(name = "song") val song: SongDetails?
)

data class SongDetails(
    @Json(name = "id") val id: String,
    @Json(name = "title") val title: String,
    // ... puedes añadir más campos si los necesitas ...
)