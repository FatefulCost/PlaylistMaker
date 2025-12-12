package com.example.playlistmaker.presentation.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.playlistmaker.R
import com.example.playlistmaker.presentation.util.ThemeManager
import com.google.android.material.switchmaterial.SwitchMaterial

class SettingsActivity : AppCompatActivity() {

    private lateinit var themeSwitch: SwitchMaterial
    private lateinit var themeManager: ThemeManager

    override fun onCreate(savedInstanceState: Bundle?) {
        themeManager = ThemeManager(this)
        applySavedTheme()

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_settings)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.settings_activity)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()
        setupToolbar()
        setupThemeSwitch()
        setupClickListeners()
    }

    private fun initViews() {
        themeSwitch = findViewById(R.id.themeSwitch)
    }

    private fun setupToolbar() {
        val toolbar = findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupThemeSwitch() {
        // Устанавливаем текущее состояние переключателя
        themeSwitch.isChecked = themeManager.isDarkTheme()

        // Обработчик изменения темы
        themeSwitch.setOnCheckedChangeListener { _, isChecked ->
            themeManager.saveTheme(isChecked)
            applyTheme(isChecked)
        }
    }

    private fun applySavedTheme() {
        // Применяем сохраненную тему при создании активности
        if (themeManager.isDarkTheme()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }

    private fun applyTheme(isDarkTheme: Boolean) {
        // Устанавливаем тему для всего приложения
        if (isDarkTheme) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        // Перезагружаем активность для применения темы
        recreate()
    }

    private fun setupClickListeners() {
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