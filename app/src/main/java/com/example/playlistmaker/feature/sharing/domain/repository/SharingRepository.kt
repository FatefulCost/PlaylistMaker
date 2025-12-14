package com.example.playlistmaker.feature.sharing.domain.repository

interface SharingRepository {
    fun shareApp(shareMessage: String, shareSubject: String)
    fun sendSupportEmail(email: String, subject: String, body: String)
    fun openTermsAndConditions(termsUrl: String)
}