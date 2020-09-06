package com.example.audiocutter.core.manager

import androidx.lifecycle.LiveData
import com.example.audiocutter.objects.AudioFile

enum class PlayerState {

}

class PlayerInfo(val currentAudio: AudioFile, val position: Int, val playerState: PlayerState)


interface AudioPlayer {
    suspend fun play(audioFile: AudioFile): Boolean
    suspend fun play(audioFile: AudioFile, startPosition: Int, endPosition: Int): Boolean
    fun pause()
    fun resume()
    fun stop()
    fun seek(position: Int)
    fun setVolume(value: Int)
    fun getPlayerInfo(): LiveData<PlayerInfo>

}