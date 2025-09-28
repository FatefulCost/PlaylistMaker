package com.example.playlistmaker

import android.content.Context
import android.content.SharedPreferences

class ThemeManager(private val context: Context) {

    companion object {
        private const val PREFS_NAME = "theme_prefs"
        private const val KEY_IS_DARK_THEME = "is_dark_theme"
    }

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun saveTheme(isDarkTheme: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_IS_DARK_THEME, isDarkTheme).apply()
    }

    fun isDarkTheme(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_DARK_THEME, false)
    }
}