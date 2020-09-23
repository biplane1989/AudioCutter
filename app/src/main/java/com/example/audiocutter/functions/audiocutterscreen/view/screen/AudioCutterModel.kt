package com.example.audiocutter.functions.audiocutterscreen.view.screen

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
    var isPlayingStatus = false
    private var mListAudio = ArrayList<AudioCutterView>()
    private var mListSearch = ArrayList<AudioCutterView>()


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
        return Transformations.map(AudioFileManagerImpl.getAllListByType()) { listAudioFilebyTypes ->
            val listAllbyType = ArrayList<AudioCutterView>()
            listAllbyType.clear()
            listAudioFilebyTypes.forEach {
                listAllbyType.add(AudioCutterView(it))
            }
            listAllbyType
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
        mListAudio.clear()
        mListSearch.addAll(mListSearch)
        return mListSearch
    }


    fun updateControllerAudio(pos: Int, state: PlayerState, rs: Boolean, stateClick: PlayerState) {
        val item = mListAudio.get(pos).copy()
        item.state = state
        mListAudio[pos] = item
        runOnBackground {
            ManagerFactory.getAudioPlayer().pause()
        }
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
        if (playerInfo.currentAudio != null) {
            if (!currentAudioPlaying.absoluteFile.equals(playerInfo.currentAudio!!.file.absoluteFile)) {

                val oldPos = getAudioFilePos(currentAudioPlaying)
                val newPos = getAudioFilePos(playerInfo.currentAudio!!.file)
                Log.d(TAG, "updateMediaInfo: old pos$oldPos   new pos $newPos")
                if (oldPos != -1) {
                    updateState(oldPos, PlayerState.IDLE)
                }
                if (newPos != -1) {
                    updateState(newPos, playerInfo.playerState)
                }


            } else {
                val atPos = getAudioFilePos(currentAudioPlaying)
                if (atPos != -1) {
                    updateState(atPos, playerInfo.playerState)
                }

                currentAudioPlaying = playerInfo.currentAudio!!.file
            }
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
        mListSearch = ArrayList()
        listTmp.forEach {
            val rs = it.audioFile.fileName.toLowerCase().contains(yourTextSearch.toLowerCase())
            if (rs) {
                mListSearch.add(it)
            }
            Log.d(TAG, "searchAudio: filename ${it.audioFile.fileName} textsearch $yourTextSearch  result $rs")
        }
        return mListSearch
    }

    fun getListsearch(): ArrayList<AudioCutterView> {
        return mListSearch
    }


}