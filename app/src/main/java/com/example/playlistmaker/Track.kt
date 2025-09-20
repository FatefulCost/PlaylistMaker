package com.example.playlistmaker

data class Track(
    val trackName: String,          // Название композиции
    val artistName: String,         // Имя исполнителя
    val trackTimeMillis: Long,          // Продолжительность трека
    val artworkUrl100: String       // Ссылка на изображение обложки
) {
    fun getFormattedTime(): String {
        val minutes = (trackTimeMillis / 60000)
        val seconds = (trackTimeMillis % 60000) / 1000
        return String.format("%d:%02d", minutes, seconds)
    }
}