package com.example.audiocutter.core

import android.content.Context
import com.example.audiocutter.core.manager.AudioCutter
import com.example.audiocutter.core.manager.AudioFileManager
import com.example.audiocutter.core.manager.ContactManager
import com.example.audiocutter.core.manager.fake.FakeAudioCutter
import com.example.audiocutter.core.manager.fake.FakeAudioFileManager
import com.example.audiocutter.core.manager.fake.FakeContactManager

object ManagerFactory {
    private val mAudioFileManager: AudioFileManager = FakeAudioFileManager()
    private val mAudioCutter: AudioCutter = FakeAudioCutter()
    private val mContactManager: ContactManager = FakeContactManager()
    fun init(appContext: Context) {

    }

    fun getAudioFileManager(): AudioFileManager {
        return mAudioFileManager
    }

    fun getAudioCutter(): AudioCutter {
        return mAudioCutter
    }

    fun getContactManager(): ContactManager {
        return mContactManager
    }


}