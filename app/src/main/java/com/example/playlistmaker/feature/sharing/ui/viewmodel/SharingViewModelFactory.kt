package com.example.playlistmaker.feature.sharing.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.playlistmaker.feature.sharing.domain.interactor.SharingInteractor

class SharingViewModelFactory(
    private val sharingInteractor: SharingInteractor
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SharingViewModel::class.java)) {
            return SharingViewModel(sharingInteractor) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}