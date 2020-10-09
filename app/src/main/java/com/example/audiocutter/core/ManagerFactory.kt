package com.example.audiocutter.core

import android.content.Context
import com.example.audiocutter.core.audioManager.AudioFileManagerImpl
import com.example.audiocutter.core.audioplayer.AudioPlayerImpl
import com.example.audiocutter.core.manager.*
import com.example.audiocutter.core.manager.AudioFileManager
import com.example.audiocutter.core.manager.AudioPlayer
import com.example.audiocutter.core.manager.ContactManager
import com.example.audiocutter.core.manager.fake.FakeAudioCutter
import com.example.audiocutter.core.manager.fake.FakeAudioFileManager
import com.example.audiocutter.core.rington.RingtonManager
import com.example.audiocutter.core.rington.RingtonManagerImpl
import com.example.core.core.AudioCutter

object ManagerFactory {
    private val mAudioFileManager: AudioFileManager = FakeAudioFileManager()
    private val mAudioCutter: AudioCutter = FakeAudioCutter()
//    private val mContactManager: ContactManager = FakeContactManager()

    fun init(appContext: Context) {
        AudioPlayerImpl.init(appContext)
        //AudioFileManagerImpl.init(appContext)
        ContactManagerImpl.init(appContext)
        RingtonManagerImpl.init(appContext)
    }

    fun getAudioFileManager(): AudioFileManager {
        return mAudioFileManager
    }

    fun getAudioCutter(): AudioCutter {
        return mAudioCutter
    }

    fun getContactManager(): ContactManager {
//        return mContactManager
        return ContactManagerImpl
    }

    fun getAudioPlayer(): AudioPlayer {
        return AudioPlayerImpl
    }

    fun getRingtoneManager(): RingtonManager {
        return RingtonManagerImpl
    }

}