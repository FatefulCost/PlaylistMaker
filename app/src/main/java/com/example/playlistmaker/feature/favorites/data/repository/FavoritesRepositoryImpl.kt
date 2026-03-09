package com.example.playlistmaker.feature.favorites.data.repository

import com.example.playlistmaker.feature.favorites.data.db.AppDatabase
import com.example.playlistmaker.feature.favorites.data.db.entity.TrackEntity
import com.example.playlistmaker.feature.favorites.domain.repository.FavoritesRepository
import com.example.playlistmaker.feature.search.data.mapper.TrackMapper
import com.example.playlistmaker.feature.search.domain.model.Track
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FavoritesRepositoryImpl(
    private val database: AppDatabase
) : FavoritesRepository {

    override suspend fun addToFavorites(track: Track) {
        val trackEntity = TrackMapper.mapToEntity(track)
        database.favoritesDao().addToFavorites(trackEntity)
    }

    override suspend fun removeFromFavorites(track: Track) {
        val trackEntity = TrackMapper.mapToEntity(track)
        database.favoritesDao().removeFromFavorites(trackEntity)
    }

    override fun getFavorites(): Flow<List<Track>> {
        return database.favoritesDao().getAllFavorites()
            .map { entities -> TrackMapper.mapFromEntityToDomainList(entities) }
    }

    override suspend fun isFavorite(trackId: Long): Boolean {
        return database.favoritesDao().isFavorite(trackId)
    }

    override suspend fun getFavoriteIds(): List<Long> {
        return database.favoritesDao().getAllFavoriteIds()
    }
}