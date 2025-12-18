package com.example.playlistmaker.feature.search.domain.interactor

import com.example.playlistmaker.feature.search.domain.model.Track

interface SearchInteractor {
    suspend fun searchTracks(query: String): SearchResult
}

sealed class SearchResult {
    data class Success(val tracks: List<Track>) : SearchResult()
    data class Error(val message: String) : SearchResult()
    object Empty : SearchResult()
}