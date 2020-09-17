package com.example.audiocutter.functions.audiocutterscreen

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.example.audiocutter.base.BaseViewModel
import com.example.audiocutter.core.ManagerFactory
import com.example.audiocutter.core.audioManager.AudioFileManagerImpl
import com.example.audiocutter.core.manager.PlayerInfo
import com.example.audiocutter.core.manager.PlayerState
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


    // chuyen trang thai play nhac
    fun playingAudioAndchangeStatus(position: Int): List<AudioCutterView> {

        val item = mListAudio.get(position).copy()

        item.state = PlayerState.PLAYING
        mListAudio[position] = item

        runOnBackground {
            ManagerFactory.getAudioPlayer().play(mListAudio.get(position).audioFile)
        }
        // trang thai phat nhac
        isPlayingStatus = true
        return mListAudio
    }

    fun pauseAudioAndChangeStatus(position: Int): List<AudioCutterView> {

        val item = mListAudio.get(position).copy()

        item.state = PlayerState.PAUSE
        mListAudio[position] = item

        runOnBackground {
            ManagerFactory.getAudioPlayer().pause()
        }
        // trang thai phat nhac
        isPlayingStatus = false
        return mListAudio
    }

    fun stopAudioAndChangeStatus(pos: Int): List<AudioCutterView> {

        val item = mListAudio.get(pos).copy()
        item.state = PlayerState.IDLE
        mListAudio[pos] = item

        runOnBackground {
            ManagerFactory.getAudioPlayer().stop()
        }
        // trang thai phat nhac
        isPlayingStatus = false
        return mListAudio
    }

    fun resumeAudioAndChangeStatus(position: Int): List<AudioCutterView> {


        val item = mListAudio.get(position).copy()
        item.state = PlayerState.PLAYING
        mListAudio[position] = item

        runOnBackground {
            ManagerFactory.getAudioPlayer().resume()
        }
        // trang thai phat nhac
        isPlayingStatus = true

        return mListAudio
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