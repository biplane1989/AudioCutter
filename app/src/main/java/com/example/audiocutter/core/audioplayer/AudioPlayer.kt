package com.example.audiocutter.core.audioplayer

import android.content.Context
import androidx.lifecycle.LiveData
import com.example.audiocutter.core.manager.AudioPlayer
import com.example.audiocutter.core.manager.PositionInfo
import com.example.audiocutter.objects.AudioFile

object AudioPlayerImpl : AudioPlayer {
    private lateinit var appContext: Context

    fun init(appContext: Context){
        this.appContext = appContext
    }

    override fun play(audioFile: AudioFile): Boolean {
        TODO("Not yet implemented")
    }

    override fun pause() {
        TODO("Not yet implemented")
    }

    override fun resume() {
        TODO("Not yet implemented")
    }

    override fun stop() {
        TODO("Not yet implemented")
    }

    override fun seek(position: Int) {
        TODO("Not yet implemented")
    }

    override fun setVolume(value: Int) {
        TODO("Not yet implemented")
    }


    override fun getPosition(): LiveData<PositionInfo> {
        TODO("Not yet implemented")
    }
}