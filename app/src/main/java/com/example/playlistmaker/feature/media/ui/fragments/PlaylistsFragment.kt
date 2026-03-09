package com.example.playlistmaker.feature.media.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
    }

    override fun onResume() {
        super.onResume()
        // При возвращении на экран обновляем список плейлистов
        viewModel.loadPlaylists()
    }

    private fun setupRecyclerView() {
        playlistsAdapter = PlaylistsAdapter(
            onPlaylistClick = { playlist ->
                // Пока не реализовано - переход на экран плейлиста
                // findNavController().navigate(R.id.playlistFragment, bundle)
            }
        )

        binding.playlistsRecyclerView.apply {
            layoutManager = GridLayoutManager(requireContext(), 2) // 2 колонки
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

        // Слушаем возврат с экрана создания плейлиста
        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<Boolean>("playlist_created")
            ?.observe(viewLifecycleOwner) { created ->
                if (created) {
                    // Показываем плейлисты и прокручиваем к началу
                    binding.playlistsRecyclerView.smoothScrollToPosition(0)
                }
            }
    }

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.playlistsRecyclerView.visibility = View.GONE
        binding.ivEmptyState.visibility = View.GONE
        binding.tvEmptyMessage.visibility = View.GONE
    }

    private fun showPlaylists(playlists: List<Playlist>) {
        binding.progressBar.visibility = View.GONE
        binding.playlistsRecyclerView.visibility = View.VISIBLE
        binding.ivEmptyState.visibility = View.GONE
        binding.tvEmptyMessage.visibility = View.GONE

        playlistsAdapter.updatePlaylists(playlists)
    }

    private fun showEmptyState() {
        binding.progressBar.visibility = View.GONE
        binding.playlistsRecyclerView.visibility = View.GONE
        binding.ivEmptyState.visibility = View.VISIBLE
        binding.tvEmptyMessage.visibility = View.VISIBLE
    }

    private fun showError(message: String) {
        binding.progressBar.visibility = View.GONE
        binding.playlistsRecyclerView.visibility = View.GONE
        binding.ivEmptyState.visibility = View.VISIBLE
        binding.tvEmptyMessage.visibility = View.VISIBLE
        binding.tvEmptyMessage.text = message
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}