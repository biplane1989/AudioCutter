package com.example.audiocutter.functions.mystudio

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.example.audiocutter.functions.mystudio.audiocutter.AudioCutterFragment
import com.example.audiocutter.functions.mystudio.audiomerger.AudioMergerFragment
import com.example.audiocutter.functions.mystudio.audiomixer.AudioMixerFragment

class MyStudioAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment = when (position) {
        0 -> AudioCutterFragment.newInstance()
        1 -> AudioMergerFragment.newInstance()
        else -> AudioMixerFragment.newInstance()
    }

    override fun getPageTitle(position: Int): CharSequence = when (position) {
        0 -> "Audio Cutter"
        1 -> "Audio Merger"
        2 -> "Audio Mixer"
        else -> ""
    }

    override fun getCount(): Int = 3
}