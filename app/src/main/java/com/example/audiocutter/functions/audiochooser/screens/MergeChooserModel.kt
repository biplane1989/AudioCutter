package com.example.audiocutter.functions.audiochooser.screens

import android.util.Log
import androidx.lifecycle.*
import com.example.audiocutter.base.BaseViewModel
import com.example.audiocutter.core.manager.ManagerFactory
import com.example.audiocutter.core.manager.PlayerInfo
import com.example.audiocutter.core.manager.PlayerState
import com.example.audiocutter.functions.audiochooser.objects.AudioCutterView
import com.example.audiocutter.functions.mystudio.screens.FragmentMeta
import com.example.audiocutter.objects.StateLoad
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

class MergeChooserModel : BaseViewModel() {
    private val audioPlayer = ManagerFactory.getDefaultAudioPlayer()
    private val TAG = MergeChooserModel::class.java.name
    private var currentAudioPlaying: File = File("")

    private var _stateLoadProgress = MutableLiveData<Int>()
    val stateLoadProgress: LiveData<Int>
        get() = _stateLoadProgress


    private var filterText = ""

    private val _listAudioFiles = MediatorLiveData<List<AudioCutterView>>()
    init {

        _listAudioFiles.addSource(ManagerFactory.getAudioFileManager().findAllAudioFiles()) {
            var listAudioFiles:List<AudioCutterView>?=null

            when (it.state) {
                StateLoad.LOADING -> {
                    _stateLoadProgress.postValue(1)
                }
                StateLoad.LOADDONE -> {
                    _stateLoadProgress.postValue(0)
                    val tmpList = ArrayList<AudioCutterView>()
                    it.listAudioFiles.forEach {
                        tmpList.add(AudioCutterView(it))
                    }
                    listAudioFiles = tmpList
                }
                StateLoad.LOADFAIL -> {
                    _stateLoadProgress.postValue(-1)
                }
            }


            _listAudioFiles.postValue(listAudioFiles)

        }
    }
    private val _listFilteredAudioFiles = liveData<List<AudioCutterView>?> {
        emitSource(_listAudioFiles.map {
            var listResult:List<AudioCutterView>?=null
            if(it != null){
                listResult = ArrayList(it)
                if (filterText.isNotEmpty()) {
                    it.forEach { item ->
                        val rs = item.audioFile.fileName.toLowerCase(Locale.getDefault()).contains(
                            filterText.toLowerCase(Locale.getDefault())
                        )
                        if (rs) {
                            listResult.add(item)
                        }
                    }
                }
            }


            listResult
        })
    }

    fun getStateLoading(): LiveData<Int> {
        return stateLoadProgress
    }


    fun getAllAudioFile(): LiveData<List<AudioCutterView>?> {
        return _listFilteredAudioFiles
    }


    private fun getListFilteredAudio(): ArrayList<AudioCutterView> {
//        Log.d(TAG, "getListPreviewAudio: ${_listAudioFiles.value!!.size}")
        return ArrayList(_listFilteredAudioFiles.value ?: ArrayList())
    }

    private fun getListAllAudio(): ArrayList<AudioCutterView> {
        return ArrayList(_listAudioFiles.value ?: ArrayList())
    }

