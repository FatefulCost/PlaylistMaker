package com.example.playlistmaker.feature.media.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.feature.favorites.domain.interactor.FavoritesInteractor
import com.example.playlistmaker.feature.search.domain.model.Track
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

sealed class FavoritesState {
    object Loading : FavoritesState()
    data class Success(val tracks: List<Track>) : FavoritesState()
    object Empty : FavoritesState()
    data class Error(val message: String) : FavoritesState()
}

class FavoritesViewModel(
    private val favoritesInteractor: FavoritesInteractor
) : ViewModel() {

    private val _favoritesState = MutableLiveData<FavoritesState>(FavoritesState.Loading)
    val favoritesState: LiveData<FavoritesState> = _favoritesState

    init {
        loadFavorites()
    }

    fun loadFavorites() {
        favoritesInteractor.getFavorites()
            .onEach { tracks ->
                _favoritesState.value = if (tracks.isEmpty()) {
                    FavoritesState.Empty
                } else {
                    FavoritesState.Success(tracks)
                }
            }
            .catch { exception ->
                _favoritesState.value = FavoritesState.Error(
                    "Ошибка загрузки избранных треков: ${exception.message}"
                )
            }
            .launchIn(viewModelScope)
    }

    fun removeFromFavorites(track: Track) {
        viewModelScope.launch {
            favoritesInteractor.removeFromFavorites(track)
        }
    }
}