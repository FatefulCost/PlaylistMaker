package com.example.playlistmaker.feature.settings.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.playlistmaker.feature.settings.domain.interactor.ThemeInteractor

class SettingsViewModel(
    private val themeInteractor: ThemeInteractor
) : ViewModel() {

    private val _themeState = MutableLiveData<Boolean>()
    val themeState: LiveData<Boolean> = _themeState

    init {
        loadTheme()
    }

    fun loadTheme() {
        _themeState.value = themeInteractor.isDarkTheme()
    }

    fun saveTheme(isDarkTheme: Boolean) {
        themeInteractor.saveTheme(isDarkTheme)
        _themeState.value = isDarkTheme
    }
}