    fun updateMediaInfo(playerInfo: PlayerInfo) {
        Log.d(TAG, "updateMediaInfo: ${playerInfo.playerState}")
        val listAudioViews = getListAllAudio()
//        val listAudioViews = getListFilteredAudio()

        if (playerInfo.currentAudio != null) {
            if (currentAudioPlaying.absoluteFile != playerInfo.currentAudio!!.file.absoluteFile) {

                val oldPos = getAudioFilePos(currentAudioPlaying)
                val newPos = getAudioFilePos(playerInfo.currentAudio!!.file)
                if (oldPos != -1) {
                    val audioFile = listAudioViews[oldPos].copy()
                    audioFile.state = PlayerState.IDLE
                    audioFile.isCheckDistance = false
                    audioFile.currentPos = playerInfo.posision.toLong()
                    audioFile.duration = playerInfo.duration.toLong()
                    listAudioViews[oldPos] = audioFile
                }
                if (newPos != -1) {
                    val audioCutterView = listAudioViews[newPos].copy()
                    updateState(audioCutterView, playerInfo, true)
                    listAudioViews[newPos] = audioCutterView
                }
            } else {
                val atPos = getAudioFilePos(currentAudioPlaying)
                if (atPos != -1) {
                    Log.d(TAG, "updateMediaInfo: atPOs   ${listAudioViews.get(atPos).state}")
                    val audioCutterView = listAudioViews[atPos].copy()
                    updateState(audioCutterView, playerInfo, true)
                    listAudioViews[atPos] = audioCutterView
                }
            }
            currentAudioPlaying = playerInfo.currentAudio!!.file
        }
        _listAudioFiles.postValue(listAudioViews)
    }


    private fun updateState(audioCutterView: AudioCutterView, playerInfo: PlayerInfo, rs: Boolean) {
        audioCutterView.state = playerInfo.playerState
        audioCutterView.isCheckDistance = rs
        audioCutterView.currentPos = playerInfo.posision.toLong()
        audioCutterView.duration = playerInfo.duration.toLong()

    }


    private fun getAudioFilePos(file: File): Int {
        val listAudioViews = getListAllAudio()
        var i = 0
        while (i < listAudioViews.size) {
            if (listAudioViews[i].audioFile.file == file) {
                return i
            }
            i++
        }
        return -1
    }

    fun searchAudio(yourTextSearch: String) {
        filterText = yourTextSearch
        _listAudioFiles.postValue(_listAudioFiles.value)

    }

    fun chooseItemAudioFile(audioCutterView: AudioCutterView, rs: Boolean) {
        try {
            val mListAudios = getListAllAudio()
            val pos = mListAudios.indexOf(audioCutterView)
            val itemAudio: AudioCutterView = mListAudios[pos].copy()
            if (!rs) {
                itemAudio.isCheckChooseItem = true
                mListAudios[pos] = itemAudio
            } else {
                itemAudio.isCheckChooseItem = false
                mListAudios[pos] = itemAudio
            }
            _listAudioFiles.postValue(mListAudios)
        } catch (e: ArrayIndexOutOfBoundsException) {
            e.printStackTrace()
        }
    }


    fun getListItemChoose(): List<AudioCutterView> {
        val mListAudios = getListAllAudio()
        var listAudio = mutableListOf<AudioCutterView>()
        for (item in mListAudios) {
            if (item.isCheckChooseItem) {
                listAudio.add(item)
            }
        }
        return listAudio
    }

    suspend fun play(pos: Int) {
        val listAudios = getListFilteredAudio()
        val audioItem = listAudios[pos]
        audioPlayer.play(audioItem.audioFile)

    }

    fun pause() {
        audioPlayer.pause()
    }

    fun stop() {
        audioPlayer.stop()
    }

    fun resume() {
        audioPlayer.resume()
    }


    override fun onReceivedAction(fragmentMeta: FragmentMeta) {
        Log.d("TAG", "onReceivedAction: receive data")
        if (fragmentMeta.action.equals("ACTION_DELETE")) {
            val audio = fragmentMeta.data as AudioCutterView
            getlistAfterReceive(audio)
            Log.d("TAG", "onReceivedAction: ${audio.audioFile.fileName}")
            super.onReceivedAction(fragmentMeta)
        }
    }

    private fun getlistAfterReceive(item: AudioCutterView) {
        val mListAudios = ArrayList(_listAudioFiles.value)

        for (index in mListAudios.indices) {
            if (mListAudios[index].audioFile.file.absolutePath.equals(item.audioFile.file.absolutePath)) {
                mListAudios.remove(mListAudios[index])
                mListAudios.add(
                    index,
                    AudioCutterView(item.audioFile, isCheckChooseItem = false)
                )
            }
        }
        _listAudioFiles.postValue(mListAudios)
    }


}