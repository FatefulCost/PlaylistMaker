package com.example.playlistmaker.feature.media.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String?,
    val coverPath: String?, // Путь к файлу обложки
    val tracksIdsJson: String, // JSON строка со списком ID треков
    val tracksCount: Int
) {
    companion object {
        fun tracksIdsToList(tracksIdsJson: String): List<Long> {
            return try {
                val type = object : TypeToken<List<Long>>() {}.type
                Gson().fromJson(tracksIdsJson, type) ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        }

        fun listToTracksIds(tracksIds: List<Long>): String {
            return Gson().toJson(tracksIds)
        }
    }
}