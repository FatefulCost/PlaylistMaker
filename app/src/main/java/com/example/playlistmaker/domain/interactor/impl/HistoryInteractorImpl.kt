package com.example.playlistmaker.domain.interactor.impl

import com.example.playlistmaker.domain.interactor.HistoryInteractor
import com.example.playlistmaker.domain.model.Track
import com.example.playlistmaker.domain.repository.HistoryRepository

class HistoryInteractorImpl(
    private val historyRepository: HistoryRepository
) : HistoryInteractor {

    override fun addTrackToHistory(track: Track) {
        historyRepository.addTrackToHistory(track)
    }

    override fun getSearchHistory(): List<Track> {
        return historyRepository.getSearchHistory()
    }

    override fun clearSearchHistory() {
        historyRepository.clearSearchHistory()
    }

    override fun isHistoryEmpty(): Boolean {
        return historyRepository.isHistoryEmpty()
    }
}