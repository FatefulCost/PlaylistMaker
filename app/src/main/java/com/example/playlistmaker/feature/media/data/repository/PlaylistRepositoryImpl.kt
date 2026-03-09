package com.example.playlistmaker.feature.media.data.repository

import com.example.playlistmaker.feature.favorites.data.db.AppDatabase
import com.example.playlistmaker.feature.media.data.db.entity.PlaylistEntity
import com.example.playlistmaker.feature.media.domain.mapper.PlaylistMapper
import com.example.playlistmaker.feature.media.domain.model.Playlist
import com.example.playlistmaker.feature.media.domain.repository.PlaylistRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PlaylistRepositoryImpl(
    private val database: AppDatabase
) : PlaylistRepository {

    override suspend fun createPlaylist(playlist: Playlist): Long {
        val entity = PlaylistMapper.mapToEntity(playlist)
        return database.playlistDao().insertPlaylist(entity)
    }

    override suspend fun updatePlaylist(playlist: Playlist) {
        val entity = PlaylistMapper.mapToEntity(playlist)
        database.playlistDao().updatePlaylist(entity)
    }

    override fun getPlaylists(): Flow<List<Playlist>> {
        return database.playlistDao().getAllPlaylists()
            .map { entities -> PlaylistMapper.mapFromEntityToDomainList(entities) }
    }

    override suspend fun getPlaylistById(id: Long): Playlist? {
        val entity = database.playlistDao().getPlaylistById(id)
        return entity?.let { PlaylistMapper.mapFromEntityToDomain(it) }
    }

    override suspend fun getPlaylistsSync(): List<Playlist> {
        return database.playlistDao().getPlaylistsSync()
            .map { PlaylistMapper.mapFromEntityToDomain(it) }
    }

    override suspend fun addTrackToPlaylist(playlistId: Long, trackId: Long) {
        val playlist = getPlaylistById(playlistId)
        if (playlist != null && !playlist.hasTrack(trackId)) {
            val updatedPlaylist = playlist.addTrack(trackId)
            updatePlaylist(updatedPlaylist)
        }
    }
}