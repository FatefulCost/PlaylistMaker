package com.example.playlistmaker.feature.search.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.playlistmaker.feature.search.domain.model.Track
import com.example.playlistmaker.feature.search.domain.repository.HistoryRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class HistoryRepositoryImpl(
    private val context: Context
) : HistoryRepository {

    companion object {
        private const val PREFS_NAME = "search_history_prefs"
        private const val KEY_HISTORY = "search_history"
        private const val MAX_HISTORY_SIZE = 10
    }

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    override fun addTrackToHistory(track: Track) {
        val history = getSearchHistory().toMutableList()
        history.removeAll { it.trackId == track.trackId }
        history.add(0, track)

        if (history.size > MAX_HISTORY_SIZE) {
            history.subList(MAX_HISTORY_SIZE, history.size).clear()
        }

        saveSearchHistory(history)
    }

    override fun getSearchHistory(): List<Track> {
        val historyJson = sharedPreferences.getString(KEY_HISTORY, null)
        return if (historyJson != null) {
            val type = object : TypeToken<List<Track>>() {}.type
            gson.fromJson(historyJson, type) ?: emptyList()
        } else {
            emptyList()
        }
    }

    override fun clearSearchHistory() {
        sharedPreferences.edit().remove(KEY_HISTORY).apply()
    }

    override fun isHistoryEmpty(): Boolean {
        return getSearchHistory().isEmpty()
    }

    private fun saveSearchHistory(history: List<Track>) {
        val historyJson = gson.toJson(history)
        sharedPreferences.edit().putString(KEY_HISTORY, historyJson).apply()
    }
}