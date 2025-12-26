package com.example.playlistmaker.feature.sharing.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.playlistmaker.feature.sharing.domain.interactor.SharingInteractor

class SharingViewModel(
    private val sharingInteractor: SharingInteractor
) : ViewModel() {

    fun shareApp(context: android.content.Context, shareMessage: String, shareSubject: String) {
        sharingInteractor.shareApp(context, shareMessage, shareSubject)
    }

    fun sendSupportEmail(context: android.content.Context, email: String, subject: String, body: String) {
        sharingInteractor.sendSupportEmail(context, email, subject, body)
    }

    fun openTermsAndConditions(context: android.content.Context, termsUrl: String) {
        sharingInteractor.openTermsAndConditions(context, termsUrl)
    }
}