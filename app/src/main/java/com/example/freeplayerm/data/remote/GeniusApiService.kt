// en: app/src/main/java/com/example/freeplayerm/data/remote/GeniusApiService.kt
package com.example.freeplayerm.data.remote

import com.example.freeplayerm.data.remote.dto.GeniusArtistResponse
import com.example.freeplayerm.data.remote.dto.GeniusSearchResponse
import com.example.freeplayerm.data.remote.dto.GeniusSongResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface GeniusApiService {

    @GET("search")
    suspend fun search(@Query("q") query: String): Response<GeniusSearchResponse>

    @GET("songs/{id}")
    suspend fun getSong(
        @Path("id") id: String,
        @Query("text_format") textFormat: String = "plain"
    ): Response<GeniusSongResponse>

    @GET("artists/{id}")
    suspend fun getArtist(
        @Path("id") id: String,
        @Query("text_format") textFormat: String = "plain"
    ): Response<GeniusArtistResponse>
}