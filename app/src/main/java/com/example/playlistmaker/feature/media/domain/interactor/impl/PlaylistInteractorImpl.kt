package com.example.playlistmaker.feature.media.domain.interactor.impl

import com.example.playlistmaker.feature.favorites.domain.repository.FavoritesRepository
import com.example.playlistmaker.feature.media.domain.interactor.PlaylistInteractor
import com.example.playlistmaker.feature.media.domain.model.Playlist
import com.example.playlistmaker.feature.media.domain.repository.PlaylistRepository
import com.example.playlistmaker.feature.search.domain.model.Track
import kotlinx.coroutines.flow.Flow
import android.util.Log

class PlaylistInteractorImpl(
    private val playlistRepository: PlaylistRepository,
    private val favoritesRepository: FavoritesRepository
) : PlaylistInteractor {

    companion object {
        private const val TAG = "PlaylistInteractor"
    }

    override suspend fun createPlaylist(playlist: Playlist): Long {
        Log.d(TAG, "createPlaylist: ${playlist.name}")
        return playlistRepository.createPlaylist(playlist)
    }

    override suspend fun updatePlaylist(playlist: Playlist) {
        Log.d(TAG, "updatePlaylist: ${playlist.name}")
        playlistRepository.updatePlaylist(playlist)
    }

    override fun getPlaylists(): Flow<List<Playlist>> {
        return playlistRepository.getPlaylists()
    }

    override suspend fun getPlaylistById(id: Long): Playlist? {
        Log.d(TAG, "getPlaylistById: $id")
        return playlistRepository.getPlaylistById(id)
    }

    override suspend fun getPlaylistsSync(): List<Playlist> {
        return playlistRepository.getPlaylistsSync()
    }

    override suspend fun addTrackToPlaylist(playlistId: Long, trackId: Long) {
        Log.d(TAG, "addTrackToPlaylist: playlistId=$playlistId, trackId=$trackId")
        playlistRepository.addTrackToPlaylist(playlistId, trackId)
    }

    override suspend fun isTrackInPlaylist(playlistId: Long, trackId: Long): Boolean {
        return playlistRepository.isTrackInPlaylist(playlistId, trackId)
    }

    override suspend fun deletePlaylist(playlistId: Long) {
        Log.d("PlaylistInteractor", "========== DELETE PLAYLIST CALLED ==========")
        Log.d("PlaylistInteractor", "deletePlaylist called with id: $playlistId")
        Log.d("PlaylistInteractor", "Thread: ${Thread.currentThread().name}")

        try {
            Log.d("PlaylistInteractor", "Calling playlistRepository.deletePlaylist")
            playlistRepository.deletePlaylist(playlistId)

            Log.d("PlaylistInteractor", "deletePlaylist completed successfully for id: $playlistId")
            Log.d("PlaylistInteractor", "========== DELETE PLAYLIST COMPLETED ==========")
        } catch (e: Exception) {
            Log.e("PlaylistInteractor", "Error in deletePlaylist for id: $playlistId", e)
            throw e
        }
    }

    override suspend fun removeTrackFromPlaylist(playlistId: Long, trackId: Long) {
        Log.d(TAG, "removeTrackFromPlaylist: playlistId=$playlistId, trackId=$trackId")
        playlistRepository.removeTrackFromPlaylist(playlistId, trackId)
    }

    override suspend fun getPlaylistTracks(playlistId: Long): List<Track> {
        Log.d(TAG, "getPlaylistTracks: $playlistId")
        val playlist = getPlaylistById(playlistId) ?: return emptyList()
        Log.d(TAG, "Playlist found, trackIds: ${playlist.tracksIds}")
        val tracks = playlistRepository.getTracksForPlaylist(playlist.tracksIds)
        Log.d(TAG, "Found ${tracks.size} tracks")
        return tracks
    }

    override suspend fun saveTrackToPlaylist(track: Track) {
        Log.d(TAG, "saveTrackToPlaylist: ${track.trackId} - ${track.trackName}")
        playlistRepository.saveTrackToPlaylist(track)
    }
}