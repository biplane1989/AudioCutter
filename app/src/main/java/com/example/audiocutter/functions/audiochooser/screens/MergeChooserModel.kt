package com.example.audiocutter.functions.audiochooser.screens

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.example.audiocutter.base.BaseAndroidViewModel
import com.example.audiocutter.base.SingleLiveEvent
import com.example.audiocutter.core.audiomanager.Folder
import com.example.audiocutter.core.manager.AudioPlayer
import com.example.audiocutter.core.manager.ManagerFactory
import com.example.audiocutter.core.manager.PlayerState
import com.example.audiocutter.functions.audiochooser.objects.AudioCutterViewItem
import com.example.audiocutter.functions.common.SortField
import com.example.audiocutter.functions.common.SortType
import com.example.audiocutter.functions.common.SortValue
import com.example.audiocutter.functions.mystudio.Constance
import com.example.audiocutter.objects.AudioFile
import com.example.audiocutter.objects.StateLoad
import com.example.audiocutter.util.Utils
import com.example.core.core.AudioFormat
import com.example.core.core.AudioMergingConfig
import kotlinx.coroutines.*
import java.util.*
import kotlin.collections.ArrayList

fun List<AudioCutterViewItem>.findAudioCutterView(filePath: String): AudioCutterViewItem? {
    this.forEach {
        if (it.audioFile.getFilePath() == filePath) {
            return it
        }
    }
    return null
}

fun List<AudioCutterViewItem>.unselectedItem(filePath: String): List<AudioCutterViewItem> {
    val item = this.findAudioCutterView(filePath)
    item?.let {
        val index = this.indexOf(item)
        if (index != -1) {
            val listCopy = ArrayList(this)
            listCopy[index] = it.copy(isCheckChooseItem = false, no = -1,isplaying = false, state = PlayerState.IDLE)
            return listCopy
        }
    }
    return this
}

data class MergingDialogData(val totalItemSelected: Int, val suggestionName: String)
data class MergingAudioData(
    val mergingConfig: AudioMergingConfig,
    val listItemsSlected: List<AudioCutterViewItem>
)


class MergeChooserModel(application: Application) : BaseAndroidViewModel(application) {
    private val audioPlayer = ManagerFactory.newAudioPlayer()
    private val TAG = "NmcheckScrMer"

    private var _stateLoadProgress = MutableLiveData<Int>()
    val stateLoadProgress: LiveData<Int>
        get() = _stateLoadProgress


    private var currNo = 0

    private val _searchAudioName = MutableLiveData<String>("")
    private val _sortAudioValue =
        MutableLiveData<SortValue>(SortValue(SortType.ASC, SortField.SORT_BY_NAME))
    private val listAudioFiles = ManagerFactory.getAudioFileManager().getAudioFiles()

    private val _listAudioCutterViewItems = MediatorLiveData<List<AudioCutterViewItem>>()
    val listAudioCutterViewItems: LiveData<List<AudioCutterViewItem>> = _listAudioCutterViewItems
    private var syncDataJob: Job? = null

    private val _showSortAudioDialog = SingleLiveEvent<SortValue>()
    val showSortAudioDialog: LiveData<SortValue> = _showSortAudioDialog

    fun getAudioPlayer(): AudioPlayer {
        return audioPlayer
    }

    private val _countItemSelected = MutableLiveData<Int>()
    val countItemSelected: LiveData<Int> = _countItemSelected


    private val _showMergingDialog = SingleLiveEvent<MergingDialogData>()
    val showMergingDialog: LiveData<MergingDialogData> = _showMergingDialog

    private val _onMergingButtonClicked = SingleLiveEvent<MergingAudioData>()
    val onMergingButtonClicked: LiveData<MergingAudioData> = _onMergingButtonClicked

    private val _checkLessThanTwoItemsIsSelected = SingleLiveEvent<Boolean>()
    val checkLessThanTwoItemsIsSelected: LiveData<Boolean> = _checkLessThanTwoItemsIsSelected

    private val _onMergingNextButtonClicked = SingleLiveEvent<Boolean>()
    val onMergingNextButtonClicked: LiveData<Boolean> = _onMergingNextButtonClicked

    private val _listAudioCutterViewItemsSelected = MutableLiveData<List<AudioCutterViewItem>>()
    val listAudioCutterViewItemsSelected: LiveData<List<AudioCutterViewItem>> =
        _listAudioCutterViewItemsSelected

    val checkNextButtonEnable: LiveData<Boolean> = listAudioCutterViewItemsSelected.map {
        _countItemSelected.value = it.size
        it.size >= 2
    }

