package com.example.audiocutter.functions.audiochooser.objects

import com.example.audiocutter.core.manager.PlayerState
import com.example.audiocutter.objects.AudioFile

data class AudioCutterViewItem(
    val audioFile: AudioFile,
    var state: PlayerState = PlayerState.IDLE,
    var isCheckChooseItem: Boolean = false,
    var duration: Long = 0L, var currentPos: Long = 0L,
    var isCheckDistance: Boolean? = null,
    var isplaying: Boolean = false, var no: Int = -1
) {
    fun swapNo(audioCutterView: AudioCutterViewItem?) {
        audioCutterView?.let {
            val tmp = no
            no = it.no
            it.no = tmp
        }

    }
}
