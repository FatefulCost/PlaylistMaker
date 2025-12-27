package com.example.playlistmaker.app.di

import android.content.Context
import android.content.SharedPreferences
import com.example.playlistmaker.core.network.RetrofitClient
import com.example.playlistmaker.core.network.createGson
import com.example.playlistmaker.core.network.createOkHttpClient
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
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

val featureModule = module {

    // Создаем Gson
    single<Gson> { createGson() }

    // Создаем OkHttpClient
    single<OkHttpClient> { createOkHttpClient() }

    // Создаем RetrofitClient
    single { RetrofitClient(get(), get()) }

    // Создаем ITunesApiService через RetrofitClient
    single { get<RetrofitClient>().createApiService() }

    // search
    // Репозитории
    single<TrackRepository> {
        TrackRepositoryImpl(get())
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
    viewModel { SearchViewModel(get(), get()) }


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

    // Медиатека
    viewModel { com.example.playlistmaker.feature.media.ui.viewmodels.PlaylistsViewModel() }
    viewModel { com.example.playlistmaker.feature.media.ui.viewmodels.FavoritesViewModel() }
}