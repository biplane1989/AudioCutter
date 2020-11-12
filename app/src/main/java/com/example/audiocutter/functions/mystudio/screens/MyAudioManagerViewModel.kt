package com.example.audiocutter.functions.mystudio.screens

import android.util.Log
import com.example.audiocutter.base.BaseViewModel

class MyAudioManagerViewModel : BaseViewModel() {
    override fun onReceivedAction(fragmentMeta: FragmentMeta) {
        super.onReceivedAction(fragmentMeta)
        Log.d("ababa", "MyAudioManagerViewModel onReceivedAction ${fragmentMeta.action}")
    }
}