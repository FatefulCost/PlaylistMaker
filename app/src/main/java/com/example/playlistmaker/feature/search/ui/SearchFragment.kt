package com.example.playlistmaker.feature.search.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.FragmentSearchBinding
import com.example.playlistmaker.feature.search.domain.model.Track
import com.example.playlistmaker.feature.search.ui.adapter.TracksAdapter
import com.example.playlistmaker.feature.search.ui.viewmodel.SearchUiState
import com.example.playlistmaker.feature.search.ui.viewmodel.SearchViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private lateinit var tracksAdapter: TracksAdapter
    private lateinit var historyAdapter: TracksAdapter

    private val viewModel: SearchViewModel by viewModel()
    private lateinit var inputMethodManager: InputMethodManager

    // Job для debounce кликов
    private var clickDebounceJob: Job? = null
    private var lastClickedTrack: Track? = null

    // Флаг для предотвращения бесконечного цикла при обновлении текста
    private var isUpdatingFromViewModel = false

    companion object {
        private const val SEARCH_QUERY_KEY = "search_query_key"
        private const val CLICK_DEBOUNCE_DELAY = 1000L
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        inputMethodManager = requireContext().getSystemService(InputMethodManager::class.java)
        setupSearchField()
        setupRecyclerView()
        setupObservers()

        viewModel.searchText.observe(viewLifecycleOwner) { searchText ->
            if (!isUpdatingFromViewModel && binding.searchEditText.text.toString() != searchText) {
                isUpdatingFromViewModel = true
                binding.searchEditText.setText(searchText)
                updateClearIcon(searchText)
                isUpdatingFromViewModel = false
            }
        }

        if (savedInstanceState != null) {
            val savedQuery = savedInstanceState.getString(SEARCH_QUERY_KEY) ?: ""
            binding.searchEditText.setText(savedQuery)
            updateClearIcon(savedQuery)

            if (savedQuery.isBlank()) {
                viewModel.loadSearchHistory()
            }
        } else {
            // При первом создании загружаем историю только если нет сохраненного состояния
            viewModel.loadSearchHistory()
        }
    }

    override fun onResume() {
        super.onResume()
        // При возврате на фрагмент проверяем, нужно ли показать результаты поиска
        val currentText = binding.searchEditText.text.toString()
        if (currentText.isNotBlank()) {
        } else {
            viewModel.loadSearchHistory()
        }
    }

    private fun setupRecyclerView() {
        tracksAdapter = TracksAdapter(
            onTrackClick = { track ->
                onTrackClicked(track)
            }
        )

        binding.tracksRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = tracksAdapter
            setHasFixedSize(true)
        }

        historyAdapter = TracksAdapter(
            onTrackClick = { track ->
                onTrackClicked(track)
            }
        )

        binding.historyRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = historyAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupObservers() {
        viewModel.searchState.observe(viewLifecycleOwner) { state ->
            when (state) {
                SearchUiState.Initial -> showInitialState()
                SearchUiState.Loading -> showLoading()
                is SearchUiState.Success -> showResults(state.tracks)
                is SearchUiState.Error -> showError(state.message)
                SearchUiState.Empty -> showEmptyResults()
                is SearchUiState.History -> showSearchHistory(state.tracks)
            }
        }
    }

    private fun onTrackClicked(track: Track) {
        clickDebounceJob?.cancel()

        clickDebounceJob = lifecycleScope.launch {
            delay(CLICK_DEBOUNCE_DELAY)

            if (lastClickedTrack?.trackId == track.trackId) {
                viewModel.addTrackToHistory(track)

                // Навигация к экрану плеера
                val bundle = Bundle().apply {
                    putSerializable("track", track)
                }
                findNavController().navigate(R.id.playerFragment, bundle)
            }
        }

        // Сохраняем последний нажатый трек
        lastClickedTrack = track
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupSearchField() {
        val searchIcon = ContextCompat.getDrawable(requireContext(), R.drawable.vector_lupa_search)
        binding.searchEditText.setCompoundDrawablesWithIntrinsicBounds(
            searchIcon, null, null, null
        )

        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (isUpdatingFromViewModel) return

                val newText = s?.toString() ?: ""
                updateClearIcon(newText)
                viewModel.onSearchTextChanged(newText)
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE) {
                hideKeyboard()
                return@setOnEditorActionListener true
            }
            false
        }

        binding.searchEditText.setOnClickListener { showKeyboard() }

        binding.searchEditText.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val editText = v as android.widget.EditText
                val drawableRight = editText.compoundDrawables[2]

                if (drawableRight != null) {
                    val clearIconStart = editText.width -
                            editText.paddingEnd -
                            drawableRight.intrinsicWidth

                    if (event.rawX >= clearIconStart) {
                        clearSearch()
                        return@setOnTouchListener true
                    }
                }
            }
            false
        }
    }

    private fun clearSearch() {
        binding.searchEditText.setText("")
        updateClearIcon("")
        hideKeyboard()
        viewModel.resetSearch()
    }

    private fun showInitialState() {
        binding.historyLayout.visibility = View.GONE
        binding.tracksRecyclerView.visibility = View.GONE
        binding.placeholderLayout.visibility = View.GONE
        binding.progressBar.visibility = View.GONE
        binding.clearHistoryButton.visibility = View.GONE
    }

    private fun showSearchHistory(history: List<Track>) {
        if (history.isNotEmpty()) {
            binding.historyLayout.visibility = View.VISIBLE
            binding.tracksRecyclerView.visibility = View.GONE
            binding.placeholderLayout.visibility = View.GONE
            binding.progressBar.visibility = View.GONE
            historyAdapter.updateTracks(history)
            binding.clearHistoryButton.visibility = View.VISIBLE
            binding.clearHistoryButton.setOnClickListener {
                viewModel.clearSearchHistory()
            }
        } else {
            showInitialState()
        }
    }

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.tracksRecyclerView.visibility = View.GONE
        binding.historyLayout.visibility = View.GONE
        binding.placeholderLayout.visibility = View.GONE
        binding.clearHistoryButton.visibility = View.GONE
    }

    private fun showResults(tracks: List<Track>) {
        binding.progressBar.visibility = View.GONE
        binding.tracksRecyclerView.visibility = View.VISIBLE
        binding.historyLayout.visibility = View.GONE
        binding.placeholderLayout.visibility = View.GONE
        binding.clearHistoryButton.visibility = View.GONE
        tracksAdapter.updateTracks(tracks)
    }

    private fun showEmptyResults() {
        binding.progressBar.visibility = View.GONE
        binding.tracksRecyclerView.visibility = View.GONE
        binding.historyLayout.visibility = View.GONE
        binding.placeholderLayout.visibility = View.VISIBLE

        binding.placeholderIcon.visibility = View.VISIBLE
        binding.placeholderIcon.setImageResource(R.drawable.nothing_found)
        binding.placeholderText.text = getString(R.string.search_empty)
        binding.retryButton.visibility = View.GONE

        binding.clearHistoryButton.visibility = View.GONE
    }

    private fun showError(message: String) {
        binding.progressBar.visibility = View.GONE
        binding.tracksRecyclerView.visibility = View.GONE
        binding.historyLayout.visibility = View.GONE
        binding.placeholderLayout.visibility = View.VISIBLE

        binding.placeholderIcon.visibility = View.VISIBLE
        binding.placeholderIcon.setImageResource(R.drawable.no_network)
        binding.placeholderText.text = message
        binding.retryButton.visibility = View.VISIBLE
        binding.retryButton.setOnClickListener { viewModel.retryLastSearch() }

        binding.clearHistoryButton.visibility = View.GONE
    }

    private fun updateClearIcon(text: CharSequence?) {
        val clearIcon = if (text.isNullOrEmpty()) {
            null
        } else {
            ContextCompat.getDrawable(requireContext(), R.drawable.ic_clear)
        }

        val searchIcon = ContextCompat.getDrawable(requireContext(), R.drawable.vector_lupa_search)
        binding.searchEditText.setCompoundDrawablesWithIntrinsicBounds(
            searchIcon, null, clearIcon, null
        )
    }

    private fun showKeyboard() {
        binding.searchEditText.requestFocus()
        binding.searchEditText.postDelayed({
            inputMethodManager.showSoftInput(binding.searchEditText, InputMethodManager.SHOW_IMPLICIT)
        }, 100)
    }

    private fun hideKeyboard() {
        inputMethodManager.hideSoftInputFromWindow(binding.searchEditText.windowToken, 0)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // Сохраняем текущий поисковый запрос
        outState.putString(SEARCH_QUERY_KEY, binding.searchEditText.text.toString())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        clickDebounceJob?.cancel() // Отменяем Job при уничтожении фрагмента
        _binding = null
    }
}