package com.example.audiocutter.functions.audiocutterscreen.view.screen

import android.os.Build
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.example.audiocutter.base.BaseViewModel
import com.example.audiocutter.core.ManagerFactory
import com.example.audiocutter.core.audioManager.AudioFileManagerImpl
import com.example.audiocutter.core.manager.PlayerInfo
import com.example.audiocutter.core.manager.PlayerState
import com.example.audiocutter.functions.audiocutterscreen.objs.AudioCutterView
import java.io.File

class AudioCutterModel : BaseViewModel() {
    private val TAG = AudioCutterModel::class.java.name
    private var currentAudioPlaying: File = File("")
    private var mListAudio = ArrayList<AudioCutterView>()


    suspend fun getAllAudioFile(): LiveData<List<AudioCutterView>> {
        return Transformations.map(ManagerFactory.getAudioFileManagerImpl().findAllAudioFiles()) { listAudioFiles ->
            mListAudio.clear()
            listAudioFiles.forEach {
                mListAudio.add(AudioCutterView(it))
            }
            mListAudio
        }
    }



    suspend fun getAllFileByType(): LiveData<List<AudioCutterView>> {
        return Transformations.map(ManagerFactory.getAudioFileManagerImpl().getAllListByType()) { listAudioFiles ->
            mListAudio.clear()
            listAudioFiles.forEach {
                mListAudio.add(AudioCutterView(it))
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
            if (!currentAudioPlaying.absoluteFile.equals(playerInfo.currentAudio!!.file.absoluteFile)) {

                val oldPos = getAudioFilePos(currentAudioPlaying)
                val newPos = getAudioFilePos(playerInfo.currentAudio!!.file)
                if (oldPos != -1) {
                    updateState(oldPos, PlayerState.IDLE)
                }
                if (newPos != -1) {
                    updateState(newPos, playerInfo.playerState)
                }
            } else {
                val atPos = getAudioFilePos(currentAudioPlaying)
                if (atPos != -1) {
                    Log.d(TAG, "updateMediaInfo: atPOs   ${mListAudio.get(atPos).state}")
                    updateState(atPos, playerInfo.playerState)
                }
            }
            currentAudioPlaying = playerInfo.currentAudio!!.file
        }

        return mListAudio
    }



    private fun updateState(pos: Int, state: PlayerState) {
        val audioFile = mListAudio[pos].copy()
        audioFile.state = state
        mListAudio[pos] = audioFile
    }

    private fun getAudioFilePos(file: File): Int {
        var i = 0
        while (i < mListAudio.size) {
            if (mListAudio.get(i).audioFile.file.equals(file)) {
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