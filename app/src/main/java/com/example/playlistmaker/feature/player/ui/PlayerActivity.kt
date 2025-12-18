package com.example.playlistmaker.feature.player.ui

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.R
import com.example.playlistmaker.feature.search.domain.model.Track

class PlayerActivity : AppCompatActivity() {

    private lateinit var backButton: ImageButton
    private lateinit var artworkImageView: ImageView
    private lateinit var trackNameTextView: TextView
    private lateinit var artistNameTextView: TextView
    private lateinit var trackDurationTextView: TextView
    private lateinit var playButton: ImageButton
    private lateinit var addToPlaylistButton: ImageButton
    private lateinit var addToFavoritesButton: ImageButton
    private lateinit var durationValueTextView: TextView
    private lateinit var albumLabelTextView: TextView
    private lateinit var albumValueTextView: TextView
    private lateinit var yearLabelTextView: TextView
    private lateinit var yearValueTextView: TextView
    private lateinit var genreLabelTextView: TextView
    private lateinit var genreValueTextView: TextView
    private lateinit var countryLabelTextView: TextView
    private lateinit var countryValueTextView: TextView
    private lateinit var progressTextView: TextView

    private var mediaPlayer: MediaPlayer? = null
    private var playbackPosition = 0
    private var isPlaying = false
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var updateProgressRunnable: Runnable

    private var currentTrack: Track? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        initViews()
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

    private fun initViews() {
        backButton = findViewById(R.id.backButton)
        artworkImageView = findViewById(R.id.artworkImageView)
        trackNameTextView = findViewById(R.id.trackNameTextView)
        artistNameTextView = findViewById(R.id.artistNameTextView)
        trackDurationTextView = findViewById(R.id.trackDurationTextView)
        durationValueTextView = findViewById(R.id.durationValueTextView)
        playButton = findViewById(R.id.playButton)
        addToPlaylistButton = findViewById(R.id.addToPlaylistButton)
        addToFavoritesButton = findViewById(R.id.addToFavoritesButton)
        progressTextView = findViewById(R.id.progressTextView)

        albumLabelTextView = findViewById(R.id.albumLabelTextView)
        albumValueTextView = findViewById(R.id.albumValueTextView)
        yearLabelTextView = findViewById(R.id.yearLabelTextView)
        yearValueTextView = findViewById(R.id.yearValueTextView)
        genreLabelTextView = findViewById(R.id.genreLabelTextView)
        genreValueTextView = findViewById(R.id.genreValueTextView)
        countryLabelTextView = findViewById(R.id.countryLabelTextView)
        countryValueTextView = findViewById(R.id.countryValueTextView)
    }

    private fun setupBackButton() {
        backButton.setOnClickListener {
            onBackPressed()
        }
    }

    private fun setupPlayButton() {
        playButton.setOnClickListener {
            togglePlayback()
        }
    }

    private fun displayTrackInfo() {
        currentTrack = intent.getSerializableExtra("track") as? Track
        currentTrack?.let { track ->
            loadArtwork(track.getHighResArtworkUrl())
            trackNameTextView.text = track.trackName
            artistNameTextView.text = track.artistName

            val duration = track.getFormattedTime()
            trackDurationTextView.text = duration
            durationValueTextView.text = duration
            progressTextView.text = "00:00"

            track.collectionName?.let { collectionName ->
                albumValueTextView.text = collectionName
                albumLabelTextView.visibility = TextView.VISIBLE
                albumValueTextView.visibility = TextView.VISIBLE
            }

            track.getReleaseYear()?.let { releaseYear ->
                yearValueTextView.text = releaseYear
                yearLabelTextView.visibility = TextView.VISIBLE
                yearValueTextView.visibility = TextView.VISIBLE
            }

            track.primaryGenreName?.let { genre ->
                genreValueTextView.text = genre
                genreLabelTextView.visibility = TextView.VISIBLE
                genreValueTextView.visibility = TextView.VISIBLE
            }

            track.country?.let { country ->
                countryValueTextView.text = country
                countryLabelTextView.visibility = TextView.VISIBLE
                countryValueTextView.visibility = TextView.VISIBLE
            }
        }
    }

    private fun setupMediaPlayer() {
        mediaPlayer = MediaPlayer().apply {
            setOnCompletionListener {
                playbackPosition = 0
                handler.removeCallbacks(updateProgressRunnable)
                progressTextView.text = "00:00"
                this@PlayerActivity.isPlaying = false
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
        progressTextView.text = "00:00"
    }

    private fun updateProgress() {
        mediaPlayer?.let { player ->
            if (player.isPlaying) {
                val currentPosition = player.currentPosition
                val minutes = currentPosition / 60000
                val seconds = (currentPosition % 60000) / 1000
                progressTextView.text = String.format("%02d:%02d", minutes, seconds)
            }
        }
    }

    private fun updatePlayButton() {
        if (isPlaying) {
            playButton.setImageResource(R.drawable.pause_button)
            playButton.contentDescription = getString(R.string.pause)
        } else {
            playButton.setImageResource(R.drawable.play_button)
            playButton.contentDescription = getString(R.string.play)
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
                .into(artworkImageView)
        } else {
            artworkImageView.setImageResource(R.drawable.vector_placeholder)
        }
    }

    override fun onBackPressed() {
        stopPlayback()
        super.onBackPressed()
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
    }

    override fun onPause() {
        super.onPause()
        if (isPlaying) {
            pausePlayback()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(updateProgressRunnable)
        mediaPlayer?.release()
        mediaPlayer = null
    }
}