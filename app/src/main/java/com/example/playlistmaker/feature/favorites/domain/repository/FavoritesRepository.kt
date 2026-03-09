package com.example.playlistmaker.feature.favorites.domain.repository

import com.example.playlistmaker.feature.search.domain.model.Track
import kotlinx.coroutines.flow.Flow

interface FavoritesRepository {
    suspend fun addToFavorites(track: Track)
    suspend fun removeFromFavorites(track: Track)
    fun getFavorites(): Flow<List<Track>>
    suspend fun isFavorite(trackId: Long): Boolean
    suspend fun getFavoriteIds(): List<Long>
}