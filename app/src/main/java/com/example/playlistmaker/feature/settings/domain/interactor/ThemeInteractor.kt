package com.example.playlistmaker.feature.settings.domain.interactor

interface ThemeInteractor {
    fun saveTheme(isDarkTheme: Boolean)
    fun isDarkTheme(): Boolean
    fun toggleTheme(): Boolean
}