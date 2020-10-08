package com.example.audiocutter.core

import android.content.Context
import com.example.audiocutter.core.audioManager.AudioFileManagerImpl
import com.example.audiocutter.core.audioplayer.AudioPlayerImpl
import com.example.audiocutter.core.manager.AudioCutter
import com.example.audiocutter.core.manager.AudioPlayer
import com.example.audiocutter.core.manager.ContactManager
import com.example.audiocutter.core.manager.fake.FakeAudioCutter
import com.example.audiocutter.core.manager.fake.FakeContactManager

object ManagerFactory {
    private val mAudioFileManager = AudioFileManagerImpl
    private val mAudioCutter: AudioCutter = FakeAudioCutter()
    private val mContactManager: ContactManager = FakeContactManager()

    fun init(appContext: Context) {
        AudioPlayerImpl.init(appContext)
        AudioFileManagerImpl.init(appContext)
    }

    fun getAudioFileManager(): AudioFileManagerImpl {
        return mAudioFileManager
    }

    fun getAudioCutter(): AudioCutter {
        return mAudioCutter
    }

    fun getContactManager(): ContactManager {
        return mContactManager
    }

    fun getAudioPlayer(): AudioPlayer {
        return AudioPlayerImpl
    }

}