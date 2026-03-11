package com.example.playlistmaker.feature.media.ui.fragments

import android.app.AlertDialog
import android.content.res.Configuration
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
    private var currentName = ""
    private var currentDescription = ""

    // Ключи для сохранения состояния
    companion object {
        private const val KEY_SELECTED_IMAGE_URI = "selected_image_uri"
        private const val KEY_IS_DATA_CHANGED = "is_data_changed"
        private const val KEY_CURRENT_NAME = "current_name"
        private const val KEY_CURRENT_DESCRIPTION = "current_description"
    }

    // Регистрируем Photo Picker
    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            selectedImageUri = uri
            loadImageToView(uri)
            isDataChanged = true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null) {
            selectedImageUri = savedInstanceState.getParcelable(KEY_SELECTED_IMAGE_URI)
            isDataChanged = savedInstanceState.getBoolean(KEY_IS_DATA_CHANGED, false)
            currentName = savedInstanceState.getString(KEY_CURRENT_NAME, "")
            currentDescription = savedInstanceState.getString(KEY_CURRENT_DESCRIPTION, "")
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
        restoreData()
        updateCreateButtonState()
    }

    private fun restoreData() {
        binding.playlistNameEditText.setText(currentName)
        binding.playlistDescriptionEditText.setText(currentDescription)
        if (selectedImageUri != null) {
            loadImageToView(selectedImageUri!!)
        }
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
            currentName = it.toString()
            updateCreateButtonState()
            isDataChanged = true
        }

        binding.playlistDescriptionEditText.doAfterTextChanged {
            currentDescription = it.toString()
            isDataChanged = true
        }
    }

    private fun setupBackPressedCallback() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            handleBackPress()
        }
    }

    private fun updateCreateButtonState() {
        binding.createButton.isEnabled = currentName.isNotBlank()
    }

    private fun loadImageToView(uri: Uri) {
        // Скрываем иконку добавления фото
        binding.addPhotoLayout.visibility = View.GONE

        // Показываем CardView с изображением
        binding.coverCardView.visibility = View.VISIBLE

        // Убираем фон у ImageView
        binding.coverImageView.setBackgroundColor(android.graphics.Color.TRANSPARENT)

        // Загружаем изображение
        Glide.with(this)
            .load(uri)
            .placeholder(R.drawable.vector_placeholder)
            .error(R.drawable.vector_placeholder)
            .centerCrop()
            .into(binding.coverImageView)
    }

    private fun createPlaylist() {
        val name = binding.playlistNameEditText.text.toString()
        if (name.isBlank()) return

        lifecycleScope.launch {
            val coverPath = selectedImageUri?.let { uri ->
                ImageUtils.copyImageToInternalStorage(requireContext(), uri)
            }

            val playlist = Playlist(
                name = name,
                description = binding.playlistDescriptionEditText.text.toString().takeIf { it.isNotBlank() },
                coverPath = coverPath,
                tracksIds = emptyList(),
                tracksCount = 0
            )

            viewModel.createPlaylist(playlist)

            Toast.makeText(
                requireContext(),
                getString(R.string.playlist_created, name),
                Toast.LENGTH_SHORT
            ).show()

            findNavController().previousBackStackEntry?.savedStateHandle?.set("playlist_created", true)
            findNavController().navigateUp()
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

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(KEY_SELECTED_IMAGE_URI, selectedImageUri)
        outState.putBoolean(KEY_IS_DATA_CHANGED, isDataChanged)
        outState.putString(KEY_CURRENT_NAME, currentName)
        outState.putString(KEY_CURRENT_DESCRIPTION, currentDescription)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}