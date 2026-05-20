package com.example.playlistmaker.feature.media.data.repository

import com.example.playlistmaker.feature.favorites.data.db.AppDatabase
import com.example.playlistmaker.feature.media.data.db.entity.PlaylistEntity
import com.example.playlistmaker.feature.media.domain.mapper.PlaylistMapper
import com.example.playlistmaker.feature.media.domain.mapper.PlaylistTrackMapper
import com.example.playlistmaker.feature.media.domain.model.Playlist
import com.example.playlistmaker.feature.media.domain.repository.PlaylistRepository
import com.example.playlistmaker.feature.search.domain.model.Track
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import android.util.Log

class PlaylistRepositoryImpl(
    private val database: AppDatabase
) : PlaylistRepository {

    companion object {
        private const val TAG = "PlaylistRepository"
    }

    override suspend fun createPlaylist(playlist: Playlist): Long {
        Log.d(TAG, "createPlaylist: ${playlist.name}")
        val entity = PlaylistMapper.mapToEntity(playlist)
        return database.playlistDao().insertPlaylist(entity)
    }

    override suspend fun updatePlaylist(playlist: Playlist) {
        Log.d(TAG, "updatePlaylist: ${playlist.name}")
        val entity = PlaylistMapper.mapToEntity(playlist)
        database.playlistDao().updatePlaylist(entity)
    }

    override fun getPlaylists(): Flow<List<Playlist>> {
        return database.playlistDao().getAllPlaylists()
            .map { entities ->
                Log.d(TAG, "getPlaylists: found ${entities.size} playlists")
                PlaylistMapper.mapFromEntityToDomainList(entities)
            }
    }

    override suspend fun getPlaylistById(id: Long): Playlist? {
        Log.d(TAG, "getPlaylistById: $id")
        val entity = database.playlistDao().getPlaylistById(id)
        return entity?.let {
            Log.d(TAG, "Found playlist: ${it.name}")
            PlaylistMapper.mapFromEntityToDomain(it)
        }
    }

    override suspend fun getPlaylistsSync(): List<Playlist> {
        val entities = database.playlistDao().getPlaylistsSync()
        Log.d(TAG, "getPlaylistsSync: found ${entities.size} playlists")
        return entities.map { PlaylistMapper.mapFromEntityToDomain(it) }
    }

    override suspend fun addTrackToPlaylist(playlistId: Long, trackId: Long) {
        Log.d(TAG, "addTrackToPlaylist: playlistId=$playlistId, trackId=$trackId")
        val playlist = getPlaylistById(playlistId)
        if (playlist != null && !playlist.hasTrack(trackId)) {
            val updatedPlaylist = playlist.addTrack(trackId)
            updatePlaylist(updatedPlaylist)
            Log.d(TAG, "Track added successfully")
        }
    }

    override suspend fun isTrackInPlaylist(playlistId: Long, trackId: Long): Boolean {
        val playlist = getPlaylistById(playlistId)
        return playlist?.hasTrack(trackId) == true
    }

    override suspend fun deletePlaylist(playlistId: Long) {
        Log.d("PlaylistRepository", "========== DELETE PLAYLIST REPOSITORY START ==========")
        Log.d("PlaylistRepository", "deletePlaylist called with id: $playlistId")
        Log.d("PlaylistRepository", "Thread: ${Thread.currentThread().name}")

        try {
            // Получаем плейлист для удаления обложки
            val playlist = getPlaylistById(playlistId)

            if (playlist == null) {
                Log.e("PlaylistRepository", "Playlist with id $playlistId not found")
                return
            }

            Log.d("PlaylistRepository", "Playlist found: ${playlist.name}, id: ${playlist.id}")

            val dao = database.playlistDao()
            dao.deletePlaylistById(playlistId)
            Log.d("PlaylistRepository", "dao.deletePlaylistById() completed")

            // Удаляем обложку
            playlist.coverPath?.let { coverPath ->
                try {
                    val coverFile = java.io.File(coverPath)
                    if (coverFile.exists()) {
                        coverFile.delete()
                        Log.d("PlaylistRepository", "Cover file deleted")
                    }
                } catch (e: Exception) {
                    Log.e("PlaylistRepository", "Error deleting cover file", e)
                }
            }

            Log.d("PlaylistRepository", "========== DELETE PLAYLIST REPOSITORY END ==========")

        } catch (e: Exception) {
            Log.e("PlaylistRepository", "========== DELETE PLAYLIST REPOSITORY ERROR ==========", e)
            throw e
        }
    }

    override suspend fun removeTrackFromPlaylist(playlistId: Long, trackId: Long) {
        Log.d(TAG, "removeTrackFromPlaylist: playlistId=$playlistId, trackId=$trackId")
        val playlist = getPlaylistById(playlistId)
        if (playlist != null) {
            val updatedTracks = playlist.tracksIds.toMutableList()
            updatedTracks.remove(trackId)
            val updatedPlaylist = playlist.copy(
                tracksIds = updatedTracks,
                tracksCount = updatedTracks.size
            )
            updatePlaylist(updatedPlaylist)
            Log.d(TAG, "Track removed successfully")
        }
    }

    override suspend fun getTracksForPlaylist(trackIds: List<Long>): List<Track> {
        if (trackIds.isEmpty()) {
            Log.d(TAG, "getTracksForPlaylist: trackIds is empty")
            return emptyList()
        }

        Log.d(TAG, "getTracksForPlaylist: getting ${trackIds.size} tracks")
        val trackEntities = database.playlistTrackDao().getTracksByIds(trackIds)
        Log.d(TAG, "Found ${trackEntities.size} track entities")

        val tracks = PlaylistTrackMapper.mapToDomainList(trackEntities)
        Log.d(TAG, "Mapped to ${tracks.size} tracks")
        return tracks
    }

    override suspend fun saveTrackToPlaylist(track: Track) {
        Log.d(TAG, "saveTrackToPlaylist: ${track.trackId} - ${track.trackName}")
        val trackEntity = PlaylistTrackMapper.mapToEntity(track)
        database.playlistTrackDao().insertTrack(trackEntity)
    }
}