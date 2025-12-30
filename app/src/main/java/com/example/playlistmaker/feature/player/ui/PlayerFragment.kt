package com.example.playlistmaker.feature.player.ui

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.FragmentPlayerBinding
import com.example.playlistmaker.feature.search.domain.model.Track

class PlayerFragment : Fragment() {

    private var _binding: FragmentPlayerBinding? = null
    private val binding get() = _binding!!

    private var mediaPlayer: MediaPlayer? = null
    private var playbackPosition = 0
    private var isPlaying = false
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var updateProgressRunnable: Runnable

    private var currentTrack: Track? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlayerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupBackButton()
        setupPlayButton()
        displayTrackInfo()
        setupMediaPlayer()

        updateProgressRunnable = object : Runnable {
            override fun run() {
                updateProgress()
                handler.postDelayed(this, 300)
            }
        }
    }

    private fun setupBackButton() {
        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupPlayButton() {
        binding.playButton.setOnClickListener {
            togglePlayback()
        }
    }

    private fun displayTrackInfo() {
        currentTrack = arguments?.getSerializable("track") as? Track
        currentTrack?.let { track ->
            loadArtwork(track.getHighResArtworkUrl())
            binding.trackNameTextView.text = track.trackName
            binding.artistNameTextView.text = track.artistName

            val duration = track.getFormattedTime()
            binding.trackDurationTextView.text = duration
            binding.durationValueTextView.text = duration
            binding.progressTextView.text = "00:00"

            track.collectionName?.let { collectionName ->
                binding.albumValueTextView.text = collectionName
                binding.albumLabelTextView.visibility = View.VISIBLE
                binding.albumValueTextView.visibility = View.VISIBLE
            }

            track.getReleaseYear()?.let { releaseYear ->
                binding.yearValueTextView.text = releaseYear
                binding.yearLabelTextView.visibility = View.VISIBLE
                binding.yearValueTextView.visibility = View.VISIBLE
            }

            track.primaryGenreName?.let { genre ->
                binding.genreValueTextView.text = genre
                binding.genreLabelTextView.visibility = View.VISIBLE
                binding.genreValueTextView.visibility = View.VISIBLE
            }

            track.country?.let { country ->
                binding.countryValueTextView.text = country
                binding.countryLabelTextView.visibility = View.VISIBLE
                binding.countryValueTextView.visibility = View.VISIBLE
            }
        }
    }

    private fun setupMediaPlayer() {
        mediaPlayer = MediaPlayer().apply {
            setOnCompletionListener {
                playbackPosition = 0
                handler.removeCallbacks(updateProgressRunnable)
                binding.progressTextView.text = "00:00"
                this@PlayerFragment.isPlaying = false
                updatePlayButton()
            }
        }
    }

    private fun togglePlayback() {
        if (isPlaying) {
            pausePlayback()
        } else {
            startPlayback()
        }
    }

    private fun startPlayback() {
        val track = currentTrack
        val previewUrl = track?.previewUrl

        if (previewUrl.isNullOrEmpty()) {
            return
        }

        try {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.pause()
            }

            mediaPlayer?.reset()
            mediaPlayer?.setDataSource(previewUrl)
            mediaPlayer?.prepareAsync()

            mediaPlayer?.setOnPreparedListener {
                it.seekTo(playbackPosition)
                it.start()
                isPlaying = true
                updatePlayButton()
                handler.post(updateProgressRunnable)
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun pausePlayback() {
        mediaPlayer?.pause()
        playbackPosition = mediaPlayer?.currentPosition ?: 0
        isPlaying = false
        updatePlayButton()
        handler.removeCallbacks(updateProgressRunnable)
    }

    private fun stopPlayback() {
        mediaPlayer?.stop()
        playbackPosition = 0
        isPlaying = false
        updatePlayButton()
        handler.removeCallbacks(updateProgressRunnable)
        binding.progressTextView.text = "00:00"
    }

    private fun updateProgress() {
        mediaPlayer?.let { player ->
            if (player.isPlaying) {
                val currentPosition = player.currentPosition
                val minutes = currentPosition / 60000
                val seconds = (currentPosition % 60000) / 1000
                binding.progressTextView.text = String.format("%02d:%02d", minutes, seconds)
            }
        }
    }

    private fun updatePlayButton() {
        if (isPlaying) {
            binding.playButton.setImageResource(R.drawable.pause_button)
            binding.playButton.contentDescription = getString(R.string.pause)
        } else {
            binding.playButton.setImageResource(R.drawable.play_button)
            binding.playButton.contentDescription = getString(R.string.play)
        }
    }

    private fun loadArtwork(artworkUrl: String) {
        if (artworkUrl.isNotEmpty()) {
            Glide.with(this)
                .load(artworkUrl)
                .placeholder(R.drawable.vector_placeholder)
                .error(R.drawable.vector_placeholder)
                .centerCrop()
                .transform(RoundedCorners(16))
                .into(binding.artworkImageView)
        } else {
            binding.artworkImageView.setImageResource(R.drawable.vector_placeholder)
        }
    }

    override fun onPause() {
        super.onPause()
        if (isPlaying) {
            pausePlayback()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(updateProgressRunnable)
        mediaPlayer?.release()
        mediaPlayer = null
        _binding = null
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
    }

}