    init {
        audioPlayer.init(application.applicationContext)
        _listAudioCutterViewItems.addSource(ManagerFactory.getAudioFileManager().getAudioFiles()) {
//            var listAudioFiles: List<AudioCutterView>? = null
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
                else -> {
                    //do nothing
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

    private suspend fun removeAllFilePathNotExistedInAudioFileSelectedMap(listAudioFileData: List<AudioFile>)=
        coroutineScope {
        val listAudioFilePathMap = HashSet(listAudioFileData.map { it.getFilePath() })
        listAudioCutterViewItemsSelected.value?.let {
            withContext(Dispatchers.Main){
                _listAudioCutterViewItemsSelected.value =
                    it.filter { listAudioFilePathMap.contains(it.audioFile.getFilePath()) }.sortedBy { it.no }
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
            removeAllFilePathNotExistedInAudioFileSelectedMap(listAudioFileData)
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
            val audioFileSelected = ArrayList<String>()
            listAudioCutterViewItemsSelected.value?.let {
                audioFileSelected.addAll(it.map { it.audioFile.getFilePath() })
            }
            listAudioFileFiltered.forEach {
                val oldItem = listAudioCutterViewItems.value?.findItem(it)
                if (oldItem == null) {
                    listAudioCutterItems.add(
                        AudioCutterViewItem(
                            it,
                            isCheckChooseItem = audioFileSelected.contains(it.getFilePath())
                        )
                    )
                } else {
                    listAudioCutterItems.add(oldItem)
                }
            }
            if (isActive) {
                withContext(Dispatchers.Main) {
                    _stateLoadProgress.value = 0
                    _listAudioCutterViewItems.value = listAudioCutterItems
                }

            }
        }
    }


    fun searchAudio(yourTextSearch: String) {
        _searchAudioName.value = yourTextSearch

    }

    fun chooseItemAudioFile(position: Int) {
        listAudioCutterViewItems.value?.let {
            if (position < 0 || position >= it.size) {
                return
            }
            val audioFileSelected = ArrayList<String>()
            listAudioCutterViewItemsSelected.value?.let {
                audioFileSelected.addAll(it.map { it.audioFile.getFilePath() })
            }
            val itemCopy = it[position].copy(isCheckChooseItem = !it[position].isCheckChooseItem)
            if (itemCopy.isCheckChooseItem) {
                if (audioFileSelected.indexOf(itemCopy.audioFile.getFilePath()) == -1) {
                    currNo++
                }
            }
            if (itemCopy.no == -1) {
                itemCopy.no = currNo
            }
            val listCopy = ArrayList(it)
            listCopy[position] = itemCopy
            _listAudioCutterViewItems.value = listCopy
            _listAudioCutterViewItemsSelected.value = listCopy.filter { it.isCheckChooseItem }.sortedBy { it.no }
        }
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

    fun onMergingDialogResult(fileName: String) {
        val itemMaxDuration = findItemMaxDurationSelected()
        itemMaxDuration?.let {
            val mergingConfig = AudioMergingConfig(
                getFormatFile(it.audioFile.getFilePath()),
                fileName,
                ManagerFactory.getAudioFileManager().getRelFolderPath(Folder.TYPE_MERGER),
                ManagerFactory.getAudioFileManager().getFolderPath(Folder.TYPE_MERGER)
            )
            listAudioCutterViewItemsSelected.value?.let {
                _onMergingButtonClicked.value = MergingAudioData(mergingConfig, it)
            }

        }
    }

    fun clickedOnMergingButton() {
        val totalItemSelected = _countItemSelected.value ?: 0
        if (totalItemSelected >= 2) {
            val itemMaxDuration = findItemMaxDurationSelected()
            itemMaxDuration?.let {
                val mergeDialogData = MergingDialogData(
                    totalItemSelected,
                    Utils.getBaseName(itemMaxDuration.audioFile.file)
                )
                _showMergingDialog.value = mergeDialogData
            }
        } else {
            _checkLessThanTwoItemsIsSelected.value = true
        }
    }

    fun clickedOnNextButton() {
        if (getAudioPlayer().getAudioIsPlaying()) {
            stop()
        }
        val totalItemSelected = _countItemSelected.value ?: 0
        if (totalItemSelected >= 2) {
            listAudioCutterViewItems.value?.let {
                it.forEach {
                    if (!it.isCheckChooseItem) {
                        it.no = -1
                    }
                }
            }
            _onMergingNextButtonClicked.value = true
        } else {
            _checkLessThanTwoItemsIsSelected.value = true
        }
    }

    fun clickedOnSortButton(){
        _sortAudioValue.value?.let {
            _showSortAudioDialog.value = it
        }

    }

    private fun findItemMaxDurationSelected(): AudioCutterViewItem? {
        listAudioCutterViewItemsSelected.value?.let {
            return it.sortedByDescending { it.duration }.firstOrNull()
        }
        return null
    }

    fun swapItemAudio(index1: Int, index2: Int) {
        listAudioCutterViewItemsSelected.value?.let {
            val listItemSelectedCopy = ArrayList(it)
            val item1 = it[index1].copy()
            val item2 = it[index2].copy()
            listItemSelectedCopy[index1] = item1
            listItemSelectedCopy[index2] = item2

            listAudioCutterViewItems.value?.let {
                val audioCutterView1 = it.findAudioCutterView(item1.audioFile.getFilePath())
                val audioCutterView2 = it.findAudioCutterView(item2.audioFile.getFilePath())
                audioCutterView1?.swapNo(audioCutterView2)
            }
            item1.swapNo(item2)
            _listAudioCutterViewItemsSelected.value = listItemSelectedCopy.sortedBy { it.no }
        }
    }

    fun removeItemAudio(item: AudioCutterViewItem) {
        listAudioCutterViewItems.value?.let {
            val newList = it.unselectedItem(item.audioFile.getFilePath())
            _listAudioCutterViewItems.value = newList
            _listAudioCutterViewItemsSelected.value =
                newList.filter { it.isCheckChooseItem }.sortedBy { it.no }

        }
    }

    fun sortAudioBy(sortValue: SortValue) {
        if (_sortAudioValue.value != sortValue) {
            _sortAudioValue.value = sortValue
        }
    }

    private fun getFormatFile(path: String): AudioFormat {
        var mimeType = ""
        if (path.indexOf(".") != -1) {
            mimeType = path.substring(path.lastIndexOf("."), path.length)
        }

        return if (mimeType == Constance.MP3) {
            AudioFormat.MP3
        } else if (mimeType == Constance.AAC || mimeType == Constance.M4A) {
            AudioFormat.AAC
        } else {
            AudioFormat.MP3
        }

    }


}