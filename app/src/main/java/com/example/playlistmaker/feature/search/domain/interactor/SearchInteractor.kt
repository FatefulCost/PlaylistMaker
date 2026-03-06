package com.example.playlistmaker.feature.search.domain.interactor

import com.example.playlistmaker.feature.search.domain.model.Track
import kotlinx.coroutines.flow.Flow

interface SearchInteractor {
    fun searchTracks(query: String): Flow<SearchResult>
}

sealed class SearchResult {
    data class Success(val tracks: List<Track>) : SearchResult()
    data class Error(val message: String) : SearchResult()
    object Empty : SearchResult()
}