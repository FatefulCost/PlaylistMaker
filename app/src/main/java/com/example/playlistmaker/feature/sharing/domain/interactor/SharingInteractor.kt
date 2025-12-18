package com.example.playlistmaker.feature.sharing.domain.interactor

interface SharingInteractor {
    fun shareApp(shareMessage: String, shareSubject: String)
    fun sendSupportEmail(email: String, subject: String, body: String)
    fun openTermsAndConditions(termsUrl: String)
}