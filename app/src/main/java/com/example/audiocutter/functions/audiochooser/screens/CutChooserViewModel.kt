package com.example.audiocutter.functions.audiochooser.screens

import android.app.Application
import android.text.TextUtils
import androidx.lifecycle.*
import com.example.audiocutter.base.BaseAndroidViewModel
import com.example.audiocutter.base.SingleLiveEvent
import com.example.audiocutter.core.manager.AudioPlayer
import com.example.audiocutter.core.manager.ManagerFactory
import com.example.audiocutter.core.manager.PlayerState
import com.example.audiocutter.functions.audiochooser.objects.AudioCutterViewItem
import com.example.audiocutter.functions.common.SortField
import com.example.audiocutter.functions.common.SortType
import com.example.audiocutter.functions.common.SortValue
import com.example.audiocutter.objects.AudioFile
import com.example.audiocutter.objects.StateLoad
import kotlinx.coroutines.*
import java.util.*
import kotlin.collections.ArrayList

fun List<AudioCutterViewItem>.findItem(audioFile: AudioFile): AudioCutterViewItem? {
    this.forEach {
        if (it.audioFile.getFilePath() == audioFile.getFilePath()) {
            return it
        }
    }
    return null
}

class CutChooserViewModel(application: Application) : BaseAndroidViewModel(application) {
    private var audioPlayer = ManagerFactory.newAudioPlayer()

    private var _stateLoadProgress = MutableLiveData<Int>()
    val stateLoadProgress: LiveData<Int>
        get() = _stateLoadProgress


    private var _isEmptyState = MutableLiveData<Boolean>()
    val isEmptyState: LiveData<Boolean>
        get() = _isEmptyState

    private val _showSortAudioDialog = SingleLiveEvent<SortValue>()
    val showSortAudioDialog: LiveData<SortValue> = _showSortAudioDialog

    private val _searchAudioName = MutableLiveData<String>("")
    private val _sortAudioValue =
        MutableLiveData<SortValue>(SortValue(SortType.ASC, SortField.SORT_BY_NAME))


    fun getStateLoading(): LiveData<Int> {
        viewModelScope
        return stateLoadProgress
    }

    fun getAudioPlayer(): AudioPlayer {
        return audioPlayer
    }


    private val listAudioFiles = ManagerFactory.getAudioFileManager().getAudioFiles()
    private val _listAudioCutterViewItems = MediatorLiveData<List<AudioCutterViewItem>>()
    val listAudioCutterViewItems: LiveData<List<AudioCutterViewItem>> = _listAudioCutterViewItems

    //private var listAudioFiles = ArrayList<AudioCutterView>()
    private var syncDataJob: Job? = null

    init {
        audioPlayer.init(application.applicationContext)
        _listAudioCutterViewItems.addSource(listAudioFiles) { it ->
            when (it.state) {
                StateLoad.LOADING -> {
                    _stateLoadProgress.value = 1
                }
                StateLoad.LOADDONE -> {
                    syncDataJob?.cancel()
                    syncDataJob = viewModelScope.launch {
                        synchronizationData()
                    }
                }
                StateLoad.LOADFAIL -> {
                    _stateLoadProgress.value = -1
                }
            }
        }
        _listAudioCutterViewItems.addSource(_searchAudioName) {
            syncDataJob?.cancel()
            syncDataJob = viewModelScope.launch {
                synchronizationData()
            }
        }
        _listAudioCutterViewItems.addSource(_sortAudioValue) {
            syncDataJob?.cancel()
            syncDataJob = viewModelScope.launch {
                synchronizationData()
            }
        }
    }

    private suspend fun synchronizationData() = coroutineScope {
        val searchAudioNameValue = _searchAudioName.value
        val sortTypeValue = _sortAudioValue.value
        val listAudioFileData = listAudioFiles.value?.listAudioFiles
        if (searchAudioNameValue == null || sortTypeValue == null || listAudioFileData == null || listAudioFiles.value?.state != StateLoad.LOADDONE) {
            return@coroutineScope
        }
        withContext(Dispatchers.Default) {
            val listAudioCutterItems = ArrayList<AudioCutterViewItem>()
            var listAudioFileFiltered = listAudioFileData
            if (searchAudioNameValue.isNotEmpty()) {
                listAudioFileFiltered = listAudioFileFiltered.filter {
                    it.fileName.toLowerCase(Locale.getDefault())
                        .contains(searchAudioNameValue.toLowerCase(Locale.getDefault()))
                }
            }
            when (sortTypeValue.sortField) {
                SortField.SORT_BY_NAME -> {
                    listAudioFileFiltered =
                        if (sortTypeValue.sortType == SortType.ASC) listAudioFileFiltered.sortedBy { it.fileName } else listAudioFileFiltered.sortedByDescending { it.fileName }
                }
                SortField.SORT_BY_DURATION -> {
                    listAudioFileFiltered =
                        if (sortTypeValue.sortType == SortType.ASC) listAudioFileFiltered.sortedBy { it.duration } else listAudioFileFiltered.sortedByDescending { it.duration }
                }
                SortField.SORT_BY_DATE -> {
                    listAudioFileFiltered =
                        if (sortTypeValue.sortType == SortType.ASC) listAudioFileFiltered.sortedBy { it.modified } else listAudioFileFiltered.sortedByDescending { it.modified }
                }
                SortField.SORT_BY_SIZE -> {
                    listAudioFileFiltered =
                        if (sortTypeValue.sortType == SortType.ASC) listAudioFileFiltered.sortedBy { it.size } else listAudioFileFiltered.sortedByDescending { it.size }
                }
            }
            listAudioFileFiltered.forEach {
                val oldItem = listAudioCutterViewItems.value?.findItem(it)
                if (oldItem == null) {
                    listAudioCutterItems.add(AudioCutterViewItem(it))
                } else {
                    listAudioCutterItems.add(oldItem)
                }
            }
            if (isActive) {
                withContext(Dispatchers.Main) {
                    _stateLoadProgress.value = 0
                    _isEmptyState.value = listAudioCutterItems.size == 0
                    _listAudioCutterViewItems.value = listAudioCutterItems
                }

            }
        }
    }
    fun clickedOnSortButton(){
        _sortAudioValue.value?.let {
            _showSortAudioDialog.value = it
        }

    }

    fun sortAudioBy(sortValue: SortValue){
        if(_sortAudioValue.value != sortValue){
            _sortAudioValue.value = sortValue
        }
    }
    fun getStateEmpty(): LiveData<Boolean> {
        return isEmptyState
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
        _searchAudioName.value = yourTextSearch
    }


}