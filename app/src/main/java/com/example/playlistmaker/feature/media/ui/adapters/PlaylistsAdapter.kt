package com.example.playlistmaker.feature.media.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.PlaylistItemBinding
import com.example.playlistmaker.feature.media.domain.model.Playlist

class PlaylistsAdapter(
    private var playlists: List<Playlist> = emptyList(),
    private val onPlaylistClick: (Playlist) -> Unit = {}
) : RecyclerView.Adapter<PlaylistsAdapter.PlaylistViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
        val binding = PlaylistItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PlaylistViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        val playlist = playlists[position]
        holder.bind(playlist)

        holder.itemView.setOnClickListener {
            onPlaylistClick(playlist)
        }
    }

    override fun getItemCount(): Int = playlists.size

    fun updatePlaylists(newPlaylists: List<Playlist>) {
        playlists = newPlaylists
        notifyDataSetChanged()
    }

    class PlaylistViewHolder(
        private val binding: PlaylistItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(playlist: Playlist) {
            binding.playlistNameTextView.text = playlist.name

            val tracksCountText = getTracksCountText(playlist.tracksCount)
            binding.playlistTracksCountTextView.text = tracksCountText

            // Загружаем обложку или показываем заглушку
            loadCover(playlist.coverPath)
        }

        private fun loadCover(coverPath: String?) {
            if (!coverPath.isNullOrEmpty()) {
                Glide.with(binding.root.context)
                    .load(coverPath)
                    .placeholder(R.drawable.vector_placeholder)
                    .error(R.drawable.vector_placeholder)
                    .centerCrop()
                    .transform(RoundedCorners(8))
                    .into(binding.playlistCoverImageView)
            } else {
                // Если нет обложки, показываем заглушку
                binding.playlistCoverImageView.setImageResource(R.drawable.vector_placeholder)
            }
        }

        private fun getTracksCountText(count: Int): String {
            return when {
                count % 10 == 1 && count % 100 != 11 -> "$count трек"
                count % 10 in 2..4 && count % 100 !in 12..14 -> "$count трека"
                else -> "$count треков"
            }
        }
    }
}