package com.example.playlistmaker.feature.media.ui.fragments

import android.app.AlertDialog
import android.net.Uri
import android.os.Bundle
import android.util.Log
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
import java.io.File

class CreatePlaylistFragment : Fragment() {

    private var _binding: FragmentCreatePlaylistBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CreatePlaylistViewModel by viewModel()

    private var selectedImageUri: Uri? = null
    private var isDataChanged = false
    private var currentName = ""
    private var currentDescription = ""

    // Переменные для режима редактирования
    private var isEditMode = false
    private var editingPlaylist: Playlist? = null
    private var originalCoverPath: String? = null

    companion object {
        private const val KEY_SELECTED_IMAGE_URI = "selected_image_uri"
        private const val KEY_IS_DATA_CHANGED = "is_data_changed"
        private const val KEY_CURRENT_NAME = "current_name"
        private const val KEY_CURRENT_DESCRIPTION = "current_description"
        private const val TAG = "CreatePlaylistFragment"
    }

    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            selectedImageUri = uri
            loadImageToView(uri)
            isDataChanged = true
            Log.d(TAG, "Image selected: $uri")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Получаем аргументы
        arguments?.let {
            isEditMode = it.getBoolean("edit_mode", false)
            editingPlaylist = it.getSerializable("playlist") as? Playlist
            Log.d(TAG, "onCreate - isEditMode: $isEditMode, playlist: ${editingPlaylist?.name}")
        }

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

        Log.d(TAG, "onViewCreated - isEditMode: $isEditMode")

        setupUI()
        setupClickListeners()
        setupTextWatchers()
        setupBackPressedCallback()

        if (isEditMode) {
            setupEditMode()
        }

        updateCreateButtonState()
    }

    private fun setupUI() {
        if (isEditMode) {
            binding.toolbar.title = getString(R.string.edit_playlist)
            binding.createButton.text = getString(R.string.save)
        } else {
            binding.toolbar.title = getString(R.string.new_playlist)
            binding.createButton.text = getString(R.string.create)
        }
    }

    private fun setupEditMode() {
        editingPlaylist?.let { playlist ->
            Log.d(TAG, "Setting up edit mode for playlist: ${playlist.name}")

            // Устанавливаем название
            currentName = playlist.name
            binding.playlistNameEditText.setText(playlist.name)

            // Устанавливаем описание
            playlist.description?.let {
                currentDescription = it
                binding.playlistDescriptionEditText.setText(it)
            }

            // Загружаем обложку
            if (!playlist.coverPath.isNullOrEmpty()) {
                val coverFile = File(playlist.coverPath)
                if (coverFile.exists()) {
                    // Создаем URI из файла
                    selectedImageUri = Uri.fromFile(coverFile)
                    loadImageToView(selectedImageUri!!)
                    originalCoverPath = playlist.coverPath
                    Log.d(TAG, "Cover loaded from file: ${playlist.coverPath}")
                } else {
                    Log.d(TAG, "Cover file not found: ${playlist.coverPath}")
                    // Если файл не найден, показываем заглушку
                    selectedImageUri = null
                    showPlaceholder()
                }
            } else {
                // Если обложки нет, показываем заглушку
                selectedImageUri = null
                showPlaceholder()
            }
        }
    }

    private fun setupClickListeners() {
        binding.toolbar.setNavigationOnClickListener {
            handleBackPress()
        }

        binding.coverContainer.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        binding.createButton.setOnClickListener {
            if (isEditMode) {
                updatePlaylist()
            } else {
                createPlaylist()
            }
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
        binding.linesCover.visibility = View.GONE

        // Убираем фон у ImageView
        binding.coverImageView.setBackgroundColor(android.graphics.Color.TRANSPARENT)

        // Загружаем изображение
        Glide.with(this)
            .load(uri)
            .placeholder(R.drawable.vector_placeholder)
            .error(R.drawable.vector_placeholder)
            .centerCrop()
            .transform(RoundedCorners(16))
            .into(binding.coverImageView)

        Log.d(TAG, "Image loaded to view from URI: $uri")
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

            // Уведомляем о создании плейлиста
            findNavController().previousBackStackEntry?.savedStateHandle?.set("playlist_updated", true)
            findNavController().navigateUp()
        }
    }

    private fun updatePlaylist() {
        val name = binding.playlistNameEditText.text.toString()
        if (name.isBlank()) return

        lifecycleScope.launch {
            try {
                val coverPath = when {
                    selectedImageUri != null && selectedImageUri.toString().startsWith("content://") -> {
                        Log.d(TAG, "Copying new image from content URI")
                        ImageUtils.copyImageToInternalStorage(requireContext(), selectedImageUri!!)
                    }
                    selectedImageUri != null && selectedImageUri.toString().startsWith("file://") -> {
                        Log.d(TAG, "Using existing file URI")
                        selectedImageUri.toString().substringAfter("file://")
                    }
                    editingPlaylist?.coverPath != null -> {
                        Log.d(TAG, "Keeping existing cover path: ${editingPlaylist?.coverPath}")
                        editingPlaylist?.coverPath
                    }
                    else -> {
                        Log.d(TAG, "No cover")
                        null
                    }
                }

                if (coverPath != null &&
                    editingPlaylist?.coverPath != null &&
                    coverPath != editingPlaylist?.coverPath) {
                    try {
                        Log.d(TAG, "Deleting old cover: ${editingPlaylist?.coverPath}")
                        ImageUtils.deleteImage(editingPlaylist?.coverPath!!)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error deleting old cover", e)
                    }
                }

                val updatedPlaylist = editingPlaylist?.copy(
                    name = name,
                    description = binding.playlistDescriptionEditText.text.toString().takeIf { it.isNotBlank() },
                    coverPath = coverPath
                ) ?: return@launch

                Log.d(TAG, "Updating playlist with coverPath: $coverPath")
                viewModel.updatePlaylist(updatedPlaylist)

                Toast.makeText(
                    requireContext(),
                    R.string.playlist_updated,
                    Toast.LENGTH_SHORT
                ).show()

                findNavController().previousBackStackEntry?.savedStateHandle?.set("playlist_updated", true)
                findNavController().navigateUp()

            } catch (e: Exception) {
                Log.e(TAG, "Error updating playlist", e)
                Toast.makeText(
                    requireContext(),
                    "Ошибка при обновлении плейлиста",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun handleBackPress() {
        if (isEditMode) {
            findNavController().navigateUp()
        } else {
            if (hasUnsavedChanges()) {
                showExitConfirmationDialog()
            } else {
                findNavController().navigateUp()
            }
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

    private fun showPlaceholder() {
        binding.addPhotoLayout.visibility = View.VISIBLE
        binding.coverCardView.visibility = View.GONE
        binding.linesCover.visibility = View.VISIBLE
        binding.coverImageView.setImageDrawable(null)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}