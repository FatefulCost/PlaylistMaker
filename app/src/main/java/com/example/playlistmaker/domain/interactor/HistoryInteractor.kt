package com.example.playlistmaker.domain.interactor

import com.example.playlistmaker.domain.model.Track

interface HistoryInteractor {
    fun addTrackToHistory(track: Track)
    fun getSearchHistory(): List<Track>
    fun clearSearchHistory()
    fun isHistoryEmpty(): Boolean
}