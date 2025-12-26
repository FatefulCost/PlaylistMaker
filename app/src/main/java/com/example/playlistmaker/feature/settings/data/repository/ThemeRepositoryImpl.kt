package com.example.playlistmaker.feature.settings.data.repository

import android.content.SharedPreferences
import com.example.playlistmaker.feature.settings.domain.repository.ThemeRepository

class ThemeRepositoryImpl(
    private val sharedPreferences: SharedPreferences
) : ThemeRepository {

    companion object {
        private const val KEY_IS_DARK_THEME = "is_dark_theme"
    }

    override fun saveTheme(isDarkTheme: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_IS_DARK_THEME, isDarkTheme).apply()
    }

    override fun isDarkTheme(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_DARK_THEME, false)
    }
}