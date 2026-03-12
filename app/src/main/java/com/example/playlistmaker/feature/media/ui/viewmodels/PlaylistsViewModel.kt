package com.example.playlistmaker.feature.media.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.feature.media.domain.interactor.PlaylistInteractor
import com.example.playlistmaker.feature.media.domain.model.Playlist
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

sealed class PlaylistsState {
    object Loading : PlaylistsState()
    data class Success(val playlists: List<Playlist>) : PlaylistsState()
    object Empty : PlaylistsState()
    data class Error(val message: String) : PlaylistsState()
}

class PlaylistsViewModel(
    private val playlistInteractor: PlaylistInteractor
) : ViewModel() {

    private val _playlistsState = MutableLiveData<PlaylistsState>(PlaylistsState.Loading)
    val playlistsState: LiveData<PlaylistsState> = _playlistsState

    init {
        loadPlaylists()
    }

    fun loadPlaylists() {
        playlistInteractor.getPlaylists()
            .onEach { playlists ->
                _playlistsState.value = if (playlists.isEmpty()) {
                    PlaylistsState.Empty
                } else {
                    PlaylistsState.Success(playlists)
                }
            }
            .catch { exception ->
                _playlistsState.value = PlaylistsState.Error(
                    "Ошибка загрузки плейлистов: ${exception.message}"
                )
            }
            .launchIn(viewModelScope)
    }

    fun createPlaylist(playlist: Playlist) {
        viewModelScope.launch {
            playlistInteractor.createPlaylist(playlist)
            loadPlaylists()
        }
    }
}