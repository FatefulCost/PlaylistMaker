package com.example.playlistmaker.feature.favorites.domain.interactor

import com.example.playlistmaker.feature.search.domain.model.Track
import kotlinx.coroutines.flow.Flow

interface FavoritesInteractor {
    suspend fun addToFavorites(track: Track)
    suspend fun removeFromFavorites(track: Track)
    fun getFavorites(): Flow<List<Track>>
    suspend fun isFavorite(trackId: Long): Boolean
    suspend fun toggleFavorite(track: Track): Boolean
}