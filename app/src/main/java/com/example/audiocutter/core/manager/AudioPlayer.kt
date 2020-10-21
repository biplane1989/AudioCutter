package com.example.audiocutter.core.manager

import android.content.Context
import androidx.lifecycle.LiveData
import com.example.audiocutter.objects.AudioFile

enum class PlayerState(value: Int) {
    IDLE(1),
    PLAYING(2),
    PAUSE(3),
    PREPARING(4)
}

class PlayerInfo(
    var currentAudio: AudioFile?,
    var posision: Int,
    var playerState: PlayerState,
    var duration: Int,
    var volume: Float
)

interface AudioPlayer {
    fun init(context: Context)
    suspend fun play(audioFile: AudioFile): Boolean
    suspend fun play(audioFile: AudioFile, currentPosition: Int): Boolean
    fun pause()
    fun resume()
    fun stop()
    fun seek(position: Int)
    fun setVolume(volume: Float)
    fun getMaxVolume(): Int
    fun getAudioIsPlaying(): Boolean
    fun getPlayerInfo(): LiveData<PlayerInfo>
    fun getPlayerInfoData(): PlayerInfo
}