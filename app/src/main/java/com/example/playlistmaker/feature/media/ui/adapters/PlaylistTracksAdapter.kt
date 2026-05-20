package com.example.playlistmaker.feature.media.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.PlaylistTrackItemBinding
import com.example.playlistmaker.feature.search.domain.model.Track

class PlaylistTracksAdapter(
    private var tracks: List<Track> = emptyList(),
    private val onTrackClick: (Track) -> Unit = {},
    private val onTrackLongClick: (Track) -> Boolean = { true }
) : RecyclerView.Adapter<PlaylistTracksAdapter.TrackViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackViewHolder {
        val binding = PlaylistTrackItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TrackViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {
        val track = tracks[position]
        holder.bind(track)

        holder.itemView.setOnClickListener {
            onTrackClick(track)
        }

        holder.itemView.setOnLongClickListener {
            onTrackLongClick(track)
        }
    }

    override fun getItemCount(): Int = tracks.size

    fun updateTracks(newTracks: List<Track>) {
        tracks = newTracks
        notifyDataSetChanged()
    }

    class TrackViewHolder(
        private val binding: PlaylistTrackItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(track: Track) {
            // Загружаем обложку
            if (track.artworkUrl100.isNotEmpty()) {
                Glide.with(binding.root.context)
                    .load(track.artworkUrl100)
                    .placeholder(R.drawable.vector_placeholder)
                    .error(R.drawable.vector_placeholder)
                    .centerCrop()
                    .transform(RoundedCorners(4))
                    .into(binding.artworkImageView)
            } else {
                binding.artworkImageView.setImageResource(R.drawable.vector_placeholder)
            }

            binding.trackNameTextView.text = track.trackName
            binding.artistNameTextView.text = track.artistName
            binding.trackTimeTextView.text = track.getFormattedTime()
        }
    }
}