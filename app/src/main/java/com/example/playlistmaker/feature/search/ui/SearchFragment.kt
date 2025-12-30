package com.example.playlistmaker.feature.search.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.FragmentSearchBinding
import com.example.playlistmaker.feature.search.ui.adapter.TracksAdapter
import com.example.playlistmaker.feature.search.ui.viewmodel.SearchUiState
import com.example.playlistmaker.feature.search.ui.viewmodel.SearchViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private lateinit var tracksAdapter: TracksAdapter
    private lateinit var historyAdapter: TracksAdapter

    private val handler = Handler(Looper.getMainLooper())
    private var searchRunnable: Runnable? = null
    private val DEBOUNCE_DELAY = 2000L

    private var lastClickTime = 0L
    private val CLICK_DEBOUNCE_DELAY = 1000L

    private val viewModel: SearchViewModel by viewModel()
    private lateinit var inputMethodManager: InputMethodManager

    // Флаг для предотвращения бесконечного цикла при обновлении текста
    private var isUpdatingFromViewModel = false

    companion object {
        private const val SEARCH_QUERY_KEY = "search_query_key"
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

        // Восстанавливаем текст поиска из ViewModel
        viewModel.searchText.observe(viewLifecycleOwner) { searchText ->
            if (!isUpdatingFromViewModel && binding.searchEditText.text.toString() != searchText) {
                isUpdatingFromViewModel = true
                binding.searchEditText.setText(searchText)
                updateClearIcon(searchText)
                isUpdatingFromViewModel = false
            }
        }

        // Восстанавливаем состояние из savedInstanceState
        savedInstanceState?.getString(SEARCH_QUERY_KEY)?.let { savedQuery ->
            binding.searchEditText.setText(savedQuery)
            updateClearIcon(savedQuery)
            viewModel.updateSearchText(savedQuery)

            if (savedQuery.isNotBlank()) {
                viewModel.searchTracks(savedQuery)
            } else {
                viewModel.loadSearchHistory()
            }
        } ?: run {
            // Иначе загружаем состояние из ViewModel
            viewModel.loadSearchHistory()
        }
    }

    private fun setupRecyclerView() {
        tracksAdapter = TracksAdapter { track ->
            onTrackClicked(track)
        }

        binding.tracksRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = tracksAdapter
            setHasFixedSize(true)
        }

        historyAdapter = TracksAdapter { track ->
            onTrackClicked(track)
        }

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

    private fun onTrackClicked(track: com.example.playlistmaker.feature.search.domain.model.Track) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastClickTime < CLICK_DEBOUNCE_DELAY) {
            return
        }
        lastClickTime = currentTime

        // Добавляем трек в историю через ViewModel
        viewModel.addTrackToHistory(track)

        // Навигация к экрану плеера
        val bundle = Bundle()
        bundle.putSerializable("track", track)
        findNavController().navigate(R.id.playerFragment, bundle)
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

                updateClearIcon(s)

                searchRunnable?.let { handler.removeCallbacks(it) }

                if (!s.isNullOrEmpty() && s.toString().isNotBlank()) {
                    searchRunnable = Runnable {
                        performDebouncedSearch(s.toString())
                    }
                    handler.postDelayed(searchRunnable!!, DEBOUNCE_DELAY)

                    // Сохраняем текст в ViewModel
                    viewModel.updateSearchText(s.toString())
                } else {
                    // При очистке поля показываем историю
                    viewModel.updateSearchText("")
                    viewModel.resetSearch()
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE) {
                searchRunnable?.let { handler.removeCallbacks(it) }
                performSearch()
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

    private fun performDebouncedSearch(query: String) {
        val trimmedQuery = query.trim()
        if (trimmedQuery.isNotBlank()) {
            viewModel.searchTracks(trimmedQuery)
        }
    }

    private fun performSearch() {
        val query = binding.searchEditText.text.toString().trim()
        if (query.isNotBlank()) {
            viewModel.searchTracks(query)
        }
    }

    private fun clearSearch() {
        // Останавливаем любой текущий поиск
        searchRunnable?.let { handler.removeCallbacks(it) }
        searchRunnable = null

        // Очищаем поле ввода
        binding.searchEditText.setText("")

        // Обновляем иконку
        updateClearIcon("")

        // Скрываем клавиатуру
        hideKeyboard()

        // Обновляем состояние в ViewModel
        viewModel.updateSearchText("")
        viewModel.resetSearch()

        // НЕМЕДЛЕННО загружаем историю
        viewModel.loadSearchHistory()
    }

    private fun showInitialState() {
        binding.historyLayout.visibility = View.GONE
        binding.tracksRecyclerView.visibility = View.GONE
        binding.placeholderLayout.visibility = View.GONE
        binding.progressBar.visibility = View.GONE
        binding.clearHistoryButton.visibility = View.GONE
    }

    private fun showSearchHistory(history: List<com.example.playlistmaker.feature.search.domain.model.Track>) {
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

    private fun showResults(tracks: List<com.example.playlistmaker.feature.search.domain.model.Track>) {
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
        handler.removeCallbacksAndMessages(null)
        _binding = null
    }
}