package com.example.playlistmaker.feature.media.domain.model

import java.io.Serializable

data class Playlist(
    val id: Long = 0,
    val name: String,
    val description: String? = null,
    val coverPath: String? = null,
    val tracksIds: List<Long> = emptyList(),
    val tracksCount: Int = 0
) : Serializable {

    fun hasTrack(trackId: Long): Boolean {
        return tracksIds.contains(trackId)
    }

    fun addTrack(trackId: Long): Playlist {
        val newTracksIds = tracksIds.toMutableList().apply {
            add(trackId)
        }
        return copy(
            tracksIds = newTracksIds,
            tracksCount = tracksCount + 1
        )
    }
}