package com.example.audiocutter.functions.MixAudioScreen.view

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.example.audiocutter.base.BaseViewModel
import com.example.audiocutter.core.ManagerFactory
import com.example.audiocutter.core.manager.PlayerInfo
import com.example.audiocutter.core.manager.PlayerState
import com.example.audiocutter.functions.audiocutterscreen.objs.AudioCutterView
import java.io.File

class MixModel : BaseViewModel() {
    private val TAG = MixModel::class.java.name
    private var currentAudioPlaying: File = File("")
    private var mListAudio = ArrayList<AudioCutterView>()
    var isChooseItem = false


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


    fun updateMediaInfo(playerInfo: PlayerInfo): List<AudioCutterView> {
        Log.d(TAG, "updateMediaInfo: ${playerInfo.playerState}")
        if (playerInfo.currentAudio != null) {
            if (currentAudioPlaying.absoluteFile != playerInfo.currentAudio!!.file.absoluteFile) {

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

    fun chooseItemAudioFile(pos: Int, rs: Boolean): List<AudioCutterView>? {
        val itemAudio: AudioCutterView
        var count = 0
        if (!rs) {
            itemAudio = mListAudio[pos].copy()
            itemAudio.isChecked = true
            mListAudio[pos] = itemAudio
        } else {
            itemAudio = mListAudio[pos].copy()
            itemAudio.isChecked = false
            mListAudio[pos] = itemAudio
        }

        for (item in mListAudio) {
            if (item.isChecked) {
                count++
                Log.d(TAG, "changeItemAudioFile: $count")
                if (count > 2 && itemAudio.isChecked) {
                    itemAudio.isChecked = false
                    mListAudio[pos] = itemAudio
                    isChooseItem = true
                } else if (count < 2) {
                    isChooseItem = false
                }
            }
        }
        return mListAudio
    }

    fun checkList(): Int {
        var count = 0
        for (item in mListAudio) {
            if (item.isChecked) {
                count++
            }
        }
        return count
    }

    fun getListItemChoose(): List<AudioCutterView> {
        var listAudio = mutableListOf<AudioCutterView>()
        for (item in mListAudio) {
            if (item.isChecked) {
                listAudio.add(item)
            }
        }
        return listAudio
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


}