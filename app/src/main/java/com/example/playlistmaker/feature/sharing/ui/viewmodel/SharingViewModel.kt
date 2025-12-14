package com.example.playlistmaker.feature.sharing.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.playlistmaker.feature.sharing.domain.interactor.SharingInteractor

class SharingViewModel(
    private val sharingInteractor: SharingInteractor
) : ViewModel() {

    fun shareApp(shareMessage: String, shareSubject: String) {
        sharingInteractor.shareApp(shareMessage, shareSubject)
    }

    fun sendSupportEmail(email: String, subject: String, body: String) {
        sharingInteractor.sendSupportEmail(email, subject, body)
    }

    fun openTermsAndConditions(termsUrl: String) {
        sharingInteractor.openTermsAndConditions(termsUrl)
    }
}