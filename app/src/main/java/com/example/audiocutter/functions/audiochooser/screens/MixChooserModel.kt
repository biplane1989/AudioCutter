package com.example.audiocutter.functions.audiochooser.screens

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.example.audiocutter.base.BaseAndroidViewModel
import com.example.audiocutter.core.manager.AudioPlayer
import com.example.audiocutter.core.manager.ManagerFactory
import com.example.audiocutter.core.manager.PlayerInfo
import com.example.audiocutter.core.manager.PlayerState
import com.example.audiocutter.functions.audiochooser.objects.AudioCutterView
import com.example.audiocutter.objects.StateLoad
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

class MixChooserModel(application: Application) : BaseAndroidViewModel(application) {

    private val audioPlayer = ManagerFactory.newAudioPlayer()
    private val TAG = MixChooserModel::class.java.name

    private var _stateLoadProgress = MutableLiveData<Int>()
    val stateLoadProgress: LiveData<Int>
        get() = _stateLoadProgress

    private var filterText = ""

    private var _isChooseItemState = MutableLiveData<Boolean?>()
    val isChooseItemState: LiveData<Boolean?>
        get() = _isChooseItemState

    fun getIsChooseItemState(): LiveData<Boolean?> {
        return isChooseItemState
    }

    private var _isEmptyState = MutableLiveData<Boolean>()
    val isEmptyState: LiveData<Boolean>
        get() = _isEmptyState


    private var _stateChecked = MutableLiveData<Int>()
    val stateChecked: LiveData<Int>
        get() = _stateChecked

    @JvmName("getStateChecked1")
    fun getStateChecked(): LiveData<Int> {
        return stateChecked
    }

    fun call() {
        _isChooseItemState.postValue(null)
    }

    private val _listAudioFiles = MediatorLiveData<List<AudioCutterView>?>()

    fun getAudioPlayer(): AudioPlayer {
        return audioPlayer
    }

    init {
        audioPlayer.init(application.applicationContext)
        _listAudioFiles.addSource(ManagerFactory.getAudioFileManager().findAllAudioFiles()) {
            var listAudioFiles: List<AudioCutterView>? = null

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


    private fun getListFilteredAudio(): ArrayList<AudioCutterView> {
        return ArrayList(_listFilteredAudioFiles.value ?: ArrayList())
    }

    private fun getListAllAudio(): ArrayList<AudioCutterView> {
        return ArrayList(_listAudioFiles.value ?: ArrayList())
    }

    fun getStateEmpty(): LiveData<Boolean> {
        return isEmptyState
    }


    fun getStateLoading(): LiveData<Int> {
        return stateLoadProgress
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

            mListAudios[pos].isCheckChooseItem = rs


            var count = 0
            for (item in mListAudios) {
                if (item.isCheckChooseItem) {
                    count++
                }
                if (count >= 2) {
                    break
                }
            }
            if (count >= 2 && !rs) {
                _isChooseItemState.postValue(true)
            } else {
                _isChooseItemState.postValue(false)
            }

            _stateChecked.postValue(count)

            _listAudioFiles.postValue(mListAudios)
        } catch (e: Exception) {
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
        val audioItem = getListFilteredAudio()[pos]
        audioPlayer.play(audioItem.audioFile)

    }

    fun pause() {
        audioPlayer.pause()
    }

    fun resume() {
        audioPlayer.resume()
    }

    fun stop() {
        audioPlayer.stop()
    }


}