package com.example.playlistmaker.app.di

import android.content.Context
import android.content.SharedPreferences
import com.example.playlistmaker.core.network.RetrofitClient
import com.example.playlistmaker.core.network.createGson
import com.example.playlistmaker.core.network.createOkHttpClient
import com.example.playlistmaker.core.theme.ThemeManager
import com.example.playlistmaker.feature.favorites.data.db.AppDatabase
import com.example.playlistmaker.feature.favorites.data.repository.FavoritesRepositoryImpl
import com.example.playlistmaker.feature.favorites.domain.interactor.FavoritesInteractor
import com.example.playlistmaker.feature.favorites.domain.interactor.impl.FavoritesInteractorImpl
import com.example.playlistmaker.feature.favorites.domain.repository.FavoritesRepository
import com.example.playlistmaker.feature.media.data.repository.PlaylistRepositoryImpl
import com.example.playlistmaker.feature.media.domain.interactor.PlaylistInteractor
import com.example.playlistmaker.feature.media.domain.interactor.impl.PlaylistInteractorImpl
import com.example.playlistmaker.feature.media.domain.repository.PlaylistRepository
import com.example.playlistmaker.feature.media.ui.viewmodels.CreatePlaylistViewModel
import com.example.playlistmaker.feature.media.ui.viewmodels.FavoritesViewModel
import com.example.playlistmaker.feature.media.ui.viewmodels.PlaylistsViewModel
import com.example.playlistmaker.feature.player.ui.viewmodel.PlayerViewModel
import com.example.playlistmaker.feature.player.ui.viewmodel.PlaylistBottomSheetViewModel
import com.example.playlistmaker.feature.player.ui.viewmodel.PlaylistMenuViewModel
import com.example.playlistmaker.feature.player.ui.viewmodel.PlaylistViewModel
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
import com.google.gson.Gson
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

val featureModule = module {

    // Создаем Gson
    single<Gson> { createGson() }

    // Создаем OkHttpClient
    single<OkHttpClient> { createOkHttpClient() }

    // Создаем RetrofitClient
    single { RetrofitClient(get(), get()) }

    // Создаем ITunesApiService через RetrofitClient
    single { get<RetrofitClient>().createApiService() }

    // База данных
    single { AppDatabase.getInstance(androidContext()) }

    // search
    // Репозитории
    single<TrackRepository> {
        TrackRepositoryImpl(
            apiService = get(),
            favoritesRepository = get() // Добавляем зависимость от FavoritesRepository
        )
    }

    single<HistoryRepository> {
        HistoryRepositoryImpl(get(named("history_prefs")), get())
    }

    // Интеракторы
    single<SearchInteractor> {
        SearchInteractorImpl(get())
    }

    single<HistoryInteractor> {
        HistoryInteractorImpl(get())
    }

    // ViewModel
    viewModel { SearchViewModel(get(), get(), get()) }


    // settings
    // Репозиторий
    single<ThemeRepository> {
        ThemeRepositoryImpl(get(named("theme_prefs")))
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


    // favorites
    // Репозиторий
    single<FavoritesRepository> {
        FavoritesRepositoryImpl(get())
    }

    // Интерактор
    single<FavoritesInteractor> {
        FavoritesInteractorImpl(get())
    }

    // Создаем общий SharedPreferences
    single<SharedPreferences> {
        androidContext().getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
    }

    // Создаем History SharedPreferences с квалификатором
    single<SharedPreferences>(named("history_prefs")) {
        androidContext().getSharedPreferences("search_history_prefs", Context.MODE_PRIVATE)
    }

    // Создаем Theme SharedPreferences с квалификатором
    single<SharedPreferences>(named("theme_prefs")) {
        androidContext().getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)
    }



    // playlists
    single<PlaylistRepository> {
        PlaylistRepositoryImpl(get())
    }

    single<PlaylistInteractor> {
        PlaylistInteractorImpl(get(), get())
    }

    // Медиатека
    viewModel { PlaylistsViewModel(get()) }
    viewModel { CreatePlaylistViewModel(get()) }
    viewModel { FavoritesViewModel(get()) }
    viewModel { PlayerViewModel(get()) }
    viewModel { PlaylistBottomSheetViewModel(get()) }
    viewModel { PlaylistViewModel(get()) }
    viewModel { PlaylistMenuViewModel(get()) }
}