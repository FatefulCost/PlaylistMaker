package com.example.playlistmaker

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_settings)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.settings_activity)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val toolbar = findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Включаем кнопку "Назад"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Обработчик нажатия на стрелку
        toolbar.setNavigationOnClickListener {
            finish()
        }


        val shareText = findViewById<TextView>(R.id.shareAppText)
        shareText.setOnClickListener {
            shareApp()
        }
        val supportText = findViewById<TextView>(R.id.supportText)
        supportText.setOnClickListener {
            sendSupportEmail()
        }
        val termsText = findViewById<TextView>(R.id.termsText)
        termsText.setOnClickListener {
            openTermsAndConditions()
        }

    }

    private fun shareApp() {
            val shareMessage = getString(R.string.share_message)
            val shareMessageExtra = getString(R.string.share_message_extra_subject)
            val shareMessageIntent = getString(R.string.share_message_extra_intent)

        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "text/plain"
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, shareMessageExtra)
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
        startActivity(Intent.createChooser(shareIntent, shareMessageIntent))
    }

    private fun sendSupportEmail() {
        val email = getString(R.string.email)
        val subject = getString(R.string.email_subject)
        val body = getString(R.string.email_body)
        val chose = getString(R.string.email_chose)
        val error = getString(R.string.email_error)

        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
        }

        try {
            startActivity(Intent.createChooser(emailIntent, chose))
        } catch (e: Exception) {
            Toast.makeText(
                this,
                error,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun openTermsAndConditions() {
        val termsUrl = getString(R.string.terms_url)
        val termsError = getString(R.string.terms_error)

        val browserIntent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(termsUrl)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        try {
            startActivity(browserIntent)
        } catch (e: Exception) {
            Toast.makeText(
                this,
                termsError,
                Toast.LENGTH_LONG
            ).show()
        }
    }


}