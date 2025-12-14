package com.example.playlistmaker.domain.interactor.impl

import com.example.playlistmaker.domain.interactor.SearchInteractor
import com.example.playlistmaker.domain.interactor.SearchResult
import com.example.playlistmaker.domain.model.Track
import com.example.playlistmaker.domain.repository.TrackRepository

class SearchInteractorImpl(
    private val trackRepository: TrackRepository
) : SearchInteractor {

    override suspend fun searchTracks(query: String): SearchResult {
        return if (query.isBlank()) {
            SearchResult.Empty
        } else {
            try {
                val tracks = trackRepository.searchTracks(query)
                if (tracks.isEmpty()) {
                    SearchResult.Empty
                } else {
                    SearchResult.Success(tracks)
                }
            } catch (e: Exception) {
                SearchResult.Error("Ошибка поиска: ${e.message ?: "Неизвестная ошибка"}")
            }
        }
    }
}