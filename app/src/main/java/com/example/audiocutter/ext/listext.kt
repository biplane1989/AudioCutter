package com.example.audiocutter.ext

import android.util.Log
import com.example.audiocutter.functions.mystudio.objects.AudioFileView
import com.example.audiocutter.objects.AudioFile

fun List<AudioFileView>.toListAudioFiles(): List<AudioFile> {
    return this.map { it.audioFile }
}

fun List<AudioFileView>.indexOf(audioFileView: AudioFileView): Int {
    var index = 0
    for (item in this) {              // TODO
        if (item.id == audioFileView.id) {
            return index
        }
        index++
    }
    return -1
}