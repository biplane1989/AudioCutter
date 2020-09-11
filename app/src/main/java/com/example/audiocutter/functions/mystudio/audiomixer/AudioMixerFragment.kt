package com.example.audiocutter.functions.mystudio.audiomixer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseFragment

class AudioMixerFragment : BaseFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_audio_mixer, container, false)
    }

    companion object {
        fun newInstance(): AudioMixerFragment = AudioMixerFragment()
    }
}