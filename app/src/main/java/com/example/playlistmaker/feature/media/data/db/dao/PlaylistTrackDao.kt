package com.example.playlistmaker.feature.media.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.playlistmaker.feature.media.data.db.entity.PlaylistTrackEntity

@Dao
interface PlaylistTrackDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrack(track: PlaylistTrackEntity)

    @Query("SELECT * FROM playlist_tracks WHERE trackId = :trackId")
    suspend fun getTrackById(trackId: Long): PlaylistTrackEntity?

    @Query("SELECT * FROM playlist_tracks WHERE trackId IN (:trackIds) ORDER BY addedAt DESC")
    suspend fun getTracksByIds(trackIds: List<Long>): List<PlaylistTrackEntity>

    @Query("DELETE FROM playlist_tracks WHERE trackId = :trackId")
    suspend fun deleteTrack(trackId: Long)
}