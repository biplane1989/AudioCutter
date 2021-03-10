package com.example.audiocutter.functions.audiochooser.screens

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseAndroidViewModel
import com.example.audiocutter.base.SingleLiveEvent
import com.example.audiocutter.core.audiomanager.Folder
import com.example.audiocutter.core.manager.AudioPlayer
import com.example.audiocutter.core.manager.ManagerFactory
import com.example.audiocutter.core.manager.PlayerState
import com.example.audiocutter.functions.audiochooser.objects.AudioCutterViewItem
import com.example.audiocutter.functions.audiochooser.objects.FolderItem
import com.example.audiocutter.functions.audiochooser.objects.FolderStatus
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

// todo

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
            listCopy[index] = it.copy(isCheckChooseItem = false, no = -1, isplaying = false, state = PlayerState.IDLE)
            return listCopy
        }
    }
    return this
}

data class MergingDialogData(val totalItemSelected: Int, val suggestionName: String)
data class MergingAudioData(val mergingConfig: AudioMergingConfig, val listItemsSlected: List<AudioCutterViewItem>)


class MergeChooserModel(application: Application) : BaseAndroidViewModel(application) {

    private val mContext = getApplication<Application>().applicationContext
    private val audioPlayer = ManagerFactory.newAudioPlayer()
    private val TAG = "NmcheckScrMer"

    private var _stateLoadProgress = MutableLiveData<Int>()
    val stateLoadProgress: LiveData<Int>
        get() = _stateLoadProgress

    private var currNo = 0

    private val _searchAudioName = MutableLiveData<String>("")
    private val _sortAudioValue = MutableLiveData<SortValue>(SortValue(SortType.ASC, SortField.SORT_BY_NAME))
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
    val listAudioCutterViewItemsSelected: LiveData<List<AudioCutterViewItem>> = _listAudioCutterViewItemsSelected

    val checkNextButtonEnable: LiveData<Boolean> = listAudioCutterViewItemsSelected.map {
        _countItemSelected.value = it.size
        it.size >= 2
    }

    private var _isEmptyState = MutableLiveData<Boolean>()
    val isEmptyState: LiveData<Boolean>
        get() = _isEmptyState

    private val _folderLiveData = MutableLiveData<FolderStatus>(FolderStatus(mContext.getString(R.string.all), false))
    val folderLiveData: LiveData<FolderStatus> get() = _folderLiveData

    private val _listFolder = MutableLiveData<List<FolderItem>>()
    val listFolder: LiveData<List<FolderItem>> get() = _listFolder

