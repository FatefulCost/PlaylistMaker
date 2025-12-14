package com.example.playlistmaker.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.domain.interactor.SearchInteractor
import com.example.playlistmaker.domain.interactor.SearchResult
import com.example.playlistmaker.domain.model.Track
import kotlinx.coroutines.launch

sealed class SearchStateUi {
    object Loading : SearchStateUi()
    data class Success(val tracks: List<Track>) : SearchStateUi()
    data class Error(val message: String) : SearchStateUi()
    object Empty : SearchStateUi()
}

class SearchViewModel(
    private val searchInteractor: SearchInteractor
) : ViewModel() {
    private val _searchState = MutableLiveData<SearchStateUi>()
    val searchState: LiveData<SearchStateUi> = _searchState

    private var lastSearchQuery: String = ""

    fun searchTracks(query: String) {
        if (query.isBlank()) {
            _searchState.value = SearchStateUi.Empty
            return
        }

        lastSearchQuery = query
        _searchState.value = SearchStateUi.Loading

        viewModelScope.launch {
            val result = searchInteractor.searchTracks(query)
            _searchState.value = when (result) {
                is SearchResult.Success -> SearchStateUi.Success(result.tracks)
                is SearchResult.Error -> SearchStateUi.Error(result.message)
                SearchResult.Empty -> SearchStateUi.Empty
            }
        }
    }

    fun retryLastSearch() {
        if (lastSearchQuery.isNotBlank()) {
            searchTracks(lastSearchQuery)
        }
    }
}