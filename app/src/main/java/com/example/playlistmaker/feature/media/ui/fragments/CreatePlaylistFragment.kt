package com.example.playlistmaker.feature.media.ui.fragments

import android.app.AlertDialog
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.addCallback
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.R
import com.example.playlistmaker.core.utils.ImageUtils
import com.example.playlistmaker.databinding.FragmentCreatePlaylistBinding
import com.example.playlistmaker.feature.media.domain.model.Playlist
import com.example.playlistmaker.feature.media.ui.viewmodels.CreatePlaylistViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class CreatePlaylistFragment : Fragment() {

    private var _binding: FragmentCreatePlaylistBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CreatePlaylistViewModel by viewModel()

    private var selectedImageUri: Uri? = null
    private var isDataChanged = false

    // Регистрируем Photo Picker
    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            selectedImageUri = uri
            loadImageToView(uri)
            isDataChanged = true
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreatePlaylistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupClickListeners()
        setupTextWatchers()
        setupBackPressedCallback()
        updateCreateButtonState()
    }

    private fun setupClickListeners() {
        // Кнопка назад в тулбаре
        binding.toolbar.setNavigationOnClickListener {
            handleBackPress()
        }

        // Область для выбора обложки
        binding.coverContainer.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        // Кнопка создания плейлиста
        binding.createButton.setOnClickListener {
            createPlaylist()
        }
    }

    private fun setupTextWatchers() {
        binding.playlistNameEditText.doAfterTextChanged {
            updateCreateButtonState()
            isDataChanged = true
        }

        binding.playlistDescriptionEditText.doAfterTextChanged {
            isDataChanged = true
        }
    }

    private fun setupBackPressedCallback() {
        // Регистрируем callback для системной кнопки "Назад"
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            handleBackPress()
        }
    }

    private fun updateCreateButtonState() {
        val name = binding.playlistNameEditText.text.toString()
        binding.createButton.isEnabled = name.isNotBlank()
    }

    private fun loadImageToView(uri: Uri) {
        // Показываем изображение
        binding.coverImageView.visibility = View.VISIBLE
        Glide.with(this)
            .load(uri)
            .placeholder(R.drawable.vector_placeholder)
            .error(R.drawable.vector_placeholder)
            .centerCrop()
            .transform(RoundedCorners(16))
            .into(binding.coverImageView)
    }

    private fun createPlaylist() {
        val name = binding.playlistNameEditText.text.toString()
        if (name.isBlank()) return

        lifecycleScope.launch {
            // Копируем изображение во внутреннее хранилище, если оно выбрано
            val coverPath = selectedImageUri?.let { uri ->
                ImageUtils.copyImageToInternalStorage(requireContext(), uri)
            }

            // Создаем плейлист
            val playlist = Playlist(
                name = name,
                description = binding.playlistDescriptionEditText.text.toString().takeIf { it.isNotBlank() },
                coverPath = coverPath,
                tracksIds = emptyList(),
                tracksCount = 0
            )

            // Сохраняем плейлист
            viewModel.createPlaylist(playlist)

            Toast.makeText(
                requireContext(),
                getString(R.string.playlist_created, name),
                Toast.LENGTH_SHORT
            ).show()

            // Возвращаемся на предыдущий экран
            findNavController().popBackStack()
        }
    }

    private fun handleBackPress() {
        if (hasUnsavedChanges()) {
            showExitConfirmationDialog()
        } else {
            findNavController().navigateUp()
        }
    }

    private fun hasUnsavedChanges(): Boolean {
        return isDataChanged || selectedImageUri != null
    }

    private fun showExitConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.exit_creation_title)
            .setMessage(R.string.exit_creation_message)
            .setPositiveButton(R.string.complete) { _, _ ->
                findNavController().navigateUp()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}