package com.example.playlistmaker.feature.search.data.repository

import com.example.playlistmaker.core.api.service.ITunesApiService
import com.example.playlistmaker.feature.search.data.mapper.TrackMapper
import com.example.playlistmaker.feature.search.domain.model.Track
import com.example.playlistmaker.feature.search.domain.repository.TrackRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn

class TrackRepositoryImpl(
    private val apiService: ITunesApiService
) : TrackRepository {

    override fun searchTracks(query: String): Flow<List<Track>> = flow {
        val response = apiService.searchTracks(query)
        if (response.isSuccessful) {
            val tracks = response.body()?.results?.let { trackResponses ->
                TrackMapper.mapToDomainList(trackResponses)
            } ?: emptyList()
            emit(tracks)
        } else {
            emit(emptyList())
        }
    }.flowOn(Dispatchers.IO)
}