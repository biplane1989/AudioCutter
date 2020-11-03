package com.example.audiocutter.functions.audiochooser.screens


import android.util.Log
import com.example.audiocutter.base.BaseViewModel
import com.example.audiocutter.core.manager.ManagerFactory
import com.example.audiocutter.core.manager.PlayerInfo
import com.example.audiocutter.core.manager.PlayerState
import com.example.audiocutter.functions.audiochooser.objects.AudioCutterView
import java.io.File

class MergePreviewModel : BaseViewModel() {
    private val TAG = MergePreviewModel::class.java.name
    private var currentAudioPlaying: File = File("")
    private var mListAudio = ArrayList<AudioCutterView>()


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
                    Log.d(TAG, "updateMediaInfo: atPOs   ${mListAudio.get(atPos).state}")
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

    suspend fun play(pos: Int) {
        val audioItem = mListAudio[pos]
        ManagerFactory.getDefaultAudioPlayer().play(audioItem.audioFile)
    }

    fun getListAudio(): List<AudioCutterView> {
        return mListAudio
    }

    fun pause() {
        ManagerFactory.getDefaultAudioPlayer().pause()
    }

    fun resume() {
        ManagerFactory.getDefaultAudioPlayer().resume()
    }

    fun initListFileAudio(listData: List<AudioCutterView>) {
        mListAudio.clear()
        mListAudio.addAll(listData)
    }

    fun removeItemAudio(pos: Int): List<AudioCutterView> {
        val atPosDelete = mListAudio[pos]
        mListAudio.remove(atPosDelete)
        return mListAudio
    }

    fun moveItemAudio(prePos: Int, nextPos: Int): List<AudioCutterView> {
        val preItem = mListAudio[prePos].copy()
        mListAudio.remove(preItem)
        mListAudio.add(nextPos, preItem)
        return mListAudio
    }

}