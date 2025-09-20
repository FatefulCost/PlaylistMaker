package com.example.playlistmaker

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

sealed class SearchState {
    data class Success(val tracks: List<Track>) : SearchState()
    data class Error(val message: String) : SearchState()
    object Empty : SearchState()
}

class SearchViewModel : ViewModel() {
    private val _searchState = MutableLiveData<SearchState>()
    val searchState: LiveData<SearchState> = _searchState

    private var lastSearchQuery: String = ""

    fun searchTracks(query: String) {
        if (query.isBlank()) {
            _searchState.value = SearchState.Empty
            return
        }

        lastSearchQuery = query
        _searchState.value = SearchState.Success(emptyList())

        viewModelScope.launch {
            try {
                val response = RetrofitClient.iTunesApi.searchTracks(query)
                if (response.isSuccessful) {
                    val tracks = response.body()?.results?.map { it.toTrack() } ?: emptyList()
                    if (tracks.isEmpty()) {
                        _searchState.value = SearchState.Empty
                    } else {
                        _searchState.value = SearchState.Success(tracks)
                    }
                } else {
                    _searchState.value = SearchState.Error("Ошибка сервера: ${response.code()}")
                }
            } catch (e: IOException) {
                _searchState.value = SearchState.Error("Проверьте подключение к интернету")
            } catch (e: Exception) {
                _searchState.value = SearchState.Error("Неизвестная ошибка")
            }
        }
    }

    fun retryLastSearch() {
        if (lastSearchQuery.isNotBlank()) {
            searchTracks(lastSearchQuery)
        }
    }
}