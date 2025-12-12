package com.example.playlistmaker.presentation.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.playlistmaker.R
import com.example.playlistmaker.data.api.RetrofitClient
import com.example.playlistmaker.domain.model.Track
import com.example.playlistmaker.presentation.adapter.TracksAdapter
import com.example.playlistmaker.presentation.creator.InteractorCreator
import com.example.playlistmaker.presentation.viewmodel.SearchStateUi
import com.example.playlistmaker.presentation.viewmodel.SearchViewModel
import com.example.playlistmaker.presentation.viewmodel.SearchViewModelFactory

class SearchActivity : AppCompatActivity() {
    private lateinit var themeInteractor: com.example.playlistmaker.domain.interactor.ThemeInteractor
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
    private lateinit var historyInteractor: com.example.playlistmaker.domain.interactor.HistoryInteractor

    private val handler = Handler(Looper.getMainLooper())
    private var searchRunnable: Runnable? = null
    private val DEBOUNCE_DELAY = 2000L

    private var lastClickTime = 0L
    private val CLICK_DEBOUNCE_DELAY = 1000L

    private val viewModel: SearchViewModel by viewModels {
        SearchViewModelFactory(
            InteractorCreator.createSearchInteractor(RetrofitClient.iTunesApi)
        )
    }

    companion object {
        private const val SEARCH_QUERY_KEY = "search_query_key"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        themeInteractor = InteractorCreator.createThemeInteractor(this)
        applySavedTheme()

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_search)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.search_activity)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()
        setupToolbar()
        setupSearchField()
        setupRecyclerView()
        setupHistory()
        setupObservers()
        showSearchHistory()
    }

    private fun applySavedTheme() {
        if (themeInteractor.isDarkTheme()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }


    private fun initViews() {
        searchEditText = findViewById(R.id.search_edit_text)
        tracksRecyclerView = findViewById(R.id.tracks_recycler_view)
        historyRecyclerView = findViewById(R.id.history_recycler_view)
        historyLayout = findViewById(R.id.history_layout)
        clearHistoryButton = findViewById(R.id.clear_history_button)
        placeholderLayout = findViewById(R.id.placeholder_layout)
        placeholderIcon = findViewById(R.id.placeholder_icon)
        placeholderText = findViewById(R.id.placeholder_text)
        retryButton = findViewById(R.id.retry_button)
        progressBar = findViewById(R.id.progressBar)
        inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager

        historyInteractor = InteractorCreator.createHistoryInteractor(this)
    }

    private fun setupToolbar() {
        val toolbar = findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar_search)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        tracksAdapter = TracksAdapter { track ->
            onTrackClicked(track)
        }

        tracksRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@SearchActivity)
            adapter = tracksAdapter
            setHasFixedSize(true)
        }

        historyAdapter = TracksAdapter { track ->
            onTrackClicked(track)
        }
        historyRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@SearchActivity)
            adapter = historyAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupHistory() {
        clearHistoryButton.setOnClickListener {
            historyInteractor.clearSearchHistory()
            showSearchHistory()
        }
    }

    private fun setupObservers() {
        viewModel.searchState.observe(this) { state ->
            when (state) {
                is SearchStateUi.Loading -> showLoading()
                is SearchStateUi.Success -> showResults(state.tracks)
                is SearchStateUi.Error -> showError(state.message)
                SearchStateUi.Empty -> showEmptyResults()
            }
        }
    }

    private fun onTrackClicked(track: Track) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastClickTime < CLICK_DEBOUNCE_DELAY) {
            return
        }
        lastClickTime = currentTime

        historyInteractor.addTrackToHistory(track)

        val intent = Intent(this, PlayerActivity::class.java)
        intent.putExtra("track", track)
        startActivity(intent)
    }

    private fun showSearchHistory() {
        val history = historyInteractor.getSearchHistory()
        if (history.isNotEmpty()) {
            historyLayout.visibility = View.VISIBLE
            tracksRecyclerView.visibility = View.GONE
            placeholderLayout.visibility = View.GONE
            progressBar.visibility = View.GONE
            historyAdapter.updateTracks(history)
            clearHistoryButton.visibility = View.VISIBLE
        } else {
            historyLayout.visibility = View.GONE
            tracksRecyclerView.visibility = View.GONE
            placeholderLayout.visibility = View.GONE
            progressBar.visibility = View.GONE
            clearHistoryButton.visibility = View.GONE
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupSearchField() {
        val searchIcon = ContextCompat.getDrawable(this, R.drawable.vector_lupa_search)
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
                    showSearchHistory()
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
            historyLayout.visibility = View.GONE
            viewModel.searchTracks(trimmedQuery)
        }
    }

    private fun performSearch() {
        val query = searchEditText.text.toString().trim()
        if (query.isNotBlank()) {
            historyLayout.visibility = View.GONE
            viewModel.searchTracks(query)
        }
    }

    private fun clearSearch() {
        searchEditText.setText("")
        hideKeyboard()
        tracksRecyclerView.visibility = View.GONE
        placeholderLayout.visibility = View.GONE
        progressBar.visibility = View.GONE
        updateClearIcon("")
        showSearchHistory()

        searchRunnable?.let { handler.removeCallbacks(it) }
    }

    private fun showLoading() {
        progressBar.visibility = View.VISIBLE
        tracksRecyclerView.visibility = View.GONE
        historyLayout.visibility = View.GONE
        placeholderLayout.visibility = View.GONE
    }

    private fun showResults(tracks: List<Track>) {
        progressBar.visibility = View.GONE
        tracksRecyclerView.visibility = View.VISIBLE
        historyLayout.visibility = View.GONE
        placeholderLayout.visibility = View.GONE
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
    }

    private fun updateClearIcon(text: CharSequence?) {
        val clearIcon = if (text.isNullOrEmpty()) {
            null
        } else {
            ContextCompat.getDrawable(this, R.drawable.ic_clear)
        }

        val searchIcon = ContextCompat.getDrawable(this, R.drawable.vector_lupa_search)
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

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        val savedQuery = savedInstanceState.getString(SEARCH_QUERY_KEY, "")
        searchEditText.setText(savedQuery)
        updateClearIcon(savedQuery)
    }

    override fun onBackPressed() {
        hideKeyboard()
        super.onBackPressed()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }
}