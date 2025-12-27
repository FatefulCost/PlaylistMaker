package com.example.playlistmaker.feature.media

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager2.widget.ViewPager2
import com.example.playlistmaker.R
import com.example.playlistmaker.feature.media.ui.adapters.MediaLibraryPagerAdapter
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class MediaActivity : AppCompatActivity() {

    private lateinit var viewPagerAdapter: MediaLibraryPagerAdapter
    private lateinit var toolbar: MaterialToolbar
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_media)

        initViews()
        setupToolbar()
        setupViewPager()
        setupTabLayout()
    }

    private fun initViews() {
        toolbar = findViewById(R.id.toolbar_media)
        tabLayout = findViewById(R.id.tab_layout_media)
        viewPager = findViewById(R.id.view_pager_media)
    }

    private fun setupToolbar() {
        toolbar.title = getString(R.string.media_library)
        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupViewPager() {
        viewPagerAdapter = MediaLibraryPagerAdapter(this)
        viewPager.adapter = viewPagerAdapter
    }

    private fun setupTabLayout() {
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> getString(R.string.favorites_tab)
                1 -> getString(R.string.playlists_tab)
                else -> ""
            }
        }.attach()
    }
}