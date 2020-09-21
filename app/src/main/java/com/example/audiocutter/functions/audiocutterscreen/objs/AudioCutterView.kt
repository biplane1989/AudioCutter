package com.example.audiocutter.functions.audiocutterscreen.objs

import com.example.audiocutter.core.manager.PlayerState
import com.example.audiocutter.objects.AudioFile

data class AudioCutterView(
    val audioFile: AudioFile,
    var state: PlayerState = PlayerState.IDLE

) {
    override fun equals(obj: Any?): Boolean {
        if (obj is AudioCutterView) {
            return obj.audioFile.file.absolutePath == audioFile.file.absolutePath
        }
        return super.equals(obj)
    }
}

