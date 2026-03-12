package com.example.playlistmaker.feature.player.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.BottomSheetPlaylistsBinding
import com.example.playlistmaker.feature.media.domain.model.Playlist
import com.example.playlistmaker.feature.media.ui.adapters.PlaylistSimpleAdapter
import com.example.playlistmaker.feature.player.ui.viewmodel.PlaylistBottomSheetViewModel
import com.example.playlistmaker.feature.search.domain.model.Track
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.shape.MaterialShapeDrawable
import androidx.core.content.ContextCompat
import android.graphics.Color
import android.view.WindowManager
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class PlaylistBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetPlaylistsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PlaylistBottomSheetViewModel by viewModel()

    private lateinit var playlistsAdapter: PlaylistSimpleAdapter
    private var currentTrack: Track? = null

    companion object {
        private const val ARG_TRACK = "track"

        fun newInstance(track: Track): PlaylistBottomSheet {
            val args = Bundle()
            args.putSerializable(ARG_TRACK, track)
            val fragment = PlaylistBottomSheet()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetPlaylistsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        currentTrack = arguments?.getSerializable(ARG_TRACK) as? Track

        setupBottomSheet(view)
        setupRecyclerView()
        setupClickListeners()
        setupObservers()

        viewModel.loadPlaylists()
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

    private fun setupRecyclerView() {
        playlistsAdapter = PlaylistSimpleAdapter(
            onPlaylistClick = { playlist ->
                currentTrack?.let { track ->
                    handlePlaylistClick(playlist, track)
                }
            }
        )

        binding.playlistsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = playlistsAdapter
        }
    }

    private fun setupClickListeners() {
        binding.btnCreateNewPlaylist.setOnClickListener {
            // Переходим на экран создания плейлиста
            findNavController().navigate(R.id.createPlaylistFragment)
            dismiss()
        }
    }

    private fun setupObservers() {
        viewModel.playlists.observe(viewLifecycleOwner) { playlists ->
            if (playlists.isEmpty()) {
                binding.emptyStateLayout.isVisible = true
                binding.playlistsRecyclerView.isVisible = false
            } else {
                binding.emptyStateLayout.isVisible = false
                binding.playlistsRecyclerView.isVisible = true
                playlistsAdapter.submitList(playlists)
            }
        }
    }

    private fun handlePlaylistClick(playlist: Playlist, track: Track) {
        lifecycleScope.launch {
            val isTrackInPlaylist = viewModel.isTrackInPlaylist(playlist.id, track.trackId)

            if (isTrackInPlaylist) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.track_already_in_playlist, playlist.name),
                    Toast.LENGTH_SHORT
                ).show()
                // Не закрываем Bottom Sheet
            } else {
                val success = viewModel.addTrackToPlaylist(playlist.id, track.trackId)
                if (success) {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.track_added_to_playlist, playlist.name),
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Ошибка при добавлении трека",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                dismiss() // Закрываем только при успешном добавлении
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}