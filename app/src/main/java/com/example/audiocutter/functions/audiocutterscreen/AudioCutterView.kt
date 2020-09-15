package com.example.audiocutter.functions.audiocutterscreen

import com.example.audiocutter.core.manager.PlayerState
import com.example.audiocutter.objects.AudioFile

data class AudioCutterView(
    val audioFile: AudioFile,
    var state: PlayerState = PlayerState.IDLE,
    var isPlaying: Boolean = false
)