    private var listAudioFileView = ArrayList<AudioCutterViewItem>()


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
        _listAudioCutterViewItems.addSource(_folderLiveData) {
            syncDataJob?.cancel()
            syncDataJob = viewModelScope.launch {
                synchronizationData()
            }
        }

    }

    private suspend fun removeAllFilePathNotExistedInAudioFileSelectedMap(listAudioFileData: List<AudioFile>) = coroutineScope {
        val listAudioFilePathMap = HashSet(listAudioFileData.map { it.getFilePath() })
        _listAudioCutterViewItemsSelected.value?.let {
            withContext(Dispatchers.Main) {
                _listAudioCutterViewItemsSelected.value = it.filter { listAudioFilePathMap.contains(it.audioFile.getFilePath()) }
                    .sortedBy { it.no }
            }
        }
    }

    private suspend fun synchronizationData() = coroutineScope {
        val searchAudioNameValue = _searchAudioName.value
        val sortTypeValue = _sortAudioValue.value
//        val listAudioFileData = listAudioFiles.value?.listAudioFiles

        var listAudioFileData: List<AudioCutterViewItem>? = null

        if (_folderLiveData.value?.folder.equals(mContext.getString(R.string.all))) {
            listAudioFileData = listAudioFileView
        } else {
            listAudioFileData = listAudioFileView.filter { it.folder.equals(_folderLiveData.value?.folder) }
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
                    listAudioFileFiltered = if (sortTypeValue.sortType == SortType.ASC) listAudioFileFiltered.sortedBy { it.audioFile.fileName } else listAudioFileFiltered.sortedByDescending { it.audioFile.fileName }
                }
                SortField.SORT_BY_DURATION -> {
                    listAudioFileFiltered = if (sortTypeValue.sortType == SortType.ASC) listAudioFileFiltered.sortedBy { it.audioFile.duration } else listAudioFileFiltered.sortedByDescending { it.audioFile.duration }
                }
                SortField.SORT_BY_DATE -> {
                    listAudioFileFiltered = if (sortTypeValue.sortType == SortType.ASC) listAudioFileFiltered.sortedBy { it.audioFile.modified } else listAudioFileFiltered.sortedByDescending { it.audioFile.modified }
                }
                SortField.SORT_BY_SIZE -> {
                    listAudioFileFiltered = if (sortTypeValue.sortType == SortType.ASC) listAudioFileFiltered.sortedBy { it.audioFile.size } else listAudioFileFiltered.sortedByDescending { it.audioFile.size }
                }
            }
            val audioFileSelected = ArrayList<String>()
            listAudioCutterViewItemsSelected.value?.let {
                audioFileSelected.addAll(it.map { it.audioFile.getFilePath() })
            }
            listAudioFileFiltered.forEach {
                val oldItem = listAudioCutterViewItems.value?.findItem(it.audioFile)
                if (oldItem == null) {
                    listAudioCutterItems.add(AudioCutterViewItem(it.audioFile, isCheckChooseItem = audioFileSelected.contains(it.audioFile.getFilePath())))
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

            var index = 0
            var newItem: AudioCutterViewItem
            for (item in listAudioFileView) {
                if (item.audioFile.getFilePath().equals(itemCopy.audioFile.getFilePath())) {
                    newItem = item.copy()
                    newItem.isCheckChooseItem = itemCopy.isCheckChooseItem
                    newItem.no = itemCopy.no
                    listAudioFileView.set(index, newItem)
                }
                index++
            }
            _listAudioCutterViewItemsSelected.value = listAudioFileView.filter { it.isCheckChooseItem }
                .sortedBy { it.no }
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
            val mergingConfig = AudioMergingConfig(getFormatFile(it.audioFile.getFilePath()), fileName, ManagerFactory.getAudioFileManager()
                .getRelFolderPath(Folder.TYPE_MERGER), ManagerFactory.getAudioFileManager()
                .getFolderPath(Folder.TYPE_MERGER))
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
                val mergeDialogData = MergingDialogData(totalItemSelected, Utils.getBaseName(itemMaxDuration.audioFile.file))
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
//            listAudioCutterViewItems.value?.let {
            listAudioFileView.let {
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

    fun clickedOnSortButton() {
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
        viewModelScope.launch {
            listAudioCutterViewItemsSelected.value?.let {
                val listItemSelectedCopy = ArrayList(it)
                val item1 = it[index1].copy()
                val item2 = it[index2].copy()

                val no1 = item1.no
                val no2 = item2.no
                item1.no = no2
                item2.no = no1

                listItemSelectedCopy[index1] = item2
                listItemSelectedCopy[index2] = item1

//            listAudioFileView.let {
                val audioCutterView1 = listAudioFileView.findAudioCutterView(item1.audioFile.getFilePath())
                val audioCutterView2 = listAudioFileView.findAudioCutterView(item2.audioFile.getFilePath())
                val num1 = audioCutterView1?.no
                val num2 = audioCutterView2?.no

//                audioCutterView1?.swapNo(audioCutterView2)
//            }
                var index = 0
                for (item in listAudioFileView){
                    if (item.audioFile.getFilePath().equals(audioCutterView1?.audioFile?.getFilePath())){
                        if (num2 != null) {
                            listAudioFileView[index].no = num2
                        }
                    }
                    if (item.audioFile.getFilePath().equals(audioCutterView2?.audioFile?.getFilePath())){
                        if (num1 != null) {
                            listAudioFileView[index].no = num1
                        }
                    }
                    index++
                }

//                listAudioCutterViewItems.value?.let {
//                    val audioCutterView1 = it.findAudioCutterView(item1.audioFile.getFilePath())
//                    val audioCutterView2 = it.findAudioCutterView(item2.audioFile.getFilePath())
//                    audioCutterView1?.swapNo(audioCutterView2)
//                }

//                item1.swapNo(item2)
//                for (item in listItemSelectedCopy.sortedBy { it.no }){
//                    Log.d("giangtd001", "swapItemAudio: name new: **"+ item.audioFile.fileName)
//                }
                for (item in listAudioFileView){
                    Log.d("giangtd001", "swapItemAudio: name new: **"+ item.audioFile.fileName + " no: "+ item.no)
                }

                _listAudioCutterViewItemsSelected.value = listItemSelectedCopy.sortedBy { it.no }
            }

//            synchronizationData()
        }

    }

    fun removeItemAudio(item: AudioCutterViewItem) {

//        listAudioCutterViewItems.value?.let {
//            val newList = it.unselectedItem(item.audioFile.getFilePath())
//            _listAudioCutterViewItems.value = newList
//            _listAudioCutterViewItemsSelected.value =
//                newList.filter { it.isCheckChooseItem }.sortedBy { it.no }
//
//        }

        viewModelScope.launch {
//            listAudioFileView.let {
//                val newList = it.unselectedItem(item.audioFile.getFilePath())
//                listAudioFileView = newList as ArrayList<AudioCutterViewItem>
//                _listAudioCutterViewItems.value = emptyList()
//                _listAudioCutterViewItemsSelected.value = listAudioFileView.filter { it.isCheckChooseItem }
//                    .sortedBy { it.no }
//
//            }
//            listAudioFileView?.let {

//                val newList = it.unselectedItem(item.audioFile.getFilePath())
//                _listAudioCutterViewItems.value = newList
//                _listAudioCutterViewItemsSelected.value =
//                    newList.filter { it.isCheckChooseItem }.sortedBy { it.no }
                var index = 0
                for (data in listAudioFileView){
                    if (item.audioFile.getFilePath().equals(data.audioFile.getFilePath())){
                        val newItem = data.copy()
                        newItem.isCheckChooseItem = false
                        newItem.isplaying = false
                        newItem.state = PlayerState.IDLE
                        listAudioFileView.set(index, newItem)
                        break
                    }
                    index++
                }
                _listAudioCutterViewItems.value = listAudioFileView

                _listAudioCutterViewItemsSelected.value =
                    listAudioFileView.filter { it.isCheckChooseItem }.sortedBy { it.no }
//            }

            synchronizationData()
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