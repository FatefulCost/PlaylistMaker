package com.example.playlistmaker.feature.sharing.domain.interactor.impl

import com.example.playlistmaker.feature.sharing.domain.interactor.SharingInteractor
import com.example.playlistmaker.feature.sharing.domain.repository.SharingRepository

class SharingInteractorImpl(
    private val sharingRepository: SharingRepository
) : SharingInteractor {

    override fun shareApp(context: android.content.Context, shareMessage: String, shareSubject: String) {
        sharingRepository.shareApp(context, shareMessage, shareSubject)
    }

    override fun sendSupportEmail(context: android.content.Context, email: String, subject: String, body: String) {
        sharingRepository.sendSupportEmail(context, email, subject, body)
    }

    override fun openTermsAndConditions(context: android.content.Context, termsUrl: String) {
        sharingRepository.openTermsAndConditions(context, termsUrl)
    }
}