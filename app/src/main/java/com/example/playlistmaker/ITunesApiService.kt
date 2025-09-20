package com.example.playlistmaker

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ITunesApiService {
    @GET("search")
    suspend fun searchTracks(
        @Query("term") term: String,
        @Query("media") media: String = "music",
        @Query("entity") entity: String = "song",
        @Query("limit") limit: Int = 50
    ): Response<ITunesSearchResponse>
}