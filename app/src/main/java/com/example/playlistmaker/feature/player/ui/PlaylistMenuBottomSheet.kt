package com.example.playlistmaker.feature.player.ui

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.BottomSheetPlaylistMenuBinding
import com.example.playlistmaker.feature.media.domain.model.Playlist
import com.example.playlistmaker.feature.player.ui.viewmodel.PlaylistMenuViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class PlaylistMenuBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetPlaylistMenuBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PlaylistMenuViewModel by viewModel()

    private var playlist: Playlist? = null

    companion object {
        private const val ARG_PLAYLIST = "playlist"
        private const val TAG = "PlaylistMenuBottomSheet"

        fun newInstance(playlist: Playlist): PlaylistMenuBottomSheet {
            val args = bundleOf(ARG_PLAYLIST to playlist)
            val fragment = PlaylistMenuBottomSheet()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialog)
        playlist = arguments?.getSerializable(ARG_PLAYLIST) as? Playlist
        Log.d(TAG, "onCreate, playlist: ${playlist?.name}")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetPlaylistMenuBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d(TAG, "onViewCreated")
        setupBottomSheet(view)
        displayPlaylistInfo()
        setupClickListeners()
        setupObservers()
    }

    private fun setupBottomSheet(view: View) {
        dialog?.apply {
            window?.setBackgroundDrawableResource(android.R.color.transparent)
            val behavior = BottomSheetBehavior.from(view.parent as View)
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.skipCollapsed = true
            window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        }
    }

    private fun displayPlaylistInfo() {
        playlist?.let { playlist ->
            Log.d(TAG, "displayPlaylistInfo: ${playlist.name}")

            if (!playlist.coverPath.isNullOrEmpty()) {
                Glide.with(requireContext())
                    .load(playlist.coverPath)
                    .placeholder(R.drawable.vector_placeholder)
                    .error(R.drawable.vector_placeholder)
                    .centerCrop()
                    .transform(RoundedCorners(4))
                    .into(binding.playlistCoverImageView)
            } else {
                binding.playlistCoverImageView.setImageResource(R.drawable.vector_placeholder)
            }

            binding.playlistNameTextView.text = playlist.name
            binding.playlistTracksCountTextView.text = getTracksCountText(playlist.tracksCount)
        }
    }

    private fun getTracksCountText(count: Int): String {
        return when {
            count % 10 == 1 && count % 100 != 11 -> "$count трек"
            count % 10 in 2..4 && count % 100 !in 12..14 -> "$count трека"
            else -> "$count треков"
        }
    }

    private fun setupClickListeners() {
        binding.menuShare.setOnClickListener {
            Log.d(TAG, "Share menu clicked")
            playlist?.let {
                // Вызываем шаринг
                viewModel.sharePlaylist(it)

                // Не закрываем сразу, даем время корутине выполниться
                Handler(Looper.getMainLooper()).postDelayed({
                    dismiss()
                }, 500) // Задержка 500мс
            }
        }

        binding.menuEdit.setOnClickListener {
            Log.d(TAG, "Edit menu clicked")
            playlist?.let {
                val bundle = Bundle().apply {
                    putSerializable("playlist", it)
                    putBoolean("edit_mode", true)
                }
                findNavController().navigate(R.id.createPlaylistFragment, bundle)
                dismiss()
            }
        }

        binding.menuDelete.setOnClickListener {
            Log.d(TAG, "Delete menu clicked")
            playlist?.let {
                showDeleteConfirmationDialog(it)
            }
        }
    }

    private fun showDeleteConfirmationDialog(playlist: Playlist) {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.delete_playlist_title)
            .setMessage(R.string.delete_playlist_confirmation)
            .setPositiveButton(R.string.delete) { _, _ ->
                Log.d(TAG, "Delete confirmed for playlist: ${playlist.name}")
                viewModel.deletePlaylist(playlist)
                dismiss()
                findNavController().popBackStack(R.id.mediaFragment, false)
            }
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                Log.d(TAG, "Delete cancelled")
                dialog.dismiss()
            }
            .show()
    }

    private fun setupObservers() {
        viewModel.shareText.observe(viewLifecycleOwner) { shareText ->
            Log.d(TAG, "shareText observed: ${shareText != null}")
            shareText?.let {
                startShareIntent(it)
            }
        }

        viewModel.showEmptyShareMessage.observe(viewLifecycleOwner) { show ->
            Log.d(TAG, "showEmptyShareMessage observed: $show")
            if (show) {
                Toast.makeText(
                    requireContext(),
                    R.string.share_playlist_empty,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        viewModel.playlistDeleted.observe(viewLifecycleOwner) { deleted ->
            Log.d(TAG, "playlistDeleted observed: $deleted")
            if (deleted) {
                Log.d(TAG, "Playlist deleted, navigating back")

                findNavController().previousBackStackEntry?.savedStateHandle?.set("playlist_deleted", true)
                dismiss()
            }
        }
    }

    private fun startShareIntent(shareText: String) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
            putExtra(Intent.EXTRA_SUBJECT, playlist?.name ?: "Плейлист")
        }
        startActivity(Intent.createChooser(shareIntent, getString(R.string.menu_share)))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}