package com.example.audiocutter.functions.mystudio

import com.example.audiocutter.core.manager.PlayerState
import com.example.audiocutter.objects.AudioFile

data class AudioFileView(
    var audioFile: AudioFile,
    var isExpanded: Boolean,
    var playerState: PlayerState = PlayerState.IDLE
) {
}