package com.example.playlistmaker.feature.media.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.ItemPlaylistSimpleBinding
import com.example.playlistmaker.feature.media.domain.model.Playlist

class PlaylistSimpleAdapter(
    private val onPlaylistClick: (Playlist) -> Unit
) : ListAdapter<Playlist, PlaylistSimpleAdapter.PlaylistViewHolder>(PlaylistDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
        val binding = ItemPlaylistSimpleBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PlaylistViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        val playlist = getItem(position)
        holder.bind(playlist)

        holder.itemView.setOnClickListener {
            onPlaylistClick(playlist)
        }
    }

    class PlaylistViewHolder(
        private val binding: ItemPlaylistSimpleBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(playlist: Playlist) {
            binding.playlistNameTextView.text = playlist.name
            binding.playlistTracksCountTextView.text = getTracksCountText(playlist.tracksCount)

            // Загружаем обложку
            if (!playlist.coverPath.isNullOrEmpty()) {
                Glide.with(binding.root.context)
                    .load(playlist.coverPath)
                    .placeholder(R.drawable.vector_placeholder)
                    .error(R.drawable.vector_placeholder)
                    .centerCrop()
                    .transform(RoundedCorners(8))
                    .into(binding.playlistCoverImageView)
            } else {
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

    class PlaylistDiffCallback : DiffUtil.ItemCallback<Playlist>() {
        override fun areItemsTheSame(oldItem: Playlist, newItem: Playlist): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Playlist, newItem: Playlist): Boolean {
            return oldItem == newItem
        }
    }
}