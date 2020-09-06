package com.example.audiocutter.functions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseFragment

class AudioCutterScreen : BaseFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.audio_cutter_screen, container, false)
    }

}