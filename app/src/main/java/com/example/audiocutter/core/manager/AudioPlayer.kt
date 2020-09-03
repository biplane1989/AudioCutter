package com.example.audiocutter.core.manager

import com.example.audiocutter.objects.AudioFile

interface PositionChangedListener {
    fun onPositionChanged(currentAudio: AudioFile, position: Int)
}


interface AudioPlayer {
    fun play(audioFile: AudioFile): Boolean
    fun pause()
    fun resume()
    fun stop()
    fun seek(position: Int)
    fun setVolume(value: Int)
    fun addPositionChangedListener(listener: PositionChangedListener)
    fun removePositionChangedListener(listener: PositionChangedListener)

}