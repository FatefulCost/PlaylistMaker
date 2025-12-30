package com.example.playlistmaker.feature.main.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.playlistmaker.R
import com.example.playlistmaker.feature.media.MediaActivity
import com.example.playlistmaker.feature.search.ui.SearchActivity
import com.example.playlistmaker.feature.settings.ui.SettingsActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val settingsButton = findViewById<Button>(R.id.button3)
        val mediaButton = findViewById<Button>(R.id.button2)
        val searchButton = findViewById<Button>(R.id.button1)

        settingsButton.setOnClickListener {
            val displayIntent = Intent(this, SettingsActivity::class.java)
            startActivity(displayIntent)
        }

        mediaButton.setOnClickListener {
             val displayIntent = Intent(this, MediaActivity::class.java)
             startActivity(displayIntent)
        }

        searchButton.setOnClickListener {
            val displayIntent = Intent(this, SearchActivity::class.java)
            startActivity(displayIntent)
        }
    }
}