package com.example.playlistmaker.feature.media.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.FragmentMediaContainerBinding
import com.example.playlistmaker.feature.media.ui.adapters.MediaLibraryPagerAdapter
import com.google.android.material.tabs.TabLayoutMediator

class MediaContainerFragment : Fragment() {

    private var _binding: FragmentMediaContainerBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMediaContainerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewPager()
        setupToolbar()
    }

    private fun setupToolbar() {
        binding.toolbarMedia.title = getString(R.string.media_library)
    }

    private fun setupViewPager() {
        binding.viewPagerMedia.adapter = MediaLibraryPagerAdapter(requireActivity())

        TabLayoutMediator(binding.tabLayoutMedia, binding.viewPagerMedia) { tab, position ->
            tab.text = when (position) {
                0 -> getString(R.string.favorites_tab)
                1 -> getString(R.string.playlists_tab)
                else -> ""
            }
        }.attach()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}