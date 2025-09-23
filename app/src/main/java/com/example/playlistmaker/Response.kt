package com.example.playlistmaker

import com.google.gson.annotations.SerializedName

data class ITunesSearchResponse(
    @SerializedName("resultCount") val resultCount: Int,
    @SerializedName("results") val results: List<TrackResponse>
)

data class TrackResponse(
    @SerializedName("trackId") val trackId: Long,
    @SerializedName("trackName") val trackName: String,
    @SerializedName("artistName") val artistName: String,
    @SerializedName("trackTimeMillis") val trackTimeMillis: Long,
    @SerializedName("artworkUrl100") val artworkUrl100: String
) {
    fun toTrack(): Track {
        return Track(
            trackId = trackId,
            trackName = trackName,
            artistName = artistName,
            trackTimeMillis = trackTimeMillis,
            artworkUrl100 = artworkUrl100
        )
    }
}