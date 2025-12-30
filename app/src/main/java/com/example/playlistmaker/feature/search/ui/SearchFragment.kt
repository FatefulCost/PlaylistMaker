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
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.playlistmaker.R
import com.example.playlistmaker.feature.player.ui.PlayerFragment
import com.example.playlistmaker.feature.search.ui.adapter.TracksAdapter
import com.example.playlistmaker.feature.search.ui.viewmodel.SearchUiState
import com.example.playlistmaker.feature.search.ui.viewmodel.SearchViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class SearchFragment : Fragment() {

    private lateinit var searchEditText: EditText
    private lateinit var tracksRecyclerView: RecyclerView
    private lateinit var historyRecyclerView: RecyclerView
    private lateinit var historyLayout: LinearLayout
    private lateinit var clearHistoryButton: Button
    private lateinit var placeholderLayout: View
    private lateinit var placeholderIcon: ImageView
    private lateinit var placeholderText: TextView
    private lateinit var retryButton: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var inputMethodManager: InputMethodManager

    private lateinit var tracksAdapter: TracksAdapter
    private lateinit var historyAdapter: TracksAdapter

    private val handler = Handler(Looper.getMainLooper())
    private var searchRunnable: Runnable? = null
    private val DEBOUNCE_DELAY = 2000L

    private var lastClickTime = 0L
    private val CLICK_DEBOUNCE_DELAY = 1000L

    private val viewModel: SearchViewModel by viewModel()

    companion object {
        private const val SEARCH_QUERY_KEY = "search_query_key"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        setupSearchField()
        setupRecyclerView()
        setupObservers()

        // Загружаем историю при старте
        viewModel.loadSearchHistory()
    }

    private fun initViews(view: View) {
        searchEditText = view.findViewById(R.id.search_edit_text)
        tracksRecyclerView = view.findViewById(R.id.tracks_recycler_view)
        historyRecyclerView = view.findViewById(R.id.history_recycler_view)
        historyLayout = view.findViewById(R.id.history_layout)
        clearHistoryButton = view.findViewById(R.id.clear_history_button)
        placeholderLayout = view.findViewById(R.id.placeholder_layout)
        placeholderIcon = view.findViewById(R.id.placeholder_icon)
        placeholderText = view.findViewById(R.id.placeholder_text)
        retryButton = view.findViewById(R.id.retry_button)
        progressBar = view.findViewById(R.id.progressBar)
        inputMethodManager = requireContext().getSystemService(InputMethodManager::class.java)
    }

    private fun setupRecyclerView() {
        tracksAdapter = TracksAdapter { track ->
            onTrackClicked(track)
        }

        tracksRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = tracksAdapter
            setHasFixedSize(true)
        }

        historyAdapter = TracksAdapter { track ->
            onTrackClicked(track)
        }
        historyRecyclerView.apply {
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

    // Остальные методы (showInitialState, showSearchHistory и т.д.) переносим из SearchActivity
    // с заменой контекста на requireContext() и view на rootView

    @SuppressLint("ClickableViewAccessibility")
    private fun setupSearchField() {
        val searchIcon = ContextCompat.getDrawable(requireContext(), R.drawable.vector_lupa_search)
        searchEditText.setCompoundDrawablesWithIntrinsicBounds(
            searchIcon, null, null, null
        )

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateClearIcon(s)

                searchRunnable?.let { handler.removeCallbacks(it) }

                if (!s.isNullOrEmpty() && s.toString().isNotBlank()) {
                    searchRunnable = Runnable {
                        performDebouncedSearch(s.toString())
                    }
                    handler.postDelayed(searchRunnable!!, DEBOUNCE_DELAY)
                } else {
                    viewModel.loadSearchHistory()
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE) {
                searchRunnable?.let { handler.removeCallbacks(it) }
                performSearch()
                hideKeyboard()
                return@setOnEditorActionListener true
            }
            false
        }

        searchEditText.setOnClickListener { showKeyboard() }

        searchEditText.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val editText = v as EditText
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
        val query = searchEditText.text.toString().trim()
        if (query.isNotBlank()) {
            viewModel.searchTracks(query)
        }
    }

    private fun clearSearch() {
        searchEditText.setText("")
        hideKeyboard()
        updateClearIcon("")
        viewModel.loadSearchHistory()
        searchRunnable?.let { handler.removeCallbacks(it) }
    }

    private fun showInitialState() {
        historyLayout.visibility = View.GONE
        tracksRecyclerView.visibility = View.GONE
        placeholderLayout.visibility = View.GONE
        progressBar.visibility = View.GONE
        clearHistoryButton.visibility = View.GONE
    }

    private fun showSearchHistory(history: List<com.example.playlistmaker.feature.search.domain.model.Track>) {
        if (history.isNotEmpty()) {
            historyLayout.visibility = View.VISIBLE
            tracksRecyclerView.visibility = View.GONE
            placeholderLayout.visibility = View.GONE
            progressBar.visibility = View.GONE
            historyAdapter.updateTracks(history)
            clearHistoryButton.visibility = View.VISIBLE
            clearHistoryButton.setOnClickListener {
                viewModel.clearSearchHistory()
            }
        } else {
            showInitialState()
        }
    }

    private fun showLoading() {
        progressBar.visibility = View.VISIBLE
        tracksRecyclerView.visibility = View.GONE
        historyLayout.visibility = View.GONE
        placeholderLayout.visibility = View.GONE
        clearHistoryButton.visibility = View.GONE
    }

    private fun showResults(tracks: List<com.example.playlistmaker.feature.search.domain.model.Track>) {
        progressBar.visibility = View.GONE
        tracksRecyclerView.visibility = View.VISIBLE
        historyLayout.visibility = View.GONE
        placeholderLayout.visibility = View.GONE
        clearHistoryButton.visibility = View.GONE
        tracksAdapter.updateTracks(tracks)
    }

    private fun showEmptyResults() {
        progressBar.visibility = View.GONE
        tracksRecyclerView.visibility = View.GONE
        historyLayout.visibility = View.GONE
        placeholderLayout.visibility = View.VISIBLE
        placeholderIcon.visibility = View.VISIBLE
        placeholderIcon.setImageResource(R.drawable.nothing_found)
        placeholderText.text = getString(R.string.search_empty)
        retryButton.visibility = View.GONE
        clearHistoryButton.visibility = View.GONE
    }

    private fun showError(message: String) {
        progressBar.visibility = View.GONE
        tracksRecyclerView.visibility = View.GONE
        historyLayout.visibility = View.GONE
        placeholderLayout.visibility = View.VISIBLE
        placeholderIcon.visibility = View.VISIBLE
        placeholderIcon.setImageResource(R.drawable.no_network)
        placeholderText.text = message
        retryButton.visibility = View.VISIBLE
        retryButton.setOnClickListener { viewModel.retryLastSearch() }
        clearHistoryButton.visibility = View.GONE
    }

    private fun updateClearIcon(text: CharSequence?) {
        val clearIcon = if (text.isNullOrEmpty()) {
            null
        } else {
            ContextCompat.getDrawable(requireContext(), R.drawable.ic_clear)
        }

        val searchIcon = ContextCompat.getDrawable(requireContext(), R.drawable.vector_lupa_search)
        searchEditText.setCompoundDrawablesWithIntrinsicBounds(
            searchIcon, null, clearIcon, null
        )
    }

    private fun showKeyboard() {
        searchEditText.requestFocus()
        searchEditText.postDelayed({
            inputMethodManager.showSoftInput(searchEditText, InputMethodManager.SHOW_IMPLICIT)
        }, 100)
    }

    private fun hideKeyboard() {
        inputMethodManager.hideSoftInputFromWindow(searchEditText.windowToken, 0)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(SEARCH_QUERY_KEY, searchEditText.text.toString())
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        val savedQuery = savedInstanceState?.getString(SEARCH_QUERY_KEY, "")
        searchEditText.setText(savedQuery)
        updateClearIcon(savedQuery)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacksAndMessages(null)
        searchRunnable = null

    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }
}