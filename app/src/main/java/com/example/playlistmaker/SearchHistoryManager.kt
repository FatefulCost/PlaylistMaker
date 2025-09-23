package com.example.playlistmaker

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SearchHistoryManager(private val context: Context) {

    companion object {
        private const val PREFS_NAME = "search_history_prefs"
        private const val KEY_HISTORY = "search_history"
        private const val MAX_HISTORY_SIZE = 10
    }

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    // Добавить трек в историю
    fun addTrackToHistory(track: Track) {
        val history = getSearchHistory().toMutableList()

        // Удаляем трек если он уже есть в истории (по trackId)
        history.removeAll { it.trackId == track.trackId }

        // Добавляем трек в начало списка
        history.add(0, track)

        // Ограничиваем размер истории
        if (history.size > MAX_HISTORY_SIZE) {
            history.subList(MAX_HISTORY_SIZE, history.size).clear()
        }

        // Сохраняем обновленную историю
        saveSearchHistory(history)
    }

    // Получить историю поиска
    fun getSearchHistory(): List<Track> {
        val historyJson = sharedPreferences.getString(KEY_HISTORY, null)
        return if (historyJson != null) {
            val type = object : TypeToken<List<Track>>() {}.type
            gson.fromJson(historyJson, type) ?: emptyList()
        } else {
            emptyList()
        }
    }

    // Очистить историю поиска
    fun clearSearchHistory() {
        sharedPreferences.edit().remove(KEY_HISTORY).apply()
    }

    // Проверить, пустая ли история
    fun isHistoryEmpty(): Boolean {
        return getSearchHistory().isEmpty()
    }

    // Сохранить историю в SharedPreferences
    private fun saveSearchHistory(history: List<Track>) {
        val historyJson = gson.toJson(history)
        sharedPreferences.edit().putString(KEY_HISTORY, historyJson).apply()
    }
}