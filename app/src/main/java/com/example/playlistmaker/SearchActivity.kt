package com.example.playlistmaker

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class SearchActivity : AppCompatActivity() {
    private lateinit var searchEditText: EditText
    private lateinit var inputMethodManager: InputMethodManager
    private var searchQuery: String = ""

    companion object {
        private const val SEARCH_QUERY_KEY = "search_query_key"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_search)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.search_activity)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val toolbar = findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar_search)
        setSupportActionBar(toolbar)

        // Включаем кнопку "Назад"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Обработчик нажатия на стрелку
        toolbar.setNavigationOnClickListener {
            finish()
        }

        searchEditText = findViewById(R.id.search_edit_text)
        inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager

        setupSearchField()
        showKeyboard()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // Сохраняем текущий текст поиска в Bundle
        outState.putString(SEARCH_QUERY_KEY, searchQuery)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        // Достаем сохраненные данные из Bundle
        val savedQuery = savedInstanceState.getString(SEARCH_QUERY_KEY, "")
        // Устанавливаем восстановленные данные обратно в EditText
        searchEditText.setText(savedQuery)
        searchQuery = savedQuery
        // Обновляем иконку очистки
        updateClearIcon(savedQuery)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupSearchField() {
        // Устанавливаем иконку поиска
        val searchIcon = ContextCompat.getDrawable(this, R.drawable.vector_lupa_search)
        searchEditText.setCompoundDrawablesWithIntrinsicBounds(
            searchIcon,  // left
            null,        // top
            null,        // right
            null         // bottom
        )

        // Используем TextWatcher для сохранения значения строки поля ввода в переменную
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Сохраняем значение строки поля ввода в переменную
                searchQuery = s?.toString() ?: ""
                updateClearIcon(s)
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // Автофокус и показ клавиатуры при касании поля
        searchEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                showKeyboard()
            }
        }

        // Обработка клика на иконку очистки
        searchEditText.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val editText = v as EditText
                val drawableRight = editText.compoundDrawables[2]

                if (drawableRight != null) {
                    // Вычисляем область иконки очистки
                    val clearIconStart = editText.width -
                            editText.paddingEnd -
                            drawableRight.intrinsicWidth

                    // Проверяем, было ли касание в области иконки
                    if (event.rawX >= clearIconStart) {
                        editText.setText("")
                        searchQuery = "" // Очищаем переменную
                        hideKeyboard()
                        return@setOnTouchListener true
                    }
                }
            }
            false
        }
    }

    private fun updateClearIcon(text: CharSequence?) {
        val clearIcon = if (text.isNullOrEmpty()) {
            null
        } else {
            ContextCompat.getDrawable(this, R.drawable.ic_clear)
        }

        val searchIcon = ContextCompat.getDrawable(this, R.drawable.vector_lupa_search)

        searchEditText.setCompoundDrawablesWithIntrinsicBounds(
            searchIcon,  // left
            null,        // top
            clearIcon,   // right
            null         // bottom
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

    // Закрываем клавиатуру при нажатии назад
    override fun onBackPressed() {
        hideKeyboard()
        super.onBackPressed()
    }
}