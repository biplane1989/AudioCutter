package com.example.audiocutter.core.manager

import android.content.Context
import com.example.audiocutter.core.audioManager.AudioFileManagerImpl
import com.example.audiocutter.core.audioplayer.AudioPlayerImpl
import com.example.audiocutter.core.contact.ContactManagerImpl
import com.example.audiocutter.core.manager.fake.FakeAudioFileManager
import com.example.audiocutter.core.result.AudioEditorManagerlmpl
import com.example.audiocutter.core.rington.RingtonManagerImpl
import com.example.core.core.AudioCutter
import com.example.core.core.AudioCutterImpl

object ManagerFactory {
    private val mAudioFileManager = AudioFileManagerImpl
    private val mAudioCutter: AudioCutter = AudioCutterImpl()
    private val mRingtons = RingtonManagerImpl
    private val defaultAudioPlayer = AudioPlayerImpl()
    private val audioPlayer = AudioPlayerImpl()


    fun init(appContext: Context) {
        defaultAudioPlayer.init(appContext)
        audioPlayer.init(appContext)
        AudioFileManagerImpl.init(appContext)
        ContactManagerImpl.init(appContext)
        RingtonManagerImpl.init(appContext)
//        AudioPlayerImpl().init(appContext)
        AudioEditorManagerlmpl.init(appContext)
        //FakeAudioFileManager.init(appContext)
        mAudioFileManager.init(appContext)
    }

    fun getRingtonManager(): RingtonManagerImpl {
        return mRingtons
    }

    fun getAudioFileManager(): AudioFileManager {
//        return mAudioFileManager
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
        return defaultAudioPlayer
    }

    fun newAudioPlayer(): AudioPlayer {
        return audioPlayer
//        return AudioPlayerImpl()
    }

    fun getRingtoneManager(): RingtonManager {
        return RingtonManagerImpl
    }

    fun getAudioEditorManager(): AudioEditorManager {
        return AudioEditorManagerlmpl
    }


}