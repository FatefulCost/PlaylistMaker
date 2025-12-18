package com.example.playlistmaker.app.di

import android.content.Context
import com.example.playlistmaker.core.theme.ThemeManager
import com.example.playlistmaker.feature.search.data.repository.HistoryRepositoryImpl
import com.example.playlistmaker.feature.search.data.repository.TrackRepositoryImpl
import com.example.playlistmaker.feature.search.domain.interactor.HistoryInteractor
import com.example.playlistmaker.feature.search.domain.interactor.SearchInteractor
import com.example.playlistmaker.feature.search.domain.interactor.impl.HistoryInteractorImpl
import com.example.playlistmaker.feature.search.domain.interactor.impl.SearchInteractorImpl
import com.example.playlistmaker.feature.search.domain.repository.HistoryRepository
import com.example.playlistmaker.feature.search.domain.repository.TrackRepository
import com.example.playlistmaker.feature.search.ui.viewmodel.SearchViewModel
import com.example.playlistmaker.feature.settings.data.repository.ThemeRepositoryImpl
import com.example.playlistmaker.feature.settings.domain.interactor.ThemeInteractor
import com.example.playlistmaker.feature.settings.domain.interactor.impl.ThemeInteractorImpl
import com.example.playlistmaker.feature.settings.domain.repository.ThemeRepository
import com.example.playlistmaker.feature.settings.ui.viewmodel.SettingsViewModel
import com.example.playlistmaker.feature.sharing.data.repository.SharingRepositoryImpl
import com.example.playlistmaker.feature.sharing.domain.interactor.SharingInteractor
import com.example.playlistmaker.feature.sharing.domain.interactor.impl.SharingInteractorImpl
import com.example.playlistmaker.feature.sharing.domain.repository.SharingRepository
import com.example.playlistmaker.feature.sharing.ui.viewmodel.SharingViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val featureModule = module {

    // search
    // Репозитории
    single<TrackRepository> {
        TrackRepositoryImpl(get())
    }

    single<HistoryRepository> {
        HistoryRepositoryImpl(androidContext())
    }

    // Интеракторы
    single<SearchInteractor> {
        SearchInteractorImpl(get())
    }

    single<HistoryInteractor> {
        HistoryInteractorImpl(get())
    }

    // ViewModel
    viewModel { SearchViewModel(get(), get()) }


    // settings
    // Репозиторий
    single<ThemeRepository> {
        ThemeRepositoryImpl(androidContext())
    }

    // Интерактор
    single<ThemeInteractor> {
        ThemeInteractorImpl(get())
    }

    // ViewModel
    viewModel { SettingsViewModel(get()) }


    // sharing
    // Репозиторий
    single<SharingRepository> {
        SharingRepositoryImpl()
    }

    // Интерактор
    single<SharingInteractor> {
        SharingInteractorImpl(get())
    }

    // ViewModel
    viewModel { SharingViewModel(get()) }

    // Тема
    single { ThemeManager(get()) }

}