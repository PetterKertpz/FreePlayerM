// app/src/main/java/com/example/freeplayerm/data/remote/genius/api/GeniusApiService.kt
package com.example.freeplayerm.data.remote.genius.api

import com.example.freeplayerm.data.remote.genius.dto.GeniusArtistResponse
import com.example.freeplayerm.data.remote.genius.dto.GeniusSearchResponse
import com.example.freeplayerm.data.remote.genius.dto.GeniusSongResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * 🌐 GENIUS API SERVICE
 *
 * Interface Retrofit para consumir Genius API
 * Base URL: https://api.genius.com/
 *
 * Requiere: Authorization header con Bearer token (configurado en interceptor)
 */
interface GeniusApiService {

    /**
     * Busca canciones, artistas y álbumes
     *
     * @param query Término de búsqueda
     * @return Lista de resultados ordenados por relevancia
     */
    @GET("search")
    suspend fun search(@Query("q") query: String): Response<GeniusSearchResponse>

    /**
     * Obtiene detalles completos de una canción
     *
     * @param id ID de la canción en Genius
     * @param textFormat Formato del texto ("plain", "html", "dom")
     * @return Metadata completa de la canción
     */
    @GET("songs/{id}")
    suspend fun getSong(
        @Path("id") id: String,
        @Query("text_format") textFormat: String = "plain"
    ): Response<GeniusSongResponse>

    /**
     * Obtiene detalles completos de un artista
     *
     * @param id ID del artista en Genius
     * @param textFormat Formato del texto ("plain", "html", "dom")
     * @return Metadata completa del artista
     */
    @GET("artists/{id}")
    suspend fun getArtist(
        @Path("id") id: String,
        @Query("text_format") textFormat: String = "plain"
    ): Response<GeniusArtistResponse>
}