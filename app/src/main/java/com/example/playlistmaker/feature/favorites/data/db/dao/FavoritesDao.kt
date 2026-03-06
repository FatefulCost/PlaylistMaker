package com.example.playlistmaker.feature.favorites.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Delete
import androidx.room.Query
import com.example.playlistmaker.feature.favorites.data.db.entity.TrackEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoritesDao {
    @Insert
    suspend fun addToFavorites(track: TrackEntity)

    @Delete
    suspend fun removeFromFavorites(track: TrackEntity)

    @Query("SELECT * FROM favorite_tracks ORDER BY addedAt DESC")
    fun getAllFavorites(): Flow<List<TrackEntity>>

    @Query("SELECT * FROM favorite_tracks WHERE trackId = :trackId")
    suspend fun getFavoriteTrackById(trackId: Long): TrackEntity?

    @Query("SELECT trackId FROM favorite_tracks")
    suspend fun getAllFavoriteIds(): List<Long>

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_tracks WHERE trackId = :trackId)")
    suspend fun isFavorite(trackId: Long): Boolean
}