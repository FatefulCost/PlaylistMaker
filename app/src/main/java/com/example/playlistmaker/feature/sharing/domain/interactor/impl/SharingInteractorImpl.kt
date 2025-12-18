package com.example.playlistmaker.feature.sharing.domain.interactor.impl

import com.example.playlistmaker.feature.sharing.domain.interactor.SharingInteractor
import com.example.playlistmaker.feature.sharing.domain.repository.SharingRepository

class SharingInteractorImpl(
    private val sharingRepository: SharingRepository
) : SharingInteractor {

    override fun shareApp(shareMessage: String, shareSubject: String) {
        sharingRepository.shareApp(shareMessage, shareSubject)
    }

    override fun sendSupportEmail(email: String, subject: String, body: String) {
        sharingRepository.sendSupportEmail(email, subject, body)
    }

    override fun openTermsAndConditions(termsUrl: String) {
        sharingRepository.openTermsAndConditions(termsUrl)
    }
}