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
    private var listData = ArrayList<AudioCutterView>()
    private var listIndexChooser = ArrayList<Int>()
    private var mListAudioSearch = ArrayList<AudioCutterView>()
    private var _stateLoadProgress = MutableLiveData<Int>()
    val stateLoadProgress: LiveData<Int>
        get() = _stateLoadProgress

    private var _countItem = MutableLiveData<Int>()
    val countItem: LiveData<Int>
        get() = _countItem

    var isChooseItem = false
    var count = 0


    private val sortListByName: Comparator<AudioCutterView> =
        Comparator { m1, m2 ->
            m1.audioFile.fileName.compareTo(m2.audioFile.fileName)
        }



    fun getAllAudioFile(): LiveData<List<AudioCutterView>> {
        return Transformations.map(ManagerFactory.getAudioFileManager().findAllAudioFiles()) {
            when (it.state) {
                StateLoad.LOADING -> {
                    _stateLoadProgress.postValue(1)
                }
                StateLoad.LOADDONE -> {
                    _stateLoadProgress.postValue(0)
                }
                StateLoad.LOADFAIL -> {
                    _stateLoadProgress.postValue(-1)
                }
            }
            mListAudio.clear()
            it.listAudioFiles.forEach {
                mListAudio.add(AudioCutterView(it))
            }

            Collections.sort(mListAudio, sortListByName)
            listData.addAll(mListAudio)

            for (item in listIndexChooser) {
                mListAudio.removeAt(item)
                mListAudio.add(
                    item,
                    AudioCutterView(
                        mListAudioSearch[item].audioFile,
                        PlayerState.IDLE,
                        isCheckChooseItem = true
                    )
                )
            }
            mListAudio
        }
    }

    fun getStateLoading(): LiveData<Int> {
        return stateLoadProgress
    }

    fun getCountItemChoose(): LiveData<Int> {
        return countItem
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
        mListAudio.clear()
        mListAudio.addAll(mListAudioSearch)
        return mListAudioSearch
    }


    fun getListAudio(): ArrayList<AudioCutterView> {
        return mListAudio

    }

    fun getListData(): ArrayList<AudioCutterView> {
        return listData
    }

    fun chooseItemAudioFile(pos: Int, rs: Boolean): List<AudioCutterView>? {
        val itemAudio: AudioCutterView
        var count = 0
        if (!rs) {
            itemAudio = mListAudio[pos].copy()
            itemAudio.isCheckChooseItem = true
            this.count++
            if (this.count > 2) {
                this.count = 2
            }
            _countItem.postValue(this.count)
            mListAudio[pos] = itemAudio
        } else {
            itemAudio = mListAudio[pos].copy()
            itemAudio.isCheckChooseItem = false
            if (this.count > 0) {
                this.count--
                _countItem.postValue(this.count)
            }
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
                listIndexChooser.add(mListAudio.indexOf(item))
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