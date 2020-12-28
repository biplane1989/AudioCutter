package com.example.audiocutter.functions.audiochooser.screens


import android.app.Application
import com.example.audiocutter.base.BaseAndroidViewModel
import com.example.audiocutter.core.manager.AudioPlayer
import com.example.audiocutter.core.manager.ManagerFactory
import com.example.audiocutter.functions.audiochooser.objects.AudioCutterView
import java.io.File

class MergePreviewModel(application: Application) : BaseAndroidViewModel(application) {
    private val TAG = MergePreviewModel::class.java.name
    private var currentAudioPlaying: File = File("")
    private var mListAudio = ArrayList<AudioCutterView>()
    private var mlistTmp = ArrayList<AudioCutterView>()
    private val audioPlayer = ManagerFactory.newAudioPlayer()
    fun getAudioPlayer(): AudioPlayer {
        return audioPlayer
    }

    init {
        audioPlayer.init(application.applicationContext)
    }

    suspend fun play(pos: Int) {
        val audioItem = mListAudio[pos]
        audioPlayer.play(audioItem.audioFile)
    }

    fun getListAudio(): List<AudioCutterView> {
        return mListAudio
    }

    fun pause() {
        audioPlayer.pause()
    }

    fun resume() {
        audioPlayer.resume()
    }

    fun initListFileAudio(listData: List<AudioCutterView>) {
        mListAudio.clear()
        mListAudio.addAll(listData)
    }

    fun removeItemAudio(item: AudioCutterView): List<AudioCutterView> {
        mListAudio.remove(item)
        return mListAudio
    }

    fun moveItemAudio(prePos: Int, nextPos: Int): List<AudioCutterView> {
        val preItem = mListAudio[prePos].copy()
        mListAudio.remove(preItem)
        mListAudio.add(nextPos, preItem)
        return mListAudio
    }

    fun stop() {
        audioPlayer.stop()
    }

    fun getListAudioTmp(): List<AudioCutterView> {
        return mlistTmp
    }

}