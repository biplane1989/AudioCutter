package com.example.audiocutter.functions.mystudio.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.example.audiocutter.functions.mystudio.Constance
import com.example.audiocutter.functions.mystudio.screens.MyStudioScreen

class MyStudioViewPagerAdapter(

    fm: FragmentManager
) : FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getItem(position: Int): Fragment = when (position) {
        0 -> {
            MyStudioScreen.newInstance(Constance.AUDIO_CUTTER)
        }
        1 -> {
            MyStudioScreen.newInstance(Constance.AUDIO_MERGER)
        }
        else -> {
            MyStudioScreen.newInstance(Constance.AUDIO_MIXER)
        }
    }

    override fun getPageTitle(position: Int): CharSequence = when (position) {
        0 -> Constance.AUDIO_CUTTER_STRING
        1 -> Constance.AUDIO_MERGER_STRING
        2 -> Constance.AUDIO_MIXER_STRING
        else -> ""
    }

    override fun getCount(): Int = 3
}