package com.example.audiocutter.functions.audiochooser.objects

import com.example.audiocutter.core.manager.PlayerState
import com.example.audiocutter.objects.AudioFile
import com.example.audiocutter.objects.StateLoad

data class AudioCutterView(
    val audioFile: AudioFile,
    var state: PlayerState = PlayerState.IDLE,
    var isCheckChooseItem: Boolean = false,
    var duration: Long = 0L,
    var currentPos: Long = 0L,
    var isCheckDistance: Boolean = false
)