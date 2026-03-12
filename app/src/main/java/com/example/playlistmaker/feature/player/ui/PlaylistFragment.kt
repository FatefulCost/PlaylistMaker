package com.example.playlistmaker.feature.player.ui

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.FragmentPlaylistBinding
import com.example.playlistmaker.feature.media.domain.model.Playlist
import com.example.playlistmaker.feature.media.ui.adapters.PlaylistTracksAdapter
import com.example.playlistmaker.feature.player.ui.viewmodel.PlaylistViewModel
import com.example.playlistmaker.feature.search.domain.model.Track
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class PlaylistFragment : Fragment() {

    private var _binding: FragmentPlaylistBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PlaylistViewModel by viewModel()

    private lateinit var tracksAdapter: PlaylistTracksAdapter

    private var currentPlaylist: Playlist? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlaylistBinding.inflate(inflater, container, false)
        return binding.root
    }


    private fun setupRecyclerView() {
        tracksAdapter = PlaylistTracksAdapter(
            onTrackClick = { track ->
                navigateToPlayer(track)
            },
            onTrackLongClick = { track ->
                showDeleteTrackDialog(track)
                true
            }
        )

        binding.tracksRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = tracksAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupClickListeners() {
        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.shareButton.setOnClickListener {
            currentPlaylist?.let { playlist ->
                viewModel.sharePlaylist(playlist)
            }
        }

        binding.menuButton.setOnClickListener {
            currentPlaylist?.let { playlist ->
                val bottomSheet = PlaylistMenuBottomSheet.newInstance(playlist)
                bottomSheet.show(parentFragmentManager, "PlaylistMenuBottomSheet")
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.getSerializable("playlist")?.let {
            currentPlaylist = it as Playlist
            viewModel.loadPlaylist(it.id)
        }

        setupRecyclerView()
        setupClickListeners()
        setupObservers()
        observePlaylistUpdate()
    }

    private fun setupObservers() {
        viewModel.playlist.observe(viewLifecycleOwner) { playlist ->
            playlist?.let {
                currentPlaylist = it
                displayPlaylistInfo(it)
            }
        }

        viewModel.tracks.observe(viewLifecycleOwner) { tracks ->
            tracksAdapter.updateTracks(tracks)
            updateTracksVisibility(tracks.isNotEmpty())
        }

        viewModel.totalDuration.observe(viewLifecycleOwner) { duration ->
            binding.totalDurationValueTextView.text = duration
        }

        viewModel.shareText.observe(viewLifecycleOwner) { shareText ->
            shareText?.let {
                startShareIntent(it)
            }
        }

        viewModel.showEmptyShareMessage.observe(viewLifecycleOwner) { show ->
            if (show) {
                Toast.makeText(
                    requireContext(),
                    R.string.share_playlist_empty,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        viewModel.trackRemoved.observe(viewLifecycleOwner) { removed ->
            if (removed) {
                Toast.makeText(
                    requireContext(),
                    R.string.track_removed_from_playlist,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        // Добавляем наблюдение за удалением плейлиста
        viewModel.playlistDeleted.observe(viewLifecycleOwner) { deleted ->
            if (deleted) {
                Log.d("PlaylistFragment", "Playlist was deleted, navigating back")

                Toast.makeText(
                    requireContext(),
                    R.string.playlist_deleted,
                    Toast.LENGTH_SHORT
                ).show()

                // Возвращаемся на экран медиатеки
                findNavController().popBackStack(R.id.mediaFragment, false)
            }
        }
    }

    private fun displayPlaylistInfo(playlist: Playlist) {
        // Загружаем обложку
        if (!playlist.coverPath.isNullOrEmpty()) {
            // Проверяем, что файл существует
            val coverFile = java.io.File(playlist.coverPath)
            if (coverFile.exists()) {
                Glide.with(requireContext())
                    .load(coverFile)  // Загружаем из файла
                    .placeholder(R.drawable.vector_placeholder)
                    .error(R.drawable.vector_placeholder)
                    .centerCrop()
                    .transform(RoundedCorners(16))
                    .into(binding.playlistCoverImageView)
            } else {
                binding.playlistCoverImageView.setImageResource(R.drawable.vector_placeholder)
            }
        } else {
            binding.playlistCoverImageView.setImageResource(R.drawable.vector_placeholder)
        }

        binding.playlistNameTextView.text = playlist.name

        if (!playlist.description.isNullOrEmpty()) {
            binding.playlistDescriptionTextView.text = playlist.description
            binding.playlistDescriptionTextView.isVisible = true
        } else {
            binding.playlistDescriptionTextView.isVisible = false
        }

        binding.playlistTracksCountTextView.text = getTracksCountText(playlist.tracksCount)
    }

    private fun updateTracksVisibility(hasTracks: Boolean) {
        if (hasTracks) {
            binding.tracksRecyclerView.isVisible = true
            binding.emptyTracksLayout.isVisible = false
        } else {
            binding.tracksRecyclerView.isVisible = false
            binding.emptyTracksLayout.isVisible = true
        }
    }

    private fun getTracksCountText(count: Int): String {
        return when {
            count % 10 == 1 && count % 100 != 11 -> "$count трек"
            count % 10 in 2..4 && count % 100 !in 12..14 -> "$count трека"
            else -> "$count треков"
        }
    }

    private fun showDeleteTrackDialog(track: Track) {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.delete_track_title)
            .setMessage(R.string.delete_track_confirmation)
            .setPositiveButton(R.string.yes) { _, _ ->
                currentPlaylist?.let { playlist ->
                    viewModel.removeTrackFromPlaylist(playlist.id, track.trackId)
                }
            }
            .setNegativeButton(R.string.no, null)
            .show()
    }

    private fun navigateToPlayer(track: Track) {
        val bundle = Bundle().apply {
            putSerializable("track", track)
        }
        findNavController().navigate(R.id.playerFragment, bundle)
    }

    private fun startShareIntent(shareText: String) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
            putExtra(Intent.EXTRA_SUBJECT, currentPlaylist?.name ?: "Плейлист")
        }
        startActivity(Intent.createChooser(shareIntent, getString(R.string.menu_share)))
    }

    private fun observePlaylistUpdate() {
        findNavController().currentBackStackEntry?.savedStateHandle
            ?.getLiveData<Boolean>("playlist_updated")
            ?.observe(viewLifecycleOwner) { updated ->
                if (updated) {
                    // Перезагружаем плейлист
                    currentPlaylist?.id?.let { playlistId ->
                        viewModel.loadPlaylist(playlistId)
                    }
                    // Очищаем handle
                    findNavController().currentBackStackEntry?.savedStateHandle?.remove<Boolean>("playlist_updated")
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}