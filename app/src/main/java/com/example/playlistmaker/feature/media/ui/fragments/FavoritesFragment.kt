package com.example.playlistmaker.feature.media.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.FragmentFavoritesBinding
import com.example.playlistmaker.feature.media.ui.adapters.FavoritesAdapter
import com.example.playlistmaker.feature.media.ui.viewmodels.FavoritesState
import com.example.playlistmaker.feature.media.ui.viewmodels.FavoritesViewModel
import com.example.playlistmaker.feature.search.domain.model.Track
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class FavoritesFragment : Fragment() {

    private var _binding: FragmentFavoritesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FavoritesViewModel by viewModel()

    private lateinit var favoritesAdapter: FavoritesAdapter

    // Job для debounce кликов
    private var clickDebounceJob: Job? = null
    private var lastClickedTrack: Track? = null

    companion object {
        private const val CLICK_DEBOUNCE_DELAY = 1000L
        private const val KEY_RECYCLER_POSITION = "recycler_position"

        fun newInstance() = FavoritesFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavoritesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupObservers()

        savedInstanceState?.let {
            val position = it.getInt(KEY_RECYCLER_POSITION, 0)
            binding.favoritesRecyclerView.scrollToPosition(position)
        }
    }

    private fun setupRecyclerView() {
        favoritesAdapter = FavoritesAdapter(
            onTrackClick = { track ->
                onTrackClicked(track)
            }
        )

        binding.favoritesRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = favoritesAdapter
            setHasFixedSize(true)
        }
    }

    private fun onTrackClicked(track: Track) {
        clickDebounceJob?.cancel()

        clickDebounceJob = lifecycleScope.launch {
            delay(CLICK_DEBOUNCE_DELAY)

            if (lastClickedTrack?.trackId == track.trackId) {
                // Навигация к экрану плеера
                val bundle = Bundle().apply {
                    putSerializable("track", track)
                }
                findNavController().navigate(R.id.playerFragment, bundle)
            }
        }

        lastClickedTrack = track
    }

    private fun setupObservers() {
        viewModel.favoritesState.observe(viewLifecycleOwner) { state ->
            when (state) {
                FavoritesState.Loading -> showLoading()
                is FavoritesState.Success -> showFavorites(state.tracks)
                FavoritesState.Empty -> showEmpty()
                is FavoritesState.Error -> showError(state.message)
            }
        }
    }

    private fun showLoading() {
        binding.favoritesRecyclerView.visibility = View.GONE
        binding.ivEmptyState.visibility = View.GONE
        binding.tvEmptyMessage.visibility = View.GONE
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun showFavorites(tracks: List<Track>) {
        binding.progressBar.visibility = View.GONE
        binding.ivEmptyState.visibility = View.GONE
        binding.tvEmptyMessage.visibility = View.GONE
        binding.favoritesRecyclerView.visibility = View.VISIBLE
        favoritesAdapter.updateTracks(tracks)
    }

    private fun showEmpty() {
        binding.progressBar.visibility = View.GONE
        binding.favoritesRecyclerView.visibility = View.GONE
        binding.ivEmptyState.visibility = View.VISIBLE
        binding.tvEmptyMessage.visibility = View.VISIBLE
        binding.tvEmptyMessage.text = getString(R.string.no_favorites_yet)
    }

    private fun showError(message: String) {
        binding.progressBar.visibility = View.GONE
        binding.favoritesRecyclerView.visibility = View.GONE
        binding.ivEmptyState.visibility = View.VISIBLE
        binding.tvEmptyMessage.visibility = View.VISIBLE
        binding.tvEmptyMessage.text = message
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val layoutManager = binding.favoritesRecyclerView.layoutManager as LinearLayoutManager
        outState.putInt(KEY_RECYCLER_POSITION, layoutManager.findFirstVisibleItemPosition())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        clickDebounceJob?.cancel()
        _binding = null
    }
}