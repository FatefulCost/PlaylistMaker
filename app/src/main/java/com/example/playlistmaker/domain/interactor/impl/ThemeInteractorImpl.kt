package com.example.playlistmaker.domain.interactor.impl

import com.example.playlistmaker.domain.interactor.ThemeInteractor
import com.example.playlistmaker.domain.repository.ThemeRepository

class ThemeInteractorImpl(
    private val themeRepository: ThemeRepository
) : ThemeInteractor {

    override fun saveTheme(isDarkTheme: Boolean) {
        themeRepository.saveTheme(isDarkTheme)
    }

    override fun isDarkTheme(): Boolean {
        return themeRepository.isDarkTheme()
    }

    override fun toggleTheme(): Boolean {
        val newThemeState = !isDarkTheme()
        saveTheme(newThemeState)
        return newThemeState
    }
}