package com.example.playlistmaker.feature.player.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.feature.media.domain.interactor.PlaylistInteractor
import com.example.playlistmaker.feature.media.domain.model.Playlist
import kotlinx.coroutines.launch

class PlaylistBottomSheetViewModel(
    private val playlistInteractor: PlaylistInteractor
) : ViewModel() {

    private val _playlists = MutableLiveData<List<Playlist>>()
    val playlists: LiveData<List<Playlist>> = _playlists

    fun loadPlaylists() {
        viewModelScope.launch {
            val playlists = playlistInteractor.getPlaylistsSync()
            _playlists.value = playlists
        }
    }

    suspend fun isTrackInPlaylist(playlistId: Long, trackId: Long): Boolean {
        return playlistInteractor.isTrackInPlaylist(playlistId, trackId)
    }

    suspend fun addTrackToPlaylist(playlistId: Long, trackId: Long): Boolean {
        return try {
            playlistInteractor.addTrackToPlaylist(playlistId, trackId)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}