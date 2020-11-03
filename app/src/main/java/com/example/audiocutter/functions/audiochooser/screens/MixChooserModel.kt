package com.example.audiocutter.functions.audiochooser.screens

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.example.audiocutter.base.BaseViewModel
import com.example.audiocutter.core.manager.ManagerFactory
import com.example.audiocutter.core.manager.PlayerInfo
import com.example.audiocutter.core.manager.PlayerState
import com.example.audiocutter.functions.audiochooser.objects.AudioCutterView
import com.example.audiocutter.objects.StateLoad
import java.io.File
import java.util.*
import kotlin.Comparator
import kotlin.collections.ArrayList

class MixModel : BaseViewModel() {
    private val TAG = MixModel::class.java.name
    private var currentAudioPlaying: File = File("")
    private var mListAudio = ArrayList<AudioCutterView>()
    private var mListAudioSearch = ArrayList<AudioCutterView>()
    private var _stateLoadProgress = MutableLiveData<Boolean>()
    val stateLoadProgress: LiveData<Boolean>
        get() = _stateLoadProgress

    var isChooseItem = false


    private val sortListByName: Comparator<AudioCutterView> =
        Comparator { m1, m2 ->
            m1!!.audioFile.fileName.substring(0, 1).toUpperCase()
                .compareTo(m2!!.audioFile.fileName.substring(0, 1).toUpperCase())
        }



    fun getAllAudioFile(): LiveData<List<AudioCutterView>> {
        return Transformations.map(ManagerFactory.getAudioFileManager().findAllAudioFiles()) {
            if (it.state == StateLoad.LOADING) {
                _stateLoadProgress.postValue(true)
            } else {
                _stateLoadProgress.postValue(false)
            }
            mListAudio.clear()
            it.listAudioFiles.forEach {
                mListAudio.add(AudioCutterView(it))
            }

            Collections.sort(mListAudio, sortListByName)
            mListAudio
        }
    }

    fun getStateLoading(): LiveData<Boolean> {
        return stateLoadProgress
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

    fun searchAudio(
        listTmp: MutableList<AudioCutterView>,
        yourTextSearch: String
    ): ArrayList<AudioCutterView> {
        mListAudioSearch.clear()
        listTmp.forEach {
            val rs = it.audioFile.fileName.toLowerCase().contains(yourTextSearch.toLowerCase())
            if (rs) {
                mListAudioSearch.add(it)
            }
        }
        return mListAudioSearch
    }

    fun getListsearch(): ArrayList<AudioCutterView> {
        return mListAudioSearch
    }

    fun getListAudio(): ArrayList<AudioCutterView> {
        return mListAudio
    }

    fun chooseItemAudioFile(pos: Int, rs: Boolean): List<AudioCutterView>? {
        val itemAudio: AudioCutterView
        var count = 0
        if (!rs) {
            itemAudio = mListAudio[pos].copy()
            itemAudio.isCheckChooseItem = true
            mListAudio[pos] = itemAudio
        } else {
            itemAudio = mListAudio[pos].copy()
            itemAudio.isCheckChooseItem = false
            mListAudio[pos] = itemAudio
        }

        for (item in mListAudio) {
            if (item.isCheckChooseItem) {
                count++
                Log.d(TAG, "changeItemAudioFile: $count")
                if (count > 2 && itemAudio.isCheckChooseItem) {
                    itemAudio.isCheckChooseItem = false
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
            if (item.isCheckChooseItem) {
                count++
            }
        }
        return count
    }

    fun getListItemChoose(): List<AudioCutterView> {
        var listAudio = mutableListOf<AudioCutterView>()
        for (item in mListAudio) {
            if (item.isCheckChooseItem) {
                listAudio.add(item)
            }
        }
        return listAudio
    }

    suspend fun play(pos: Int) {
        val audioItem = mListAudio[pos]
        ManagerFactory.getDefaultAudioPlayer().play(audioItem.audioFile)

    }

    fun pause() {
        ManagerFactory.getDefaultAudioPlayer().pause()
    }

    fun resume() {
        ManagerFactory.getDefaultAudioPlayer().resume()
    }


}