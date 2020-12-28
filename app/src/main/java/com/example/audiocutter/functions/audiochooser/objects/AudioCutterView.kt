package com.example.audiocutter.functions.audiochooser.objects

import android.os.Parcel
import android.os.Parcelable
import com.example.audiocutter.core.manager.PlayerState
import com.example.audiocutter.objects.AudioFile

data class AudioCutterView(
    val audioFile: AudioFile,
    var state: PlayerState = PlayerState.IDLE,
    var isCheckChooseItem: Boolean = false,
    var duration: Long = 0L, var currentPos: Long = 0L,
    var isCheckDistance: Boolean? = null,
    var isplaying: Boolean = false, var no: Int = -1
)
