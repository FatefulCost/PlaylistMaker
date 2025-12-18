package com.example.playlistmaker.feature.settings.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.playlistmaker.core.theme.ThemeManager

class SettingsViewModel(
    private val themeManager: ThemeManager
) : ViewModel() {

    private val _themeState = MutableLiveData<Boolean>()
    val themeState: LiveData<Boolean> = _themeState

    init {
        loadTheme()
    }

    fun loadTheme() {
        _themeState.value = themeManager.isDarkTheme()
    }

    fun saveTheme(isDarkTheme: Boolean) {
        themeManager.saveTheme(isDarkTheme)
        _themeState.value = isDarkTheme
    }
}