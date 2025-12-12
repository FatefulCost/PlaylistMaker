package com.example.playlistmaker.domain.repository

interface ThemeRepository {
    fun saveTheme(isDarkTheme: Boolean)
    fun isDarkTheme(): Boolean
}