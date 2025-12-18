package com.example.playlistmaker.feature.search.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.feature.search.domain.interactor.HistoryInteractor
import com.example.playlistmaker.feature.search.domain.interactor.SearchInteractor
import com.example.playlistmaker.feature.search.domain.interactor.SearchResult
import com.example.playlistmaker.feature.search.domain.model.Track
import kotlinx.coroutines.launch

sealed class SearchUiState {
    object Initial : SearchUiState()
    object Loading : SearchUiState()
    data class Success(val tracks: List<Track>) : SearchUiState()
    data class Error(val message: String) : SearchUiState()
    object Empty : SearchUiState()
    data class History(val tracks: List<Track>) : SearchUiState()
}

class SearchViewModel(
    private val searchInteractor: SearchInteractor,
    private val historyInteractor: HistoryInteractor
) : ViewModel() {

    private val _searchState = MutableLiveData<SearchUiState>(SearchUiState.Initial)
    val searchState: LiveData<SearchUiState> = _searchState

    private var lastSearchQuery: String = ""

    fun loadSearchHistory() {
        val history = historyInteractor.getSearchHistory()
        if (history.isEmpty()) {
            _searchState.value = SearchUiState.Initial
        } else {
            _searchState.value = SearchUiState.History(history)
        }
    }

    fun searchTracks(query: String) {
        if (query.isBlank()) {
            loadSearchHistory()
            return
        }

        lastSearchQuery = query
        _searchState.value = SearchUiState.Loading

        viewModelScope.launch {
            val result = searchInteractor.searchTracks(query)
            _searchState.value = when (result) {
                is SearchResult.Success -> SearchUiState.Success(result.tracks)
                is SearchResult.Error -> SearchUiState.Error(result.message)
                SearchResult.Empty -> SearchUiState.Empty
            }
        }
    }

    fun addTrackToHistory(track: Track) {
        historyInteractor.addTrackToHistory(track)
    }

    fun clearSearchHistory() {
        historyInteractor.clearSearchHistory()
        loadSearchHistory()
    }

    fun retryLastSearch() {
        if (lastSearchQuery.isNotBlank()) {
            searchTracks(lastSearchQuery)
        }
    }
}