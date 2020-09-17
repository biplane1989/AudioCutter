package com.example.audiocutter.functions.audiocutterscreen

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.example.audiocutter.core.ManagerFactory
import com.example.audiocutter.core.audioManager.AudioFileManagerImpl
import com.example.audiocutter.core.manager.PlayerInfo
import com.example.audiocutter.core.manager.PlayerState
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class AudioCutterModel : ViewModel() {
    private val TAG = AudioCutterModel::class.java.name

    var isPlayingStatus = false
    private var mListAudio = ArrayList<AudioCutterView>()
    val mainScope = MainScope()


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

        mainScope.launch {
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

        mainScope.launch {
            ManagerFactory.getAudioPlayer().pause()
        }
        // trang thai phat nhac
        isPlayingStatus = false
        return mListAudio
    }

    fun stopAudioAndChangeStatus(): List<AudioCutterView> {


        mainScope.launch {
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

        mainScope.launch {
            ManagerFactory.getAudioPlayer().resume()
        }

        // trang thai phat nhac
        isPlayingStatus = true

        return mListAudio
    }


    fun updatePlayerInfo(playerInfo: PlayerInfo): List<AudioCutterView> {
        var selectedPosition = -1
        var i = 0
        while (i < mListAudio.size) {
            if (mListAudio.get(i).audioFile.file.absoluteFile.equals(playerInfo.currentAudio!!.file.absoluteFile)) {
                selectedPosition = i
                break
            }
            i++
        }

        if (selectedPosition == -1) {

            //audio bi nguoi dùng xóa
            mainScope.launch {
                ManagerFactory.getAudioPlayer().stop()
            }

        } else {
            val item = mListAudio.get(selectedPosition).copy()
            // update
            item.state = PlayerState.PLAYING


            mListAudio[selectedPosition] = item
        }
        return mListAudio
    }


}