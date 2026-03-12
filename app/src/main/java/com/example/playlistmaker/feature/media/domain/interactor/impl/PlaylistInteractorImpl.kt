package com.example.playlistmaker.feature.media.domain.interactor.impl

import com.example.playlistmaker.feature.media.domain.interactor.PlaylistInteractor
import com.example.playlistmaker.feature.media.domain.model.Playlist
import com.example.playlistmaker.feature.media.domain.repository.PlaylistRepository
import kotlinx.coroutines.flow.Flow

class PlaylistInteractorImpl(
    private val playlistRepository: PlaylistRepository
) : PlaylistInteractor {

    override suspend fun createPlaylist(playlist: Playlist): Long {
        return playlistRepository.createPlaylist(playlist)
    }

    override suspend fun updatePlaylist(playlist: Playlist) {
        playlistRepository.updatePlaylist(playlist)
    }

    override fun getPlaylists(): Flow<List<Playlist>> {
        return playlistRepository.getPlaylists()
    }

    override suspend fun getPlaylistById(id: Long): Playlist? {
        return playlistRepository.getPlaylistById(id)
    }

    override suspend fun getPlaylistsSync(): List<Playlist> {
        return playlistRepository.getPlaylistsSync()
    }

    override suspend fun addTrackToPlaylist(playlistId: Long, trackId: Long) {
        playlistRepository.addTrackToPlaylist(playlistId, trackId)
    }

    override suspend fun isTrackInPlaylist(playlistId: Long, trackId: Long): Boolean {
        val playlist = playlistRepository.getPlaylistById(playlistId)
        return playlist?.hasTrack(trackId) == true
    }
}