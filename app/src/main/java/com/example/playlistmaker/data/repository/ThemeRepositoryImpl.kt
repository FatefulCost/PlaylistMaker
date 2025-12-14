package com.example.playlistmaker.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.playlistmaker.domain.repository.ThemeRepository

class ThemeRepositoryImpl(
    private val context: Context
) : ThemeRepository {

    companion object {
        private const val PREFS_NAME = "theme_prefs"
        private const val KEY_IS_DARK_THEME = "is_dark_theme"
    }

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun saveTheme(isDarkTheme: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_IS_DARK_THEME, isDarkTheme).apply()
    }

    override fun isDarkTheme(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_DARK_THEME, false)
    }
}