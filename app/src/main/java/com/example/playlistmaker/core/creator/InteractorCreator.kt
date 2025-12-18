package com.example.playlistmaker.core.creator

import android.content.Context
import com.example.playlistmaker.core.api.service.ITunesApiService
import com.example.playlistmaker.feature.search.data.repository.HistoryRepositoryImpl
import com.example.playlistmaker.feature.search.data.repository.TrackRepositoryImpl
import com.example.playlistmaker.feature.search.domain.interactor.HistoryInteractor
import com.example.playlistmaker.feature.search.domain.interactor.SearchInteractor
import com.example.playlistmaker.feature.search.domain.interactor.impl.HistoryInteractorImpl
import com.example.playlistmaker.feature.search.domain.interactor.impl.SearchInteractorImpl
import com.example.playlistmaker.feature.settings.data.repository.ThemeRepositoryImpl
import com.example.playlistmaker.feature.settings.domain.interactor.ThemeInteractor
import com.example.playlistmaker.feature.settings.domain.interactor.impl.ThemeInteractorImpl
import com.example.playlistmaker.feature.sharing.data.repository.SharingRepositoryImpl
import com.example.playlistmaker.feature.sharing.domain.interactor.SharingInteractor
import com.example.playlistmaker.feature.sharing.domain.interactor.impl.SharingInteractorImpl

object InteractorCreator {

    fun createSearchInteractor(apiService: ITunesApiService): SearchInteractor {
        return SearchInteractorImpl(
            trackRepository = TrackRepositoryImpl(apiService)
        )
    }

    fun createHistoryInteractor(context: Context): HistoryInteractor {
        return HistoryInteractorImpl(
            historyRepository = HistoryRepositoryImpl(context)
        )
    }

    fun createThemeInteractor(context: Context): ThemeInteractor {
        return ThemeInteractorImpl(
            themeRepository = ThemeRepositoryImpl(context)
        )
    }

    fun createSharingInteractor(context: Context): SharingInteractor {
        return SharingInteractorImpl(
            sharingRepository = SharingRepositoryImpl(context)
        )
    }
}