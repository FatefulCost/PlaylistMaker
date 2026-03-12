package com.example.playlistmaker.feature.player.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.feature.media.domain.interactor.PlaylistInteractor
import com.example.playlistmaker.feature.media.domain.model.Playlist
import com.example.playlistmaker.feature.search.domain.model.Track
import kotlinx.coroutines.launch

class PlaylistViewModel(
    private val playlistInteractor: PlaylistInteractor
) : ViewModel() {

    private val _playlist = MutableLiveData<Playlist?>()
    val playlist: LiveData<Playlist?> = _playlist

    private val _tracks = MutableLiveData<List<Track>>()
    val tracks: LiveData<List<Track>> = _tracks

    private val _totalDuration = MutableLiveData<String>()
    val totalDuration: LiveData<String> = _totalDuration

    private val _shareText = MutableLiveData<String?>()
    val shareText: LiveData<String?> = _shareText

    private val _showEmptyShareMessage = MutableLiveData<Boolean>()
    val showEmptyShareMessage: LiveData<Boolean> = _showEmptyShareMessage

    private val _trackRemoved = MutableLiveData<Boolean>()
    val trackRemoved: LiveData<Boolean> = _trackRemoved

    private val _playlistDeleted = MutableLiveData<Boolean>()
    val playlistDeleted: LiveData<Boolean> = _playlistDeleted

    private var currentPlaylist: Playlist? = null
    private var currentTracks: List<Track> = emptyList()

    fun loadPlaylist(playlistId: Long) {
        viewModelScope.launch {
            try {
                val playlist = playlistInteractor.getPlaylistById(playlistId)
                _playlist.value = playlist
                currentPlaylist = playlist

                playlist?.let {
                    val tracks = playlistInteractor.getPlaylistTracks(playlistId)
                    currentTracks = tracks
                    _tracks.value = tracks
                    calculateTotalDuration(tracks)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun calculateTotalDuration(tracks: List<Track>) {
        if (tracks.isEmpty()) {
            _totalDuration.value = "0 минут"
            return
        }

        val totalSeconds = tracks.sumOf { it.trackTimeMillis } / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60

        val durationText = when {
            hours > 0 -> "$hours ч $minutes мин"
            else -> "$minutes минут"
        }
        _totalDuration.value = durationText
    }

    fun sharePlaylist(playlist: Playlist) {
        if (currentTracks.isEmpty()) {
            _showEmptyShareMessage.value = true
            _showEmptyShareMessage.value = false
            return
        }

        val shareText = buildString {
            appendLine(playlist.name)
            if (!playlist.description.isNullOrEmpty()) {
                appendLine(playlist.description)
            }
            appendLine("${currentTracks.size} треков")
            appendLine()

            currentTracks.forEachIndexed { index, track ->
                val minutes = track.trackTimeMillis / 60000
                val seconds = (track.trackTimeMillis % 60000) / 1000
                val duration = String.format("%d:%02d", minutes, seconds)
                appendLine("${index + 1}. ${track.artistName} - ${track.trackName} ($duration)")
            }
        }

        _shareText.value = shareText
        _shareText.value = null
    }

    fun removeTrackFromPlaylist(playlistId: Long, trackId: Long) {
        viewModelScope.launch {
            try {
                playlistInteractor.removeTrackFromPlaylist(playlistId, trackId)
                loadPlaylist(playlistId)
                _trackRemoved.value = true
                _trackRemoved.value = false
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}