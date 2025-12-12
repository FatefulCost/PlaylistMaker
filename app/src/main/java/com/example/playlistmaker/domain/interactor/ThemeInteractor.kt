package com.example.playlistmaker.domain.interactor

interface ThemeInteractor {
    fun saveTheme(isDarkTheme: Boolean)
    fun isDarkTheme(): Boolean
    fun toggleTheme(): Boolean
}