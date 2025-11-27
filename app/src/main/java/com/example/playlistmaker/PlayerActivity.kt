package com.example.playlistmaker

import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_player)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.player_screen)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()
        setupBackButton()
        displayTrackInfo()
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

        // Инициализация элементов информации
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

    private fun displayTrackInfo() {
        val track = intent.getSerializableExtra("track") as? Track
        track?.let {
            // Загружаем обложку высокого качества
            loadArtwork(it.getHighResArtworkUrl())

            // Основная информация
            trackNameTextView.text = it.trackName
            artistNameTextView.text = it.artistName

            // Длительность трека (дублируется в двух местах)
            val duration = it.getFormattedTime()
            trackDurationTextView.text = duration
            durationValueTextView.text = duration

            // Дополнительная информация (показываем только если есть)
            it.collectionName?.let { collectionName ->
                albumValueTextView.text = collectionName
                albumLabelTextView.visibility = TextView.VISIBLE
                albumValueTextView.visibility = TextView.VISIBLE
            }

            it.getReleaseYear()?.let { releaseYear ->
                yearValueTextView.text = releaseYear
                yearLabelTextView.visibility = TextView.VISIBLE
                yearValueTextView.visibility = TextView.VISIBLE
            }

            it.primaryGenreName?.let { genre ->
                genreValueTextView.text = genre
                genreLabelTextView.visibility = TextView.VISIBLE
                genreValueTextView.visibility = TextView.VISIBLE
            }

            it.country?.let { country ->
                countryValueTextView.text = country
                countryLabelTextView.visibility = TextView.VISIBLE
                countryValueTextView.visibility = TextView.VISIBLE
            }
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
}