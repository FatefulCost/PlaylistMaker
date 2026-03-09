package com.example.playlistmaker.feature.media.domain.interactor

import com.example.playlistmaker.feature.media.domain.model.Playlist
import kotlinx.coroutines.flow.Flow

interface PlaylistInteractor {
    suspend fun createPlaylist(playlist: Playlist): Long
    suspend fun updatePlaylist(playlist: Playlist)
    fun getPlaylists(): Flow<List<Playlist>>
    suspend fun getPlaylistById(id: Long): Playlist?
    suspend fun getPlaylistsSync(): List<Playlist>
    suspend fun addTrackToPlaylist(playlistId: Long, trackId: Long)
    suspend fun isTrackInPlaylist(playlistId: Long, trackId: Long): Boolean
}