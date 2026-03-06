package com.example.playlistmaker.feature.search.domain.interactor.impl

import com.example.playlistmaker.feature.search.domain.interactor.SearchInteractor
import com.example.playlistmaker.feature.search.domain.interactor.SearchResult
import com.example.playlistmaker.feature.search.domain.repository.TrackRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

class SearchInteractorImpl(
    private val trackRepository: TrackRepository
) : SearchInteractor {

    override fun searchTracks(query: String): Flow<SearchResult> {
        return if (query.isBlank()) {
            flow { emit(SearchResult.Empty) }
        } else {
            trackRepository.searchTracks(query)
                .map { tracks ->
                    if (tracks.isEmpty()) {
                        SearchResult.Empty
                    } else {
                        SearchResult.Success(tracks)
                    }
                }
                .catch { exception ->
                    emit(SearchResult.Error("Ошибка поиска: ${exception.message ?: "Неизвестная ошибка"}"))
                }
        }
    }
}