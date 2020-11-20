package com.example.audiocutter.ext

import com.example.audiocutter.functions.mystudio.objects.AudioFileView
import com.example.audiocutter.objects.AudioFile

fun List<AudioFileView>.toListAudioFiles(): List<AudioFile> {
    return this.map { it.audioFile }
}