package com.example.playlistmaker.feature.favorites.domain.interactor.impl

import com.example.playlistmaker.feature.favorites.domain.interactor.FavoritesInteractor
import com.example.playlistmaker.feature.favorites.domain.repository.FavoritesRepository
import com.example.playlistmaker.feature.search.domain.model.Track
import kotlinx.coroutines.flow.Flow

class FavoritesInteractorImpl(
    private val favoritesRepository: FavoritesRepository
) : FavoritesInteractor {

    override suspend fun addToFavorites(track: Track) {
        favoritesRepository.addToFavorites(track)
    }

    override suspend fun removeFromFavorites(track: Track) {
        favoritesRepository.removeFromFavorites(track)
    }

    override fun getFavorites(): Flow<List<Track>> {
        return favoritesRepository.getFavorites()
    }

    override suspend fun isFavorite(trackId: Long): Boolean {
        return favoritesRepository.isFavorite(trackId)
    }

    override suspend fun toggleFavorite(track: Track): Boolean {
        val isFavorite = isFavorite(track.trackId)
        if (isFavorite) {
            removeFromFavorites(track)
        } else {
            addToFavorites(track)
        }
        return !isFavorite
    }
}