package com.example.audiocutter.core.manager

import android.content.Context
import com.example.audiocutter.core.audiomanager.AudioFileManagerImpl
import com.example.audiocutter.core.audioplayer.AudioPlayerImpl
import com.example.audiocutter.core.contact.ContactManagerImpl
import com.example.audiocutter.core.flashcall.FlashCallSettingImpl
import com.example.audiocutter.core.result.AudioEditorManagerlmpl
import com.example.audiocutter.core.rington.RingtonManagerImpl
import com.example.audiocutter.database.DatabaseHelper
import com.example.core.core.AudioCutter
import com.example.core.core.AudioCutterImpl

object ManagerFactory {
    private val mAudioFileManager = AudioFileManagerImpl
    private val mAudioCutter: AudioCutter = AudioCutterImpl()
    private val mRingtons = RingtonManagerImpl
    private val defaultAudioPlayer = AudioPlayerImpl()
    private lateinit var flashCallSetting:FlashCallSetting
    private lateinit var appContext: Context

//    private val audioPlayer = AudioPlayerImpl()


    fun init(appContext: Context) {
        this.appContext = appContext.applicationContext
        DatabaseHelper.create(this.appContext)
        defaultAudioPlayer.init(appContext)
//        audioPlayer.init(appContext)
        AudioFileManagerImpl.init(appContext)
        RingtonManagerImpl.init(appContext)
//        AudioPlayerImpl().init(appContext)
        AudioEditorManagerlmpl.init(appContext)

        //FakeAudioFileManager.init(appContext)
        mAudioFileManager.init(appContext)
        flashCallSetting = FlashCallSettingImpl()
    }

    fun getAppContext(): Context {
        return this.appContext
    }

    fun getFlashCallSetting(): FlashCallSetting {
        return flashCallSetting
    }

    fun getRingtonManager(): RingtonManagerImpl {
        return mRingtons
    }

    fun getAudioFileManager(): AudioFileManager {
//        return FakeAudioFileManager
        return mAudioFileManager
    }

    fun getAudioCutter(): AudioCutter {
        return mAudioCutter
    }

    fun createNewContactManager(appContext: Context): ContactManager {
//        return mContactManager
        return ContactManagerImpl(appContext)
    }

    fun getDefaultAudioPlayer(): AudioPlayer {
        return defaultAudioPlayer
    }

    fun newAudioPlayer(): AudioPlayer {
//        return audioPlayer
        return AudioPlayerImpl()
    }

    fun getRingtoneManager(): RingtonManager {
        return RingtonManagerImpl
    }

    fun getAudioEditorManager(): AudioEditorManager {
        return AudioEditorManagerlmpl
    }


}