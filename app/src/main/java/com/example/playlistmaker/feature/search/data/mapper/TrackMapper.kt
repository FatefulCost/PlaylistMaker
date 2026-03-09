package com.example.playlistmaker.feature.search.data.mapper

import com.example.playlistmaker.core.api.dto.TrackResponse
import com.example.playlistmaker.feature.favorites.data.db.entity.TrackEntity
import com.example.playlistmaker.feature.search.domain.model.Track

object TrackMapper {
    fun mapToDomain(trackResponse: TrackResponse): Track {
        return Track(
            trackId = trackResponse.trackId,
            trackName = trackResponse.trackName,
            artistName = trackResponse.artistName,
            trackTimeMillis = trackResponse.trackTimeMillis,
            artworkUrl100 = trackResponse.artworkUrl100,
            collectionName = trackResponse.collectionName,
            releaseDate = trackResponse.releaseDate,
            primaryGenreName = trackResponse.primaryGenreName,
            country = trackResponse.country,
            previewUrl = trackResponse.previewUrl
        )
    }

    fun mapFromResponseToDomainList(trackResponses: List<TrackResponse>): List<Track> {
        return trackResponses.map { mapToDomain(it) }
    }

    fun mapToEntity(track: Track, addedAt: Long = System.currentTimeMillis()): TrackEntity {
        return TrackEntity(
            trackId = track.trackId,
            trackName = track.trackName,
            artistName = track.artistName,
            trackTimeMillis = track.trackTimeMillis,
            artworkUrl100 = track.artworkUrl100,
            collectionName = track.collectionName,
            releaseDate = track.releaseDate,
            primaryGenreName = track.primaryGenreName,
            country = track.country,
            previewUrl = track.previewUrl,
            addedAt = addedAt
        )
    }

    fun mapFromEntityToDomain(trackEntity: TrackEntity): Track {
        return Track(
            trackId = trackEntity.trackId,
            trackName = trackEntity.trackName,
            artistName = trackEntity.artistName,
            trackTimeMillis = trackEntity.trackTimeMillis,
            artworkUrl100 = trackEntity.artworkUrl100,
            collectionName = trackEntity.collectionName,
            releaseDate = trackEntity.releaseDate,
            primaryGenreName = trackEntity.primaryGenreName,
            country = trackEntity.country,
            previewUrl = trackEntity.previewUrl
        )
    }

    fun mapFromEntityToDomainList(trackEntities: List<TrackEntity>): List<Track> {
        return trackEntities.map { mapFromEntityToDomain(it) }
    }
}