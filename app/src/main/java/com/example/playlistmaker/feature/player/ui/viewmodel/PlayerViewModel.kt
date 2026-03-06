package com.example.playlistmaker.feature.player.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.feature.favorites.domain.interactor.FavoritesInteractor
import com.example.playlistmaker.feature.search.domain.model.Track
import kotlinx.coroutines.launch

class PlayerViewModel(
    private val favoritesInteractor: FavoritesInteractor
) : ViewModel() {

    private val _favoriteState = MutableLiveData<Boolean>()
    val favoriteState: LiveData<Boolean> = _favoriteState

    fun checkFavoriteStatus(track: Track) {
        viewModelScope.launch {
            val isFavorite = favoritesInteractor.isFavorite(track.trackId)
            _favoriteState.value = isFavorite
        }
    }

    fun toggleFavorite(track: Track) {
        viewModelScope.launch {
            val newState = favoritesInteractor.toggleFavorite(track)
            _favoriteState.value = newState
        }
    }
}