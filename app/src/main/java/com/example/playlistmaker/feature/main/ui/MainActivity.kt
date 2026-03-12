package com.example.playlistmaker.feature.main.ui

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNavigation()
    }

    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        // Настройка BottomNavigationView
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        // Создаем AppBarConfiguration
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.mediaFragment,
                R.id.searchFragment,
                R.id.settingsFragment
            )
        )

        bottomNavigationView.setupWithNavController(navController)

        // Добавляем слушатель для отслеживания текущего экрана
        navController.addOnDestinationChangedListener { _, destination, _ ->
            // Проверяем, открыт ли PlayerFragment или CreatePlaylistFragment
            if (destination.id == R.id.playerFragment || destination.id == R.id.createPlaylistFragment) {
                // Скрываем BottomNavigationView
                bottomNavigationView.visibility = View.GONE
            } else {
                // Показываем BottomNavigationView для других экранов
                bottomNavigationView.visibility = View.VISIBLE
            }
        }
    }
}