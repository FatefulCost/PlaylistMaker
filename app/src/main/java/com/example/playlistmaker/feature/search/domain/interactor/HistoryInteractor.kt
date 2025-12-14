package com.example.playlistmaker.feature.search.domain.interactor

import com.example.playlistmaker.feature.search.domain.model.Track

interface HistoryInteractor {
    fun addTrackToHistory(track: Track)
    fun getSearchHistory(): List<Track>
    fun clearSearchHistory()
}