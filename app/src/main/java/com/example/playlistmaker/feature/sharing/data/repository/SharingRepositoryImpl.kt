package com.example.playlistmaker.feature.sharing.data.repository

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.example.playlistmaker.feature.sharing.domain.repository.SharingRepository

class SharingRepositoryImpl : SharingRepository {

    override fun shareApp(context: Context, shareMessage: String, shareSubject: String) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, shareSubject)
            putExtra(Intent.EXTRA_TEXT, shareMessage)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        try {
            context.startActivity(Intent.createChooser(shareIntent, "Поделиться приложением"))
        } catch (e: Exception) {
            Toast.makeText(context, "Не удалось открыть диалог шаринга", Toast.LENGTH_LONG).show()
        }
    }

    override fun sendSupportEmail(context: Context, email: String, subject: String, body: String) {
        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        try {
            context.startActivity(Intent.createChooser(emailIntent, "Выберите почтовый клиент"))
        } catch (e: Exception) {
            Toast.makeText(context, "Не удалось открыть почтовый клиент", Toast.LENGTH_LONG).show()
        }
    }

    override fun openTermsAndConditions(context: Context, termsUrl: String) {
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