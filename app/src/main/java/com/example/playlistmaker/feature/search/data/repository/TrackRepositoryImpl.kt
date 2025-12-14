package com.example.playlistmaker.feature.search.data.repository

import com.example.playlistmaker.core.api.service.ITunesApiService
import com.example.playlistmaker.feature.search.data.mapper.TrackMapper
import com.example.playlistmaker.feature.search.domain.model.Track
import com.example.playlistmaker.feature.search.domain.repository.TrackRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TrackRepositoryImpl(
    private val apiService: ITunesApiService
) : TrackRepository {

    override suspend fun searchTracks(query: String): List<Track> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.searchTracks(query)
            if (response.isSuccessful) {
                response.body()?.results?.let { trackResponses ->
                    return@withContext TrackMapper.mapToDomainList(trackResponses)
                } ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}