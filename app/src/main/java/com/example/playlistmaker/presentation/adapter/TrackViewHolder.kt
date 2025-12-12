package com.example.playlistmaker.presentation.adapter

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.R
import com.example.playlistmaker.domain.model.Track

class TrackViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val artworkImageView: ImageView = itemView.findViewById(R.id.artworkImageView)
    private val trackNameTextView: TextView = itemView.findViewById(R.id.trackNameTextView)
    private val artistNameTextView: TextView = itemView.findViewById(R.id.artistNameTextView)
    private val trackTimeTextView: TextView = itemView.findViewById(R.id.trackTimeTextView)
    private val dotTextView: TextView = itemView.findViewById(R.id.dotTextView)

    fun bind(track: Track) {
        // Загружаем обложку с помощью Glide
        if (track.artworkUrl100.isNotEmpty()) {
            Glide.with(itemView.context)
                .load(track.artworkUrl100)
                .placeholder(R.drawable.vector_placeholder)
                .error(R.drawable.vector_placeholder)
                .centerCrop()
                .transform(RoundedCorners(10))
                .into(artworkImageView)
        } else {
            // Если URL пустой, показываем плейсхолдер
            artworkImageView.setImageResource(R.drawable.vector_placeholder)
        }

        trackNameTextView.text = track.trackName
        artistNameTextView.text = track.artistName
        trackTimeTextView.text = track.getFormattedTime()

        // Показываем точку только если есть и исполнитель и время
        dotTextView.visibility = if (track.artistName.isNotEmpty() && track.trackTimeMillis > 0) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }
}