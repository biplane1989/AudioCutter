package com.example.audiocutter.functions.ChooseScreen.view

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.example.audiocutter.base.BaseViewModel
import com.example.audiocutter.core.ManagerFactory
import com.example.audiocutter.core.audioManager.AudioFileManagerImpl
import com.example.audiocutter.core.manager.PlayerInfo
import com.example.audiocutter.core.manager.PlayerState
import com.example.audiocutter.functions.audiocutterscreen.objs.AudioCutterView
import com.example.audiocutter.functions.audiocutterscreen.view.screen.AudioCutterModel
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

class RecentModel :BaseViewModel(){
    private val TAG = AudioCutterModel::class.java.name
    private var currentAudioPlaying: File = File("")
    var isPlayingStatus = false
    private var mListAudio = ArrayList<AudioCutterView>()
    var isCheckItem = false


    suspend fun getAllAudioFile(): LiveData<List<AudioCutterView>> {
        return Transformations.map(AudioFileManagerImpl.findAllAudioFiles()) { listAudioFiles ->
            mListAudio.clear()
            listAudioFiles.forEach {
                mListAudio.add(AudioCutterView(it))
            }
            mListAudio
        }
    }



    suspend fun getAllFileByType(): LiveData<List<AudioCutterView>> {
        return Transformations.map(AudioFileManagerImpl.getAllListByType()) { listAudioFiles ->
            mListAudio.clear()
            listAudioFiles.forEach {
                mListAudio.add(AudioCutterView(it))
            }
            mListAudio
        }
    }


    fun controllerAudio(position: Int, state: PlayerState): List<AudioCutterView> {
        when (state) {
            PlayerState.IDLE -> {
                updateControllerAudio(position, PlayerState.PLAYING, true, PlayerState.IDLE)
            }
            PlayerState.PAUSE -> {
                updateControllerAudio(position, PlayerState.PLAYING, true, PlayerState.PAUSE)
            }
            PlayerState.PLAYING -> {
                updateControllerAudio(position, PlayerState.PAUSE, false, PlayerState.PLAYING)
            }
        }

        return mListAudio
    }


    fun updateControllerAudio(pos: Int, state: PlayerState, rs: Boolean, stateClick: PlayerState) {
        val item = mListAudio.get(pos).copy()
        item.state = state
        mListAudio[pos] = item

        runOnBackground {
            when (stateClick) {
                PlayerState.IDLE -> ManagerFactory.getAudioPlayer().play(item.audioFile)
                PlayerState.PLAYING -> ManagerFactory.getAudioPlayer().pause()
                PlayerState.PAUSE -> ManagerFactory.getAudioPlayer().resume()
            }
        }
        isPlayingStatus = rs
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

    fun changeItemAudioFile(pos: Int, rs: Boolean): List<AudioCutterView>? {
        var itemAudio: AudioCutterView
        var count = 0
        if (!rs) {
            itemAudio = mListAudio.get(pos).copy()
            itemAudio.isChecked = true
            mListAudio[pos] = itemAudio
        } else {
            itemAudio = mListAudio.get(pos).copy()
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
                    isCheckItem = true
                } else if (count < 2) {
                    isCheckItem = false
                }
            }
        }
        return mListAudio
    }

    fun checkList(): Boolean {
        var count = 0
        for (item in mListAudio) {
            if (item.isChecked) {
                count++
            }
        }
        if (count > 2 || count < 2) {
            return false
        } else if (count == 2) {
            return true
        }
        return false
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


}