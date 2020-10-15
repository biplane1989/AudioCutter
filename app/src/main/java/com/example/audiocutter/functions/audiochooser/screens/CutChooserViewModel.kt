package com.example.audiocutter.functions.audiochooser.screens

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.example.audiocutter.base.BaseViewModel
import com.example.audiocutter.core.ManagerFactory
import com.example.audiocutter.core.manager.PlayerInfo
import com.example.audiocutter.core.manager.PlayerState
import com.example.audiocutter.functions.audiochooser.objects.AudioCutterView
import java.io.File

class CutChooserViewModel : BaseViewModel() {
    private val TAG = CutChooserViewModel::class.java.name
    private var currentAudioPlaying: File = File("")
    private var mListAudio = ArrayList<AudioCutterView>()
    var duration: Long? = 0L
    private val sortListByName: Comparator<AudioCutterView> =
        Comparator { m1, m2 ->
            m1!!.audioFile.fileName.substring(0, 1).toUpperCase()
                .compareTo(m2!!.audioFile.fileName.substring(0, 1).toUpperCase())
        }

    fun getAllAudioFile(): LiveData<List<AudioCutterView>> {
        return Transformations.map(
            ManagerFactory.getAudioFileManager().findAllAudioFiles()
        ) {
            mListAudio.clear()
            it.listAudioFiles.forEach {
                mListAudio.add(
                    AudioCutterView(
                        it
                    )
                )
            }
            mListAudio
        }
    }


    suspend fun play(pos: Int) {
        val audioItem = mListAudio[pos]
        ManagerFactory.getAudioPlayer().play(audioItem.audioFile)
    }

    fun pause() {
        ManagerFactory.getAudioPlayer().pause()
    }

    fun resume() {
        ManagerFactory.getAudioPlayer().resume()
    }


    fun updateMediaInfo(playerInfo: PlayerInfo): List<AudioCutterView> {
        Log.d(TAG, "updateMediaInfo: ${playerInfo.playerState}")
        if (playerInfo.currentAudio != null) {
            if (currentAudioPlaying.absoluteFile != playerInfo.currentAudio!!.file.absoluteFile) {

                val oldPos = getAudioFilePos(currentAudioPlaying)
                val newPos = getAudioFilePos(playerInfo.currentAudio!!.file)
                if (oldPos != -1) {
                    val audioFile = mListAudio[oldPos].copy()
                    audioFile.state = PlayerState.IDLE
                    audioFile.isCheckDistance = false
                    audioFile.currentPos = playerInfo.posision.toLong()
                    audioFile.duration = playerInfo.duration.toLong()
                    mListAudio[oldPos] = audioFile
                }
                if (newPos != -1) {
                    updateState(newPos, playerInfo, true)
                }
            } else {
                val atPos = getAudioFilePos(currentAudioPlaying)
                if (atPos != -1) {
                    updateState(atPos, playerInfo, true)
                }
            }
            currentAudioPlaying = playerInfo.currentAudio!!.file
        }

        return mListAudio
    }

    private fun updateState(pos: Int, playerInfo: PlayerInfo, rs: Boolean) {
        val audioFile = mListAudio[pos].copy()
        audioFile.state = playerInfo.playerState
        audioFile.isCheckDistance = rs
        audioFile.currentPos = playerInfo.posision.toLong()
        audioFile.duration = playerInfo.duration.toLong()
        mListAudio[pos] = audioFile

    }


    private fun getAudioFilePos(file: File): Int {
        var i = 0
        while (i < mListAudio.size) {
            if (mListAudio[i].audioFile.file == file) {
                return i
            }
            i++
        }
        return -1
    }

    fun searchAudio(
        listTmp: MutableList<AudioCutterView>,
        yourTextSearch: String
    ): ArrayList<AudioCutterView> {
        mListAudio.clear()
        listTmp.forEach {
            val rs = it.audioFile.fileName.toLowerCase().contains(yourTextSearch.toLowerCase())
            if (rs) {
                mListAudio.add(it)
            }
        }
        return mListAudio
    }

    fun getListsearch(): ArrayList<AudioCutterView> {
        return mListAudio
    }


}