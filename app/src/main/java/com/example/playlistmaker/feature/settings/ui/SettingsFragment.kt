package com.example.playlistmaker.feature.settings.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.FragmentSettingsBinding
import com.example.playlistmaker.feature.settings.ui.viewmodel.SettingsViewModel
import com.example.playlistmaker.feature.sharing.ui.viewmodel.SharingViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val settingsViewModel: SettingsViewModel by viewModel()
    private val sharingViewModel: SharingViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
        setupThemeSwitch()
        setupObservers()
        setupClickListeners()
    }

    private fun initViews() {
        // View binding уже инициализирован
    }

    private fun setupThemeSwitch() {
        binding.themeSwitch.setOnCheckedChangeListener { _, isChecked ->
            settingsViewModel.saveTheme(isChecked)
            applyTheme(isChecked)
        }
    }

    private fun setupObservers() {
        settingsViewModel.themeState.observe(viewLifecycleOwner) { isDarkTheme ->
            binding.themeSwitch.isChecked = isDarkTheme
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
        binding.shareAppText.setOnClickListener {
            sharingViewModel.shareApp(
                context = requireContext(),
                shareMessage = getString(R.string.share_message),
                shareSubject = getString(R.string.share_message_extra_subject)
            )
        }

        binding.supportText.setOnClickListener {
            sharingViewModel.sendSupportEmail(
                context = requireContext(),
                email = getString(R.string.email),
                subject = getString(R.string.email_subject),
                body = getString(R.string.email_body)
            )
        }

        binding.termsText.setOnClickListener {
            sharingViewModel.openTermsAndConditions(
                context = requireContext(),
                termsUrl = getString(R.string.terms_url)
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}