package com.example.playlistmaker.feature.search.domain.repository

import com.example.playlistmaker.feature.search.domain.model.Track

interface HistoryRepository {
    fun addTrackToHistory(track: Track)
    fun getSearchHistory(): List<Track>
    fun clearSearchHistory()
    fun isHistoryEmpty(): Boolean
}