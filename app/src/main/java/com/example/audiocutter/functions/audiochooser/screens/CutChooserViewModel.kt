package com.example.audiocutter.functions.audiochooser.screens

import android.app.Application
import android.text.TextUtils
import android.util.Log
import androidx.lifecycle.*
import com.example.audiocutter.base.BaseAndroidViewModel
import com.example.audiocutter.core.manager.AudioPlayer
import com.example.audiocutter.core.manager.ManagerFactory
import com.example.audiocutter.core.manager.PlayerInfo
import com.example.audiocutter.core.manager.PlayerState
import com.example.audiocutter.functions.audiochooser.event.OnActionCallback
import com.example.audiocutter.functions.audiochooser.objects.AudioCutterView
import com.example.audiocutter.functions.contacts.objects.SelectItemStatus
import com.example.audiocutter.functions.contacts.objects.SelectItemView
import com.example.audiocutter.objects.AudioFileScans
import com.example.audiocutter.objects.StateLoad
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

class CutChooserViewModel(application: Application) : BaseAndroidViewModel(application) {

    private val mContext = getApplication<Application>().applicationContext
    private val TAG = CutChooserViewModel::class.java.name
    private var currentAudioPlaying: File = File("")

    var duration: Long? = 0L

    private var audioPlayer = ManagerFactory.newAudioPlayer()
    private lateinit var mcallBack: OnActionCallback

    private var _stateLoadProgress = MutableLiveData<Int>()
    val stateLoadProgress: LiveData<Int>
        get() = _stateLoadProgress


    private var _isEmptyState = MutableLiveData<Boolean>()
    val isEmptyState: LiveData<Boolean>
        get() = _isEmptyState


    fun getStateLoading(): LiveData<Int> {
        viewModelScope
        return stateLoadProgress
    }

    fun getAudioPlayer(): AudioPlayer {
        return audioPlayer
    }

    private var filterText = ""

    private val _listAudioFiles = MediatorLiveData<List<AudioCutterView>?>()

    private var listAudioFiles = ArrayList<AudioCutterView>()

    init {
        audioPlayer.init(application.applicationContext)
        _listAudioFiles.addSource(ManagerFactory.getAudioFileManager().findAllAudioFiles()) { it ->
            when (it.state) {
                StateLoad.LOADING -> {
                    _stateLoadProgress.postValue(1)
                }
                StateLoad.LOADDONE -> {
                    _stateLoadProgress.postValue(0)

//                    val tmpList = ArrayList<AudioCutterView>()
//                    it.listAudioFiles.forEach {
//                        tmpList.add(AudioCutterView(it))
//                    }
//                    listAudioFiles = tmpList

                    synchronizationData(it)

                    Log.d(TAG, "list size 11:  ${listAudioFiles.size}")
                    _listAudioFiles.postValue(listAudioFiles)
                }
                StateLoad.LOADFAIL -> {
                    _stateLoadProgress.postValue(-1)
                }
            }
        }
    }

    private fun synchronizationData(audioFileScans: AudioFileScans) {
        val resultListAudio = ArrayList<AudioCutterView>()
        val newListAudio = audioFileScans.listAudioFiles
        var isInstance = false
        if (listAudioFiles.isEmpty()) {
            newListAudio.forEach { audioFile ->
                resultListAudio.add(AudioCutterView(audioFile))
            }
        } else {
            for (newItem in newListAudio) {
                isInstance = false
                for (oldItem in listAudioFiles) {
                    if (TextUtils.equals(newItem.getFilePath(), oldItem.audioFile.getFilePath())) {
                        resultListAudio.add(oldItem)
                        isInstance = true
                        break
                    }
                }
                if (!isInstance) {
                    resultListAudio.add(AudioCutterView(newItem))
                }
            }
        }

        listAudioFiles.clear()
        listAudioFiles.addAll(resultListAudio)
    }


    private val _listFilteredAudioFiles = liveData<List<AudioCutterView>?> {
        emitSource(_listAudioFiles.map {
            it?.let {
                var listResult: List<AudioCutterView>? = null
                listResult = ArrayList(it)
                val listEmpty = ArrayList<Boolean>()
                if (filterText.isNotEmpty()) {
                    listResult.clear()
                    it.forEach { item ->
                        val rs = item.audioFile.fileName.toLowerCase(Locale.getDefault())
                            .contains(filterText.toLowerCase(Locale.getDefault()))
                        listEmpty.add(rs)
                        if (rs) {
                            listResult.add(item)
                        }
                    }
                    if (!listEmpty.contains(true)) {
                        _isEmptyState.postValue(false)
                    } else {
                        _isEmptyState.postValue(true)
                    }
                }

                listResult
            }
        })
    }

    fun getAllAudioFile(): LiveData<List<AudioCutterView>?> {
        return _listFilteredAudioFiles
    }

    fun getStateEmpty(): LiveData<Boolean> {
        return isEmptyState
    }

    fun getListAllAudioFile(): ArrayList<AudioCutterView> {
        return ArrayList(_listAudioFiles.value ?: ArrayList())
    }

    fun getListFilteredAudios(): ArrayList<AudioCutterView> {
        return ArrayList(_listFilteredAudioFiles.value ?: ArrayList())
    }


    suspend fun play(pos: Int) {
        val listAudios = getListFilteredAudios()
        val audioItem = listAudios[pos]
        Log.d("TAG", "CheckDUrationModel:  filename:${audioItem.audioFile.fileName}  duration  ${audioItem.duration}")
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

    fun stop() {
        audioPlayer.stop()
    }

    fun searchAudio(yourTextSearch: String) {
        filterText = yourTextSearch
        _listAudioFiles.postValue(_listAudioFiles.value)
    }

    fun updateMediaInfo(playerInfo: PlayerInfo) {
        Log.d(TAG, "updateMediaInfo: ${playerInfo.playerState}")
        val listAudioViews = getListAllAudioFile()
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


    private fun updateState(audioFile: AudioCutterView, playerInfo: PlayerInfo, rs: Boolean) {

        audioFile.state = playerInfo.playerState
        audioFile.isCheckDistance = rs
        audioFile.currentPos = playerInfo.posision.toLong()
        audioFile.duration = playerInfo.duration.toLong()

    }


    private fun getAudioFilePos(file: File): Int {
        val listAudios = getListAllAudioFile()
        var i = 0
        while (i < listAudios.size) {
            if (listAudios[i].audioFile.file == file) {
                return i
            }
            i++
        }
        return -1
    }


}