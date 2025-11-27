package com.example.playlistmaker

import java.io.Serializable

data class Track(
    val trackId: Long,
    val trackName: String,
    val artistName: String,
    val trackTimeMillis: Long,
    val artworkUrl100: String,
    val collectionName: String?,
    val releaseDate: String?,
    val primaryGenreName: String?,
    val country: String?,
    val previewUrl: String?
) : Serializable {
    fun getFormattedTime(): String {
        val minutes = (trackTimeMillis / 60000)
        val seconds = (trackTimeMillis % 60000) / 1000
        return String.format("%d:%02d", minutes, seconds)
    }

    // Функция для получения URL обложки высокого качества
    fun getHighResArtworkUrl(): String {
        return artworkUrl100.replace("100x100bb", "512x512bb")
    }

    // Функция для получения года из releaseDate
    fun getReleaseYear(): String? {
        return releaseDate?.take(4) // Берем первые 4 символа (год)
    }
}