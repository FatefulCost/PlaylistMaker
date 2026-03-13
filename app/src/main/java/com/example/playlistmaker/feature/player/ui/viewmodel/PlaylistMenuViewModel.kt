package com.example.playlistmaker.feature.player.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.feature.media.domain.interactor.PlaylistInteractor
import com.example.playlistmaker.feature.media.domain.model.Playlist
import com.example.playlistmaker.feature.search.domain.model.Track
import kotlinx.coroutines.launch
import android.util.Log
import kotlinx.coroutines.delay

class PlaylistMenuViewModel(
    private val playlistInteractor: PlaylistInteractor
) : ViewModel() {

    private val _shareText = MutableLiveData<String?>()
    val shareText: LiveData<String?> = _shareText

    private val _showEmptyShareMessage = MutableLiveData<Boolean>()
    val showEmptyShareMessage: LiveData<Boolean> = _showEmptyShareMessage

    private val _playlistDeleted = MutableLiveData<Boolean>()
    val playlistDeleted: LiveData<Boolean> = _playlistDeleted

    fun sharePlaylist(playlist: Playlist) {
        Log.d("PlaylistMenuVM", "sharePlaylist called for playlist: ${playlist.name}, id: ${playlist.id}")

        viewModelScope.launch {
            try {
                val tracks = playlistInteractor.getPlaylistTracks(playlist.id)
                Log.d("PlaylistMenuVM", "Loaded ${tracks.size} tracks")

                if (tracks.isEmpty()) {
                    _showEmptyShareMessage.value = true
                    viewModelScope.launch {
                        kotlinx.coroutines.delay(100)
                        _showEmptyShareMessage.value = false
                    }
                    return@launch
                }

                val shareText = buildShareText(playlist, tracks)
                Log.d("PlaylistMenuVM", "Share text built, length: ${shareText.length}")
                _shareText.value = shareText

                viewModelScope.launch {
                    kotlinx.coroutines.delay(100)
                    _shareText.value = null
                }

            } catch (e: Exception) {
                Log.e("PlaylistMenuVM", "Error in sharePlaylist", e)
                _showEmptyShareMessage.value = true
                viewModelScope.launch {
                    kotlinx.coroutines.delay(100)
                    _showEmptyShareMessage.value = false
                }
            }
        }
    }

    private fun buildShareText(playlist: Playlist, tracks: List<Track>): String {
        return buildString {
            appendLine(playlist.name)
            if (!playlist.description.isNullOrEmpty()) {
                appendLine(playlist.description)
            }
            val tracksCountText = when {
                tracks.size % 10 == 1 && tracks.size % 100 != 11 -> "${tracks.size} трек"
                tracks.size % 10 in 2..4 && tracks.size % 100 !in 12..14 -> "${tracks.size} трека"
                else -> "${tracks.size} треков"
            }
            appendLine(tracksCountText)
            appendLine()

            tracks.forEachIndexed { index, track ->
                val minutes = track.trackTimeMillis / 60000
                val seconds = (track.trackTimeMillis % 60000) / 1000
                val duration = String.format("%d:%02d", minutes, seconds)
                appendLine("${index + 1}. ${track.artistName} - ${track.trackName} ($duration)")
            }
        }
    }

    fun deletePlaylist(playlist: Playlist) {
        Log.d("PlaylistMenuVM", "deletePlaylist called for playlist: ${playlist.name}, id: ${playlist.id}")

        viewModelScope.launch {
            try {
                Log.d("PlaylistMenuVM", "Coroutine started for delete")

                playlistInteractor.deletePlaylist(playlist.id)

                Log.d("PlaylistMenuVM", "Playlist deleted successfully")
                _playlistDeleted.postValue(true)

            } catch (e: Exception) {
                Log.e("PlaylistMenuVM", "Error deleting playlist", e)
            }
        }
    }
}