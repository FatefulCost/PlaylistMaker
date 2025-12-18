package com.example.playlistmaker.feature.sharing.domain.interactor

interface SharingInteractor {
    fun shareApp(context: android.content.Context, shareMessage: String, shareSubject: String)
    fun sendSupportEmail(context: android.content.Context, email: String, subject: String, body: String)
    fun openTermsAndConditions(context: android.content.Context, termsUrl: String)
}