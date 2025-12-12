package com.example.playlistmaker.domain.repository

import com.example.playlistmaker.domain.model.Track

interface HistoryRepository {
    fun addTrackToHistory(track: Track)
    fun getSearchHistory(): List<Track>
    fun clearSearchHistory()
    fun isHistoryEmpty(): Boolean
}