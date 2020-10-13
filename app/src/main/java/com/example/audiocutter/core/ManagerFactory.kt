package com.example.audiocutter.core

import android.content.Context
import com.example.audiocutter.core.audioManager.AudioFileManagerImpl
import com.example.audiocutter.core.audioplayer.AudioPlayerImpl
import com.example.audiocutter.core.manager.AudioPlayer
import com.example.audiocutter.core.manager.ContactManager
import com.example.audiocutter.core.manager.ContactManagerImpl
import com.example.audiocutter.core.manager.fake.FakeAudioCutter
import com.example.audiocutter.core.rington.RingtonManager
import com.example.audiocutter.core.rington.RingtonManagerImpl
import com.example.core.core.AudioCutter

object ManagerFactory {
    private val mAudioFileManager = AudioFileManagerImpl
    private val mAudioCutter: AudioCutter = FakeAudioCutter()
    private val mRingtons = RingtonManagerImpl

    fun init(appContext: Context) {
        AudioPlayerImpl.init(appContext)
        //AudioFileManagerImpl.init(appContext)
        ContactManagerImpl.init(appContext)
        RingtonManagerImpl.init(appContext)
    }

    fun getRingtonManager(): RingtonManagerImpl {
        return mRingtons
    }

    fun getAudioFileManager(): AudioFileManagerImpl {
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