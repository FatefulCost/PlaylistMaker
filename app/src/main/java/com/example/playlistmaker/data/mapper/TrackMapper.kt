package com.example.playlistmaker.data.mapper

import com.example.playlistmaker.data.api.dto.TrackResponse
import com.example.playlistmaker.domain.model.Track

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

    fun mapToDomainList(trackResponses: List<TrackResponse>): List<Track> {
        return trackResponses.map { mapToDomain(it) }
    }
}