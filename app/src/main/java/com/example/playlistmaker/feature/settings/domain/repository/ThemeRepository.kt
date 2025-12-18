package com.example.playlistmaker.feature.settings.domain.repository

interface ThemeRepository {
    fun saveTheme(isDarkTheme: Boolean)
    fun isDarkTheme(): Boolean
}