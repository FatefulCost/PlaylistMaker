package com.example.playlistmaker.feature.search.domain.repository

import com.example.playlistmaker.feature.search.domain.model.Track

interface TrackRepository {
    suspend fun searchTracks(query: String): List<Track>
}