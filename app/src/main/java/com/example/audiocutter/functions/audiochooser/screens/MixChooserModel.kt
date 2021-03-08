package com.example.audiocutter.functions.audiochooser.screens

import android.app.Application
import android.text.TextUtils
import android.util.Log
import androidx.lifecycle.*
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseAndroidViewModel
import com.example.audiocutter.base.SingleLiveEvent
import com.example.audiocutter.core.manager.AudioPlayer
import com.example.audiocutter.core.manager.ManagerFactory
import com.example.audiocutter.functions.audiochooser.objects.AudioCutterViewItem
import com.example.audiocutter.functions.audiochooser.objects.FolderItem
import com.example.audiocutter.functions.audiochooser.objects.FolderStatus
import com.example.audiocutter.functions.common.SortField
import com.example.audiocutter.functions.common.SortType
import com.example.audiocutter.functions.common.SortValue
import com.example.audiocutter.objects.AudioFile
import com.example.audiocutter.objects.AudioFileScans
import com.example.audiocutter.objects.StateLoad
import kotlinx.coroutines.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.HashSet

class MixChooserModel(application: Application) : BaseAndroidViewModel(application) {

    private val mContext = getApplication<Application>().applicationContext
    private val audioPlayer = ManagerFactory.newAudioPlayer()
    private val TAG = MixChooserModel::class.java.name

    private var _stateLoadProgress = MutableLiveData<Int>()
    val stateLoadProgress: LiveData<Int>
        get() = _stateLoadProgress


    private var _isEmptyState = MutableLiveData<Boolean>()
    val isEmptyState: LiveData<Boolean>
        get() = _isEmptyState

    private val _folderLiveData =
        MutableLiveData<FolderStatus>(FolderStatus(mContext.getString(R.string.all), false))
    val folderLiveData: LiveData<FolderStatus> get() = _folderLiveData

    private val _listFolder = MutableLiveData<List<FolderItem>>()
    val listFolder: LiveData<List<FolderItem>> get() = _listFolder

    private val _listAudioCutterViewItems = MediatorLiveData<List<AudioCutterViewItem>>()
    val listAudioCutterViewItems: LiveData<List<AudioCutterViewItem>> = _listAudioCutterViewItems

    fun getAudioPlayer(): AudioPlayer {
        return audioPlayer
    }


    private val _showSortAudioDialog = SingleLiveEvent<SortValue>()
    val showSortAudioDialog: LiveData<SortValue> = _showSortAudioDialog

    private val _checkMoreThanTwoItemsIsSelected = SingleLiveEvent<Boolean>()
    val checkMoreThanTwoItemsIsSelected: LiveData<Boolean> = _checkMoreThanTwoItemsIsSelected

    private val _searchAudioName = MutableLiveData<String>("")
    private val _sortAudioValue =
        MutableLiveData<SortValue>(SortValue(SortType.ASC, SortField.SORT_BY_NAME))
    private val listAudioFiles = ManagerFactory.getAudioFileManager().getAudioFiles()
    private var syncDataJob: Job? = null
    private val _countItemSelected = MutableLiveData<Int>()
    val countItemSelected: LiveData<Int> = _countItemSelected

    private val _onMixingNextButtonClicked = SingleLiveEvent<List<AudioFile>>()
    val onMixingNextButtonClicked: LiveData<List<AudioFile>> = _onMixingNextButtonClicked

    val checkNextButtonEnable: LiveData<Boolean> = listAudioCutterViewItems.map {
        val countItemSelected = audioFileSelectedMap.size
        _countItemSelected.value = countItemSelected
        countItemSelected == 2
    }

    private val audioFileSelectedMap = HashSet<String>()

