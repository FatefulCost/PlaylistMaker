package com.example.playlistmaker.core.theme

import androidx.appcompat.app.AppCompatDelegate
import com.example.playlistmaker.feature.settings.domain.interactor.ThemeInteractor

class ThemeManager(
    private val themeInteractor: ThemeInteractor
) {

    fun applySavedTheme() {
        val isDarkTheme = themeInteractor.isDarkTheme()
        applyTheme(isDarkTheme)
    }

    fun applyTheme(isDarkTheme: Boolean) {
        if (isDarkTheme) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }

    fun isDarkTheme(): Boolean {
        return themeInteractor.isDarkTheme()
    }

    fun saveTheme(isDarkTheme: Boolean) {
        themeInteractor.saveTheme(isDarkTheme)
    }
}