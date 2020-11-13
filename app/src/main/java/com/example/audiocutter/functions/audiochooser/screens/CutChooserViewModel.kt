package com.example.audiocutter.functions.audiochooser.screens

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.example.audiocutter.base.BaseViewModel
import com.example.audiocutter.core.manager.ManagerFactory
import com.example.audiocutter.core.manager.PlayerInfo
import com.example.audiocutter.core.manager.PlayerState
import com.example.audiocutter.functions.audiochooser.event.OnActionCallback
import com.example.audiocutter.functions.audiochooser.objects.AudioCutterView
import com.example.audiocutter.objects.StateLoad
import java.io.File
import java.util.*
import kotlin.Comparator
import kotlin.collections.ArrayList

class AudioCutterModel : BaseViewModel() {


    private val TAG = AudioCutterModel::class.java.name
    private var currentAudioPlaying: File = File("")

    private var mListAudioPrev = ArrayList<AudioCutterView>()
    private var mListAudios = ArrayList<AudioCutterView>()
    private var mListAudioSearch = ArrayList<AudioCutterView>()


    var duration: Long? = 0L
    var audioPlayer = ManagerFactory.getDefaultAudioPlayer()
    private lateinit var mcallBack: OnActionCallback

    private var _stateLoadProgress = MutableLiveData<Int>()
    val stateLoadProgress: LiveData<Int>
        get() = _stateLoadProgress


    fun getStateLoading(): LiveData<Int> {
        return stateLoadProgress
    }

    private val sortListByName: Comparator<AudioCutterView> =
        Comparator { m1, m2 ->
            m1.audioFile.fileName.compareTo(m2.audioFile.fileName)
        }


    fun getAllAudioFile(): LiveData<List<AudioCutterView>> {
        return Transformations.map(
            ManagerFactory.getAudioFileManager().findAllAudioFiles()
        ) { it ->
            Log.d(TAG, "getAllAudioFile: checkList stateLoading ${it.state}")
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
            mListAudioPrev.clear()
            it.listAudioFiles.forEach {
                mListAudioPrev.add(AudioCutterView(it))
            }
            Collections.sort(mListAudioPrev, sortListByName)
            mListAudios.addAll(mListAudioPrev)
            mListAudioPrev
        }
    }


    suspend fun play(pos: Int) {
        val audioItem = mListAudioPrev[pos]
        audioPlayer.play(audioItem.audioFile)
    }

    fun pause() {
        if (audioPlayer.getPlayerInfoData().playerState == PlayerState.PLAYING) {
            audioPlayer.pause()
        }

    }

    fun resume() {
        audioPlayer.resume()
    }


    fun updateMediaInfo(playerInfo: PlayerInfo): List<AudioCutterView> {
        Log.d(TAG, "updateMediaInfo: ${playerInfo.playerState}")
        if (playerInfo.currentAudio != null) {
            if (currentAudioPlaying.absoluteFile != playerInfo.currentAudio!!.file.absoluteFile) {


                val oldPos = getAudioFilePos(currentAudioPlaying)
                val newPos = getAudioFilePos(playerInfo.currentAudio!!.file)
                if (oldPos != -1) {
                    val audioFile = mListAudioPrev[oldPos].copy()
                    audioFile.state = PlayerState.IDLE
                    audioFile.isCheckDistance = false
                    audioFile.currentPos = playerInfo.posision.toLong()
                    audioFile.duration = playerInfo.duration.toLong()
                    mListAudioPrev[oldPos] = audioFile
                }
                if (newPos != -1) {
                    updateState(newPos, playerInfo, true)
                }
            } else {
                val atPos = getAudioFilePos(currentAudioPlaying)
                if (atPos != -1) {
                    updateState(atPos, playerInfo, true)
                }
            }
            currentAudioPlaying = playerInfo.currentAudio!!.file
        }

        return mListAudioPrev
    }

    private fun updateState(pos: Int, playerInfo: PlayerInfo, rs: Boolean) {
        val audioFile = mListAudioPrev[pos].copy()
        audioFile.state = playerInfo.playerState
        audioFile.isCheckDistance = rs
        audioFile.currentPos = playerInfo.posision.toLong()
        audioFile.duration = playerInfo.duration.toLong()
        mListAudioPrev[pos] = audioFile

    }


    private fun getAudioFilePos(file: File): Int {
        var i = 0
        while (i < mListAudioPrev.size) {
            if (mListAudioPrev[i].audioFile.file == file) {
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
            val rs = it.audioFile.fileName.toLowerCase(Locale.getDefault())
                .contains(yourTextSearch.toLowerCase(Locale.getDefault()))
            if (rs) {
                mListAudioSearch.add(it)
            }
        }
        mListAudioPrev.clear()
        mListAudioPrev.addAll(mListAudioSearch)
        /***bug play audio file**/
        return mListAudioPrev
    }


    fun getListAudio(): ArrayList<AudioCutterView> {
        return mListAudioPrev
    }

    fun getListData(): ArrayList<AudioCutterView> {
        return mListAudios
    }


}