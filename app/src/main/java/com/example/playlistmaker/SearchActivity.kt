package com.example.playlistmaker

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class SearchActivity : AppCompatActivity() {
    // lateinit переменные для View элементов
    private lateinit var searchEditText: EditText
    private lateinit var tracksRecyclerView: RecyclerView
    private lateinit var placeholderLayout: View
    private lateinit var placeholderIcon: ImageView
    private lateinit var placeholderText: TextView
    private lateinit var retryButton: Button
    private lateinit var inputMethodManager: InputMethodManager

    // Используем ViewModel для управления состоянием
    private val viewModel: SearchViewModel by viewModels()
    private lateinit var tracksAdapter: TracksAdapter

    companion object {
        private const val SEARCH_QUERY_KEY = "search_query_key" // Ключ для сохранения состояния
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_search)

        // Обработка системных отступов (для notch экранов)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.search_activity)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()          // Инициализация View элементов
        setupToolbar()       // Настройка тулбара
        setupSearchField()   // Настройка поля поиска
        setupRecyclerView()  // Настройка RecyclerView
        setupObservers()     // Настройка наблюдения за ViewModel
    }

    private fun initViews() {
        searchEditText = findViewById(R.id.search_edit_text)
        tracksRecyclerView = findViewById(R.id.tracks_recycler_view)
        placeholderLayout = findViewById(R.id.placeholder_layout)
        placeholderIcon = findViewById(R.id.placeholder_icon)
        placeholderText = findViewById(R.id.placeholder_text)
        retryButton = findViewById(R.id.retry_button)
        inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
    }

    private fun setupToolbar() {
        val toolbar = findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar_search)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // Показываем кнопку назад
        toolbar.setNavigationOnClickListener { finish() } // Закрываем Activity при нажатии
    }

    private fun setupRecyclerView() {
        tracksAdapter = TracksAdapter()
        tracksRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@SearchActivity) // Вертикальный список
            adapter = tracksAdapter                                  // Наш адаптер
            setHasFixedSize(true)                                    // Оптимизация производительности
        }
    }

    private fun setupObservers() {
        viewModel.searchState.observe(this) { state ->
            when (state) {
                is SearchState.Success -> {
                    if (state.tracks.isEmpty()) {
                        showLoading() // Пустой список = показываем загрузку
                    } else {
                        showResults(state.tracks) // Непустой список = показываем результаты
                    }
                }
                is SearchState.Error -> showError(state.message)
                is SearchState.Empty -> showEmptyResults()
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupSearchField() {
        // Устанавливаем иконку поиска слева
        val searchIcon = ContextCompat.getDrawable(this, R.drawable.vector_lupa_search)
        searchEditText.setCompoundDrawablesWithIntrinsicBounds(
            searchIcon, null, null, null
        )

        // Слушатель изменений текста для обновления иконки очистки
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateClearIcon(s) // Обновляем иконку очистки
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Обработка нажатия кнопки Done на клавиатуре
        searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE) {
                performSearch() // Выполняем поиск
                hideKeyboard()  // Скрываем клавиатуру
                return@setOnEditorActionListener true
            }
            false
        }

        // Показ клавиатуры при касании поля
        searchEditText.setOnClickListener { showKeyboard() }

        // Обработка касания иконки очистки (справа)
        searchEditText.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val editText = v as EditText
                val drawableRight = editText.compoundDrawables[2] // Правая иконка

                if (drawableRight != null) {
                    // Вычисляем область иконки очистки
                    val clearIconStart = editText.width -
                            editText.paddingEnd -
                            drawableRight.intrinsicWidth

                    // Проверяем, было ли касание в области иконки
                    if (event.rawX >= clearIconStart) {
                        clearSearch() // Очищаем поиск
                        return@setOnTouchListener true
                    }
                }
            }
            false
        }
    }

    private fun performSearch() {
        val query = searchEditText.text.toString().trim()
        if (query.isNotBlank()) {
            viewModel.searchTracks(query) // Запускаем поиск через ViewModel
        }
    }

    private fun clearSearch() {
        searchEditText.setText("")        // Очищаем текст
        hideKeyboard()                    // Скрываем клавиатуру
        tracksRecyclerView.visibility = View.GONE    // Скрываем список
        placeholderLayout.visibility = View.GONE     // Скрываем плейсхолдер
        updateClearIcon("")               // Обновляем иконку
    }

    private fun showLoading() {
        tracksRecyclerView.visibility = View.GONE
        placeholderLayout.visibility = View.VISIBLE
        // Вместо иконки загрузки, используем только текст
        placeholderIcon.visibility = View.GONE
        placeholderText.text = getString(R.string.search_loading)
        retryButton.visibility = View.GONE
    }

    private fun showResults(tracks: List<Track>) {
        tracksRecyclerView.visibility = View.VISIBLE
        placeholderLayout.visibility = View.GONE
        tracksAdapter.updateTracks(tracks)
    }

    private fun showEmptyResults() {
        tracksRecyclerView.visibility = View.GONE
        placeholderLayout.visibility = View.VISIBLE
        placeholderIcon.visibility = View.VISIBLE // Показываем иконку для "ничего не найдено"
        placeholderIcon.setImageResource(R.drawable.nothing_found)
        placeholderText.text = getString(R.string.search_empty)
        retryButton.visibility = View.GONE
    }

    private fun showError(message: String) {
        tracksRecyclerView.visibility = View.GONE
        placeholderLayout.visibility = View.VISIBLE
        placeholderIcon.visibility = View.VISIBLE // Показываем иконку для ошибки
        placeholderIcon.setImageResource(R.drawable.no_network)
        placeholderText.text = message
        retryButton.visibility = View.VISIBLE
        retryButton.setOnClickListener { viewModel.retryLastSearch() }
    }

    private fun updateClearIcon(text: CharSequence?) {
        // Показываем иконку очистки только если есть текст
        val clearIcon = if (text.isNullOrEmpty()) {
            null
        } else {
            ContextCompat.getDrawable(this, R.drawable.ic_clear)
        }

        val searchIcon = ContextCompat.getDrawable(this, R.drawable.vector_lupa_search)
        searchEditText.setCompoundDrawablesWithIntrinsicBounds(
            searchIcon, null, clearIcon, null // Устанавливаем иконки слева и справа
        )
    }

    private fun showKeyboard() {
        searchEditText.requestFocus() // Фокусируемся на поле ввода
        searchEditText.postDelayed({
            // Показываем клавиатуру с задержкой (для корректного отображения)
            inputMethodManager.showSoftInput(searchEditText, InputMethodManager.SHOW_IMPLICIT)
        }, 100)
    }

    private fun hideKeyboard() {
        // Скрываем клавиатуру
        inputMethodManager.hideSoftInputFromWindow(searchEditText.windowToken, 0)
    }

    // Сохраняем текущий поисковый запрос при повороте экрана
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(SEARCH_QUERY_KEY, searchEditText.text.toString())
    }

    // Восстанавливаем поисковый запрос после поворота экрана
    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        val savedQuery = savedInstanceState.getString(SEARCH_QUERY_KEY, "")
        searchEditText.setText(savedQuery)
        updateClearIcon(savedQuery)
    }

    // Скрываем клавиатуру при нажатии назад
    override fun onBackPressed() {
        hideKeyboard()
        super.onBackPressed()
    }
}