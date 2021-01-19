package com.example.audiocutter.functions.audiochooser.screens


import android.app.Application
import com.example.audiocutter.base.BaseAndroidViewModel
import com.example.audiocutter.core.manager.AudioPlayer
import com.example.audiocutter.core.manager.ManagerFactory
import com.example.audiocutter.functions.audiochooser.objects.AudioCutterViewItem

class MergePreviewModel(application: Application) : BaseAndroidViewModel(application) {
    private val TAG = MergePreviewModel::class.java.name
    private var mListAudio = ArrayList<AudioCutterViewItem>()
    private var mlistPath = ArrayList<String>()
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

    fun getListAudio(): List<AudioCutterViewItem> {
        return mListAudio
    }

    fun pause() {
        audioPlayer.pause()
    }

    fun resume() {
        audioPlayer.resume()
    }

    fun initListFileAudio(listData: List<AudioCutterViewItem>) {
        mListAudio.clear()
        mListAudio.addAll(listData)
        mlistPath.clear()
        mListAudio.forEach {
            mlistPath.add(it.audioFile.getFilePath())
        }
    }

    fun removeItemAudio(item: AudioCutterViewItem): List<AudioCutterViewItem> {
        mListAudio.remove(item)
        return mListAudio
    }

    fun moveItemAudio(prePos: Int, nextPos: Int): List<AudioCutterViewItem> {
        val preItem = mListAudio[prePos].copy()
        mListAudio.remove(preItem)
        mListAudio.add(nextPos, preItem)
        mlistPath.clear()
        mListAudio.forEach {
            mlistPath.add(it.audioFile.getFilePath())
        }
        return mListAudio
    }

    fun stop() {
        audioPlayer.stop()
    }

    fun getListPath(): List<String> {
        return mlistPath
    }


}