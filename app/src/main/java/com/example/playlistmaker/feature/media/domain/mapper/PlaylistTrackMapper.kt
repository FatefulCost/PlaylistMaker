package com.example.playlistmaker.feature.media.domain.mapper

import com.example.playlistmaker.feature.media.data.db.entity.PlaylistTrackEntity
import com.example.playlistmaker.feature.search.domain.model.Track

object PlaylistTrackMapper {
    fun mapToEntity(track: Track): PlaylistTrackEntity {
        return PlaylistTrackEntity(
            trackId = track.trackId,
            trackName = track.trackName,
            artistName = track.artistName,
            trackTimeMillis = track.trackTimeMillis,
            artworkUrl100 = track.artworkUrl100,
            collectionName = track.collectionName,
            releaseDate = track.releaseDate,
            primaryGenreName = track.primaryGenreName,
            country = track.country,
            previewUrl = track.previewUrl
        )
    }

    fun mapToDomain(entity: PlaylistTrackEntity): Track {
        return Track(
            trackId = entity.trackId,
            trackName = entity.trackName,
            artistName = entity.artistName,
            trackTimeMillis = entity.trackTimeMillis,
            artworkUrl100 = entity.artworkUrl100,
            collectionName = entity.collectionName,
            releaseDate = entity.releaseDate,
            primaryGenreName = entity.primaryGenreName,
            country = entity.country,
            previewUrl = entity.previewUrl
        )
    }

    fun mapToDomainList(entities: List<PlaylistTrackEntity>): List<Track> {
        return entities.map { mapToDomain(it) }
    }
}