    private val listAudioFileView = ArrayList<AudioCutterViewItem>()        //list audio have folser

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
                        listAudioFileView.clear()                               // todo show list folder
                        val newList = listAudioFiles.value?.listAudioFiles
                        newList?.let {
                            for (item in it) {
                                val audio = AudioCutterViewItem(item)
                                item.file.parentFile?.name?.let {
                                    audio.folder = it
                                    listAudioFileView.add(audio)
                                }
                            }
                        }
                        synchronizationData()
                    }

                }
                StateLoad.LOADFAIL -> {
                    _stateLoadProgress.value = -1
                }
                else -> {

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
        _listAudioCutterViewItems.addSource(_folderLiveData) {
            syncDataJob?.cancel()
            syncDataJob = viewModelScope.launch {
                synchronizationData()
            }
        }
    }

    fun sortAudioBy(sortValue: SortValue) {
        if (_sortAudioValue.value != sortValue) {
            _sortAudioValue.value = sortValue
        }
    }

    private fun removeAllFilePathNotExistedInAudioFileSelectedMap(listAudioFileData: List<AudioFile>) {
        val listAudioFilePathMap = HashSet(listAudioFileData.map { it.getFilePath() })
        val listItems = audioFileSelectedMap.filter { listAudioFilePathMap.contains(it) }
        audioFileSelectedMap.clear()
        audioFileSelectedMap.addAll(listItems)
    }

    private suspend fun synchronizationData() = coroutineScope {
        val searchAudioNameValue = _searchAudioName.value
        val sortTypeValue = _sortAudioValue.value
//        val listAudioFileData = listAudioFiles.value?.listAudioFiles


        var listAudioFileData: List<AudioCutterViewItem>? = null

        if (_folderLiveData.value?.folder.equals(mContext.getString(R.string.all))) {
            listAudioFileData = listAudioFileView
        } else {
            listAudioFileData =
                listAudioFileView.filter { it.folder.equals(_folderLiveData.value?.folder) }
        }

        if (searchAudioNameValue == null || sortTypeValue == null || listAudioFileData == null || listAudioFiles.value?.state != StateLoad.LOADDONE) {
            return@coroutineScope
        }
        withContext(Dispatchers.Default) {
            removeAllFilePathNotExistedInAudioFileSelectedMap(listAudioFileView.map { it.audioFile })

            val listAudioCutterItems = ArrayList<AudioCutterViewItem>()
            var listAudioFileFiltered = listAudioFileData
            if (searchAudioNameValue.isNotEmpty()) {
                listAudioFileFiltered = listAudioFileFiltered.filter {
                    it.audioFile.fileName.toLowerCase(Locale.getDefault())
                        .contains(searchAudioNameValue.toLowerCase(Locale.getDefault()))
                }
            }
            when (sortTypeValue.sortField) {
                SortField.SORT_BY_NAME -> {
                    listAudioFileFiltered =
                        if (sortTypeValue.sortType == SortType.ASC) listAudioFileFiltered.sortedBy { it.audioFile.fileName } else listAudioFileFiltered.sortedByDescending { it.audioFile.fileName }
                }
                SortField.SORT_BY_DURATION -> {
                    listAudioFileFiltered =
                        if (sortTypeValue.sortType == SortType.ASC) listAudioFileFiltered.sortedBy { it.audioFile.duration } else listAudioFileFiltered.sortedByDescending { it.audioFile.duration }
                }
                SortField.SORT_BY_DATE -> {
                    listAudioFileFiltered =
                        if (sortTypeValue.sortType == SortType.ASC) listAudioFileFiltered.sortedBy { it.audioFile.modified } else listAudioFileFiltered.sortedByDescending { it.audioFile.modified }
                }
                SortField.SORT_BY_SIZE -> {
                    listAudioFileFiltered =
                        if (sortTypeValue.sortType == SortType.ASC) listAudioFileFiltered.sortedBy { it.audioFile.size } else listAudioFileFiltered.sortedByDescending { it.audioFile.size }
                }
            }
            listAudioFileFiltered.forEach {
                val oldItem = listAudioCutterViewItems.value?.findItem(it.audioFile)
                if (oldItem == null) {
                    listAudioCutterItems.add(
                        AudioCutterViewItem(
                            it.audioFile,
                            isCheckChooseItem = audioFileSelectedMap.contains(it.audioFile.getFilePath())
                        )
                    )
                } else {
                    listAudioCutterItems.add(oldItem)
                }
            }
            if (isActive) {
                withContext(Dispatchers.Main) {
                    _stateLoadProgress.value = 0
//                    _isEmptyState.value = listAudioCutterItems.size == 0
                    _isEmptyState.value = listAudioFileView.size == 0
                    _listAudioCutterViewItems.value = listAudioCutterItems
                }

            }
        }
    }

    fun showFolder() {
        var listData: List<AudioCutterViewItem> = listAudioFileView
        val newList = ArrayList<FolderItem>()
        listData = listData.sortedBy { it.folder }
        newList.add(FolderItem(mContext.getString(R.string.all), listData.size))

        var hearder = ""
        var count = 1
        var index = 1
        for (item in listData) {
            if (!item.folder.equals(hearder)) {
                hearder = item.folder
                newList.add(FolderItem(item.folder, 0))
                if (count >= 1) {
                    newList[index - 1].count = count
                }
                count = 1
                index++
            } else {
                count++
            }
            if (item == listData.get(listData.lastIndex)) {
                newList[index - 1].count = count
            }
        }
        newList.set(0, FolderItem(mContext.getString(R.string.all), listData.size))
        _listFolder.value = newList

        _folderLiveData.value?.let {        // thay doi trang thai show folder
            _folderLiveData.value = FolderStatus(it.folder, !it.status)
        }
    }

    fun clickItemFolder(itemAudio: FolderItem) {
        _folderLiveData.value?.let {        // thay doi trang thai show folder
            _folderLiveData.value = FolderStatus(itemAudio.folder, !it.status)
        }
    }


    fun getStateEmpty(): LiveData<Boolean> {
        return isEmptyState
    }


    fun getStateLoading(): LiveData<Int> {
        return stateLoadProgress
    }


    fun searchAudio(yourTextSearch: String) {
        _searchAudioName.value = yourTextSearch
    }

    fun clickedOnNextButton() {
        listAudioFiles.value?.listAudioFiles?.let {
            if (getAudioPlayer().getAudioIsPlaying()) {
                stop()
            }
            val listAudioSelected = it.filter { audioFileSelectedMap.contains(it.getFilePath()) }
            if (listAudioSelected.size == 2) {
                _onMixingNextButtonClicked.value = listAudioSelected
            }
        }
    }

    fun chooseItemAudioFile(position: Int) {
        Log.d(TAG, "chooseItemAudioFile: posititon : "+ position)
        listAudioCutterViewItems.value?.let {
            if (position < 0 || position >= it.size) {
                return
            }

            if (_countItemSelected.value == 2 && !audioFileSelectedMap.contains(it[position].audioFile.getFilePath())) {
                _checkMoreThanTwoItemsIsSelected.value = true
                return
            }
            val itemCopy = it[position].copy(isCheckChooseItem = !it[position].isCheckChooseItem)
            if (itemCopy.isCheckChooseItem) {
                audioFileSelectedMap.add(itemCopy.audioFile.getFilePath())
            } else {
                audioFileSelectedMap.remove(itemCopy.audioFile.getFilePath())
            }

            val listCopy = ArrayList(it)
            listCopy[position] = itemCopy
            _listAudioCutterViewItems.value = listCopy
        }

    }

    fun clickedOnSortButton() {
        _sortAudioValue.value?.let {
            _showSortAudioDialog.value = it
        }

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