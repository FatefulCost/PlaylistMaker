package com.example.playlistmaker.presentation.creator

import android.content.Context
import com.example.playlistmaker.data.api.service.ITunesApiService
import com.example.playlistmaker.data.repository.HistoryRepositoryImpl
import com.example.playlistmaker.data.repository.ThemeRepositoryImpl
import com.example.playlistmaker.data.repository.TrackRepositoryImpl
import com.example.playlistmaker.domain.interactor.HistoryInteractor
import com.example.playlistmaker.domain.interactor.SearchInteractor
import com.example.playlistmaker.domain.interactor.ThemeInteractor
import com.example.playlistmaker.domain.interactor.impl.HistoryInteractorImpl
import com.example.playlistmaker.domain.interactor.impl.SearchInteractorImpl
import com.example.playlistmaker.domain.interactor.impl.ThemeInteractorImpl

object InteractorCreator {

    private var searchInteractor: SearchInteractor? = null
    private var historyInteractor: HistoryInteractor? = null
    private var themeInteractor: ThemeInteractor? = null

    fun createSearchInteractor(apiService: ITunesApiService): SearchInteractor {
        return searchInteractor ?: synchronized(this) {
            searchInteractor ?: SearchInteractorImpl(
                trackRepository = TrackRepositoryImpl(apiService)
            ).also { searchInteractor = it }
        }
    }

    fun createHistoryInteractor(context: Context): HistoryInteractor {
        return historyInteractor ?: synchronized(this) {
            historyInteractor ?: HistoryInteractorImpl(
                historyRepository = HistoryRepositoryImpl(context)
            ).also { historyInteractor = it }
        }
    }

    fun createThemeInteractor(context: Context): ThemeInteractor {
        return themeInteractor ?: synchronized(this) {
            themeInteractor ?: ThemeInteractorImpl(
                themeRepository = ThemeRepositoryImpl(context)
            ).also { themeInteractor = it }
        }
    }
}