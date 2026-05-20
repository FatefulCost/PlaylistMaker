package com.example.playlistmaker.feature.media.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.FragmentPlaylistsBinding
import com.example.playlistmaker.feature.media.domain.model.Playlist
import com.example.playlistmaker.feature.media.ui.adapters.PlaylistsAdapter
import com.example.playlistmaker.feature.media.ui.viewmodels.PlaylistsState
import com.example.playlistmaker.feature.media.ui.viewmodels.PlaylistsViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class PlaylistsFragment : Fragment() {

    private var _binding: FragmentPlaylistsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PlaylistsViewModel by viewModel()

    private lateinit var playlistsAdapter: PlaylistsAdapter

    companion object {
        fun newInstance() = PlaylistsFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlaylistsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupClickListeners()
        setupObservers()

        observePlaylistCreation()
        observePlaylistDeletion()
    }

    private fun observePlaylistDeletion() {
        findNavController().currentBackStackEntry?.savedStateHandle
            ?.getLiveData<Boolean>("playlist_deleted")
            ?.observe(viewLifecycleOwner) { deleted ->
                if (deleted) {
                    Log.d("PlaylistsFragment", "Playlist deleted, refreshing list")
                    // Обновляем список плейлистов
                    viewModel.loadPlaylists()
                    // Показываем сообщение об удалении
                    Toast.makeText(
                        requireContext(),
                        R.string.playlist_deleted,
                        Toast.LENGTH_SHORT
                    ).show()
                    // Очищаем handle
                    findNavController().currentBackStackEntry?.savedStateHandle?.remove<Boolean>("playlist_deleted")
                }
            }
    }

    private fun observePlaylistCreation() {
        findNavController().currentBackStackEntry?.savedStateHandle
            ?.getLiveData<Boolean>("playlist_created")
            ?.observe(viewLifecycleOwner) { created ->
                if (created) {
                    viewModel.loadPlaylists()
                    binding.playlistsRecyclerView.smoothScrollToPosition(0)
                    findNavController().currentBackStackEntry?.savedStateHandle?.remove<Boolean>("playlist_created")
                }
            }
    }

    override fun onResume() {
        super.onResume()
        // При возвращении на экран также обновляем список плейлистов
        viewModel.loadPlaylists()
    }

    private fun setupRecyclerView() {
        playlistsAdapter = PlaylistsAdapter(
            onPlaylistClick = { playlist ->
                val bundle = Bundle().apply {
                    putSerializable("playlist", playlist)
                }
                findNavController().navigate(R.id.playlistFragment, bundle)
            }
        )

        binding.playlistsRecyclerView.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = playlistsAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupClickListeners() {
        binding.btnCreatePlaylist.setOnClickListener {
            findNavController().navigate(R.id.createPlaylistFragment)
        }
    }

    private fun setupObservers() {
        viewModel.playlistsState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is PlaylistsState.Loading -> showLoading()
                is PlaylistsState.Success -> showPlaylists(state.playlists)
                is PlaylistsState.Empty -> showEmptyState()
                is PlaylistsState.Error -> showError(state.message)
            }
        }
    }

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.playlistsRecyclerView.visibility = View.GONE
        binding.emptyStateLayout.visibility = View.GONE
    }

    private fun showPlaylists(playlists: List<Playlist>) {
        binding.progressBar.visibility = View.GONE
        binding.playlistsRecyclerView.visibility = View.VISIBLE
        binding.emptyStateLayout.visibility = View.GONE

        playlistsAdapter.updatePlaylists(playlists)
    }

    private fun showEmptyState() {
        binding.progressBar.visibility = View.GONE
        binding.playlistsRecyclerView.visibility = View.GONE
        binding.emptyStateLayout.visibility = View.VISIBLE
    }

    private fun showError(message: String) {
        binding.progressBar.visibility = View.GONE
        binding.playlistsRecyclerView.visibility = View.GONE
        binding.emptyStateLayout.visibility = View.VISIBLE
        binding.tvEmptyMessage.text = message
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}