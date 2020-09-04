package com.example.audiocutter.core.manager

import androidx.lifecycle.LiveData
import com.example.audiocutter.objects.AudioFile

class PositionInfo(val currentAudio: AudioFile, val position: Int)


interface AudioPlayer {
    fun play(audioFile: AudioFile): Boolean
    fun pause()
    fun resume()
    fun stop()
    fun seek(position: Int)
    fun setVolume(value: Int)
    fun getPosition() : LiveData<PositionInfo>

}