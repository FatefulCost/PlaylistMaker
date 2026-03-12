package com.example.playlistmaker.feature.favorites.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.playlistmaker.feature.favorites.data.db.entity.TrackEntity

@Dao
interface TrackDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrack(track: TrackEntity)

    @Query("SELECT * FROM favorite_tracks WHERE trackId = :trackId")
    suspend fun getTrackById(trackId: Long): TrackEntity?

    @Query("SELECT * FROM favorite_tracks WHERE trackId IN (:trackIds)")
    suspend fun getTracksByIds(trackIds: List<Long>): List<TrackEntity>
}