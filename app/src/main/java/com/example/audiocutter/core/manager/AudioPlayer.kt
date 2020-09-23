package com.example.audiocutter.core.manager

import androidx.lifecycle.LiveData
import com.example.audiocutter.objects.AudioFile

enum class PlayerState(value: Int) {
    IDLE(1),
    PLAYING(2),
    PAUSE(3)
}

class PlayerInfo(
    var currentAudio: AudioFile?,
    var position: Int,
    var playerState: PlayerState,
    var duration: Int,
    var volume: Int
)

interface AudioPlayer {
    suspend fun play(audioFile: AudioFile): Boolean
    suspend fun play(audioFile: AudioFile, currentPosition: Int)
    suspend fun play(audioFile: AudioFile, startPosition: Int, endPosition: Int): Boolean
    fun pause()
    fun resume()
    fun stop()
    fun seek(position: Int)
    fun setVolume(value: Int)
    fun getPlayerInfo(): LiveData<PlayerInfo>

}