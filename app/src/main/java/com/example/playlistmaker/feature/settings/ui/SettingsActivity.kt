package com.example.playlistmaker.feature.settings.ui

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.playlistmaker.R
import com.example.playlistmaker.feature.settings.ui.viewmodel.SettingsViewModel
import com.example.playlistmaker.feature.sharing.ui.viewmodel.SharingViewModel
import com.google.android.material.switchmaterial.SwitchMaterial
import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingsActivity : AppCompatActivity() {

    private lateinit var themeSwitch: SwitchMaterial

    private val settingsViewModel: SettingsViewModel by viewModel()
    private val sharingViewModel: SharingViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        initViews()
        setupToolbar()
        setupThemeSwitch()
        setupObservers()
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
        themeSwitch.setOnCheckedChangeListener { _, isChecked ->
            settingsViewModel.saveTheme(isChecked)
            applyTheme(isChecked)
        }
    }

    private fun setupObservers() {
        settingsViewModel.themeState.observe(this) { isDarkTheme ->
            themeSwitch.isChecked = isDarkTheme
        }
    }

    private fun applyTheme(isDarkTheme: Boolean) {
        if (isDarkTheme) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }

    private fun setupClickListeners() {
        val shareText = findViewById<TextView>(R.id.shareAppText)
        shareText.setOnClickListener {
            sharingViewModel.shareApp(
                context = this,
                shareMessage = getString(R.string.share_message),
                shareSubject = getString(R.string.share_message_extra_subject)
            )
        }

        val supportText = findViewById<TextView>(R.id.supportText)
        supportText.setOnClickListener {
            sharingViewModel.sendSupportEmail(
                context = this,
                email = getString(R.string.email),
                subject = getString(R.string.email_subject),
                body = getString(R.string.email_body)
            )
        }

        val termsText = findViewById<TextView>(R.id.termsText)
        termsText.setOnClickListener {
            sharingViewModel.openTermsAndConditions(
                context = this,
                termsUrl = getString(R.string.terms_url)
            )
        }
    }
}