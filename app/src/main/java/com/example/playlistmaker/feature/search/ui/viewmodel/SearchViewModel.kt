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

    var lastSearchText: String = ""
        private set

    private val _searchState = MutableLiveData<SearchUiState>(SearchUiState.Initial)
    val searchState: LiveData<SearchUiState> = _searchState

    private var lastSearchQuery: String = ""
    private var lastSearchResults: List<Track> = emptyList()

    // Добавляем LiveData для текста поиска
    private val _searchText = MutableLiveData<String>("")
    val searchText: LiveData<String> = _searchText

    fun loadSearchHistory() {
        // Если есть сохраненные результаты поиска, показываем их
        if (lastSearchResults.isNotEmpty()) {
            _searchState.value = SearchUiState.Success(lastSearchResults)
            return
        }

        // Иначе показываем историю
        val history = historyInteractor.getSearchHistory()
        if (history.isEmpty()) {
            _searchState.value = SearchUiState.Initial
        } else {
            _searchState.value = SearchUiState.History(history)
        }
    }

    fun searchTracks(query: String) {
        if (query.isBlank()) {
            _searchText.value = ""
            loadSearchHistory()
            return
        }

        lastSearchQuery = query
        _searchText.value = query
        _searchState.value = SearchUiState.Loading

        viewModelScope.launch {
            val result = searchInteractor.searchTracks(query)
            _searchState.value = when (result) {
                is SearchResult.Success -> {
                    lastSearchResults = result.tracks
                    SearchUiState.Success(result.tracks)
                }
                is SearchResult.Error -> SearchUiState.Error(result.message)
                SearchResult.Empty -> {
                    lastSearchResults = emptyList()
                    SearchUiState.Empty
                }
            }
        }
    }

    fun addTrackToHistory(track: Track) {
        historyInteractor.addTrackToHistory(track)
    }

    fun clearSearchHistory() {
        historyInteractor.clearSearchHistory()
        lastSearchResults = emptyList()
        _searchText.value = ""
        loadSearchHistory()
    }

    fun retryLastSearch() {
        if (lastSearchQuery.isNotBlank()) {
            searchTracks(lastSearchQuery)
        }
    }

    // Метод для сброса состояния поиска
    fun resetSearch() {
        _searchText.value = ""
        lastSearchResults = emptyList()
        loadSearchHistory()
    }

    // Метод для обновления текста поиска без выполнения поиска
    fun updateSearchText(text: String) {
        lastSearchText = text
    }
}