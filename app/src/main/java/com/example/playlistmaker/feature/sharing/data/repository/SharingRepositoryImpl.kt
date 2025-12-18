package com.example.playlistmaker.feature.sharing.data.repository

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.example.playlistmaker.feature.sharing.domain.repository.SharingRepository

class SharingRepositoryImpl(
    private val context: Context
) : SharingRepository {

    override fun shareApp(shareMessage: String, shareSubject: String) {
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "text/plain"
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, shareSubject)
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
        context.startActivity(Intent.createChooser(shareIntent, "Поделиться приложением"))
    }

    override fun sendSupportEmail(email: String, subject: String, body: String) {
        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
        }

        try {
            context.startActivity(Intent.createChooser(emailIntent, "Выберите почтовый клиент"))
        } catch (e: Exception) {
            Toast.makeText(context, "Не удалось открыть почтовый клиент", Toast.LENGTH_LONG).show()
        }
    }

    override fun openTermsAndConditions(termsUrl: String) {
        val browserIntent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(termsUrl)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        try {
            context.startActivity(browserIntent)
        } catch (e: Exception) {
            Toast.makeText(context, "Не удалось открыть браузер", Toast.LENGTH_LONG).show()
        }
    }
}