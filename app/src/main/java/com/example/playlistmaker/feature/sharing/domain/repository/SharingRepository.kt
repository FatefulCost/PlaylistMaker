package com.example.playlistmaker.feature.sharing.domain.repository

import android.content.Context

interface SharingRepository {
    fun shareApp(context: Context, shareMessage: String, shareSubject: String)
    fun sendSupportEmail(context: Context, email: String, subject: String, body: String)
    fun openTermsAndConditions(context: Context, termsUrl: String)
}