package com.example.audiocutter.functions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseFragment

class ListOnlineAudioScreen : BaseFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.list_online_audio_screen, container, false)
    }
}