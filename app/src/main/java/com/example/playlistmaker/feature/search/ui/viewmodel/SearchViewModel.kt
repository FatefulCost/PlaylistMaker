package com.example.playlistmaker.feature.search.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.feature.search.domain.interactor.HistoryInteractor
import com.example.playlistmaker.feature.search.domain.interactor.SearchInteractor
import com.example.playlistmaker.feature.search.domain.interactor.SearchResult
import com.example.playlistmaker.feature.search.domain.model.Track
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
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

    private val _searchText = MutableLiveData<String>("")
    val searchText: LiveData<String> = _searchText

    private var searchJob: Job? = null
    private var lastSearchQuery: String = ""

    private val searchQueryFlow = MutableSharedFlow<String>(extraBufferCapacity = 1)
    private var lastSearchResults: List<Track> = emptyList()

    init {
        setupSearchDebounce()
        loadSearchHistory()
    }

    private fun setupSearchDebounce() {
        viewModelScope.launch {
            searchQueryFlow
                .debounce(2000L) // Задержка 2 секунды
                .distinctUntilChanged()
                .collectLatest { query ->
                    if (query.isNotBlank()) {
                        performSearch(query)
                    } else {
                        loadSearchHistory()
                    }
                }
        }
    }

    fun onSearchTextChanged(text: String) {
        _searchText.value = text
        lastSearchText = text
        viewModelScope.launch {
            searchQueryFlow.emit(text)
        }
    }

    private fun performSearch(query: String) {
        if (query.isBlank()) return

        lastSearchQuery = query
        _searchState.value = SearchUiState.Loading

        viewModelScope.launch {
            searchInteractor.searchTracks(query)
                .catch { exception ->
                    _searchState.value = SearchUiState.Error(
                        "Ошибка поиска: ${exception.message ?: "Неизвестная ошибка"}"
                    )
                }
                .collect { result ->
                    when (result) {
                        is SearchResult.Success -> {
                            lastSearchResults = result.tracks // Сохраняем результаты
                            _searchState.value = SearchUiState.Success(result.tracks)
                        }
                        is SearchResult.Error -> {
                            lastSearchResults = emptyList()
                            _searchState.value = SearchUiState.Error(result.message)
                        }
                        SearchResult.Empty -> {
                            lastSearchResults = emptyList()
                            _searchState.value = SearchUiState.Empty
                        }
                    }
                }
        }
    }


    fun loadSearchHistory() {
        // Если есть результаты последнего поиска и есть текст в поиске, не показываем историю
        val currentText = _searchText.value ?: ""
        if (lastSearchResults.isNotEmpty() && currentText.isNotBlank()) {
            _searchState.value = SearchUiState.Success(lastSearchResults)
            return
        }

        val history = historyInteractor.getSearchHistory()
        if (history.isEmpty()) {
            _searchState.value = SearchUiState.Initial
        } else {
            _searchState.value = SearchUiState.History(history)
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
            performSearch(lastSearchQuery)
        }
    }

    fun resetSearch() {
        _searchText.value = ""
        lastSearchResults = emptyList() // Очищаем результаты поиска
        viewModelScope.launch {
            searchQueryFlow.emit("")
        }
        loadSearchHistory()
    }
}