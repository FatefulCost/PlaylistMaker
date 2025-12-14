package com.example.playlistmaker.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.playlistmaker.domain.interactor.SearchInteractor

class SearchViewModelFactory(
    private val searchInteractor: SearchInteractor
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SearchViewModel::class.java)) {
            return SearchViewModel(searchInteractor) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}