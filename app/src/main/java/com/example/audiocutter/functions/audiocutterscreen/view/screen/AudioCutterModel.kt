package com.example.audiocutter.functions.audiocutterscreen.view.screen

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


    suspend fun getAllAudioFile(): LiveData<List<AudioCutterView>> {
        return Transformations.map(AudioFileManagerImpl.findAllAudioFiles()) { listAudioFiles ->

            listAudioFiles.forEach {
                mListAudio.add(AudioCutterView(it))
            }
            mListAudio
        }
    }


    suspend fun getAllFileByType(): LiveData<List<AudioCutterView>> {
        return Transformations.map(AudioFileManagerImpl.getAllListByType()) { listAudioFilebyTypes ->
            val listAudioCutterItem = ArrayList<AudioCutterView>()
            listAudioFilebyTypes.forEach {
                listAudioCutterItem.add(AudioCutterView(it))
            }
            listAudioCutterItem
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
            if (currentAudioPlaying.absoluteFile != playerInfo.currentAudio!!.file.absoluteFile) {

                val oldPos = getAudioFilePos(currentAudioPlaying)
                val newPos = getAudioFilePos(playerInfo.currentAudio!!.file)

                if (oldPos != -1) {
                    val audioFile = mListAudio[oldPos].copy()
                    audioFile.state = PlayerState.IDLE
                    mListAudio[oldPos] = audioFile
                }
                if (newPos != -1) {
                    val audioFile = mListAudio[newPos].copy()
                    audioFile.state = playerInfo.playerState
                    mListAudio[newPos] = audioFile
                }
                currentAudioPlaying = playerInfo.currentAudio!!.file

            } else {
                val atPos = getAudioFilePos(currentAudioPlaying)
                if (atPos != -1) {
                    val audioFile = mListAudio[atPos].copy()
                    audioFile.state = playerInfo.playerState
                    mListAudio[atPos] = audioFile
                }
                currentAudioPlaying = playerInfo.currentAudio!!.file
            }
        }

        return mListAudio
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


}