package com.example.playlistmaker.feature.settings.ui

import android.os.Bundle
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.playlistmaker.R
import com.example.playlistmaker.core.creator.InteractorCreator
import com.example.playlistmaker.feature.sharing.ui.viewmodel.SharingViewModel
import com.example.playlistmaker.feature.sharing.ui.viewmodel.SharingViewModelFactory
import com.example.playlistmaker.feature.settings.ui.viewmodel.SettingsViewModel
import com.example.playlistmaker.feature.settings.ui.viewmodel.SettingsViewModelFactory
import com.google.android.material.switchmaterial.SwitchMaterial

class SettingsActivity : AppCompatActivity() {

    private lateinit var themeSwitch: SwitchMaterial

    private val settingsViewModel: SettingsViewModel by viewModels {
        SettingsViewModelFactory(
            InteractorCreator.createThemeInteractor(this)
        )
    }

    private val sharingViewModel: SharingViewModel by viewModels {
        SharingViewModelFactory(
            InteractorCreator.createSharingInteractor(this)
        )
    }

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
            applyTheme(isChecked, recreate = false)
        }
    }

    private fun setupObservers() {
        settingsViewModel.themeState.observe(this) { isDarkTheme ->
            themeSwitch.isChecked = isDarkTheme
        }
    }

    private fun applyTheme(isDarkTheme: Boolean, recreate: Boolean = false) {
        if (isDarkTheme) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        if (recreate) {
            recreate()
        }
    }

    private fun setupClickListeners() {
        val shareText = findViewById<TextView>(R.id.shareAppText)
        shareText.setOnClickListener {
            sharingViewModel.shareApp(
                shareMessage = getString(R.string.share_message),
                shareSubject = getString(R.string.share_message_extra_subject)
            )
        }

        val supportText = findViewById<TextView>(R.id.supportText)
        supportText.setOnClickListener {
            sharingViewModel.sendSupportEmail(
                email = getString(R.string.email),
                subject = getString(R.string.email_subject),
                body = getString(R.string.email_body)
            )
        }

        val termsText = findViewById<TextView>(R.id.termsText)
        termsText.setOnClickListener {
            sharingViewModel.openTermsAndConditions(
                termsUrl = getString(R.string.terms_url)
            )
        }
    }
}