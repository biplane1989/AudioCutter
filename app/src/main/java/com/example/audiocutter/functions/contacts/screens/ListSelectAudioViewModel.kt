package com.example.audiocutter.functions.contacts.screens

import android.app.Application
import android.text.TextUtils
import android.util.Log
import androidx.lifecycle.*
import com.example.audiocutter.base.BaseAndroidViewModel
import com.example.audiocutter.base.SingleLiveEvent
import com.example.audiocutter.core.manager.AudioPlayer
import com.example.audiocutter.core.manager.ManagerFactory
import com.example.audiocutter.core.manager.PlayerState
import com.example.audiocutter.functions.audiochooser.objects.AudioCutterViewItem
import com.example.audiocutter.functions.audiochooser.screens.findItem
import com.example.audiocutter.functions.common.SortField
import com.example.audiocutter.functions.common.SortType
import com.example.audiocutter.functions.common.SortValue
import com.example.audiocutter.functions.contacts.objects.SelectItemStatus
import com.example.audiocutter.functions.contacts.objects.SelectItemView
import com.example.audiocutter.objects.AudioFile
import com.example.audiocutter.objects.AudioFileScans
import com.example.audiocutter.objects.StateLoad
import com.example.audiocutter.util.Utils
import kotlinx.coroutines.*
import java.util.*
import kotlin.collections.ArrayList

fun List<SelectItemView>.findItem(audioFile: AudioFile): SelectItemView? {
    this.forEach {
        if (it.audioFile.getFilePath() == audioFile.getFilePath()) {
            return it
        }
    }
    return null
}

class ListSelectAudioViewModel(application: Application) : BaseAndroidViewModel(application) {
    private val _sortAudioValue =
        MutableLiveData<SortValue>(SortValue(SortType.ASC, SortField.SORT_BY_NAME))

    private val mContext = getApplication<Application>().applicationContext
    private val audioPlayer = ManagerFactory.newAudioPlayer()

    private val _showSortAudioDialog = SingleLiveEvent<SortValue>()
    val showSortAudioDialog: LiveData<SortValue> = _showSortAudioDialog

    private val mAudioMediatorLiveData = MediatorLiveData<ArrayList<SelectItemView>>()
    private var loadingStatus: MutableLiveData<Boolean> = MutableLiveData()
    private var isEmptyStatus: MutableLiveData<Boolean> = MutableLiveData()
    private var isChoseMusic: MutableLiveData<Boolean> = MutableLiveData()
    private var isFisrtLoadData = true
    private var syncDataJob: Job? = null
    private var itemSelected: SelectItemView? = null

    fun getLoadingStatus(): LiveData<Boolean> {
        return loadingStatus
    }

    fun getIsEmptyStatus(): LiveData<Boolean> {
        return isEmptyStatus
    }

    fun getListAudioFile(): MediatorLiveData<ArrayList<SelectItemView>> {
        return mAudioMediatorLiveData
    }

    fun getChoseMusic(): LiveData<Boolean> {
        return isChoseMusic
    }

    private val listAudioFiles = ManagerFactory.getAudioFileManager().getAudioFiles()
    private val _searchAudioName = MutableLiveData<String>("")
    private lateinit var mInitFileUri: String

    init {
        audioPlayer.init(application.applicationContext)
    }

    fun init(fileUri: String) {
        mInitFileUri = fileUri
        mAudioMediatorLiveData.addSource(_searchAudioName) {
            syncDataJob?.cancel()
            syncDataJob = viewModelScope.launch {
                synchronizationData()
            }
        }
        mAudioMediatorLiveData.addSource(_sortAudioValue) {
            syncDataJob?.cancel()
            syncDataJob = viewModelScope.launch {
                synchronizationData()
            }
        }

        mAudioMediatorLiveData.addSource(listAudioFiles) {

            loadingStatus.value = true
            if (it.state == StateLoad.LOADING) {
                loadingStatus.postValue(true)
            }
            if (it.state == StateLoad.LOADDONE) {
                syncDataJob?.cancel()
                syncDataJob = viewModelScope.launch {
                    synchronizationData()
                }


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
            val resultListAudio = ArrayList<SelectItemView>()
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
                if (itemSelected?.audioFile?.getFilePath() == it.getFilePath()) {
                    itemSelected?.let {
                        resultListAudio.add(it)
                    }
                } else {
                    val oldItem = mAudioMediatorLiveData.value?.findItem(it)
                    if (oldItem == null) {
                        val item = SelectItemView(
                            it,
                            false,
                            false,
                            SelectItemStatus(),
                            false
                        )
                        resultListAudio.add(item)
                    } else {
                        resultListAudio.add(oldItem)
                    }
                }

            }
            if (isActive) {
                withContext(Dispatchers.Main) {
                    loadingStatus.value = false
                    isEmptyStatus.value = resultListAudio.size == 0
                    mAudioMediatorLiveData.value = resultListAudio
                    if (isFisrtLoadData) {
                        selectRingtone(mInitFileUri)
                        isFisrtLoadData = false
                    }
                }

            }
        }
    }

    fun selectRingtone(ringtonePath: String) {
        mAudioMediatorLiveData.value?.let {
            val listCopy = ArrayList(it)
            var index = 0
            for (item in listCopy) {
                if (TextUtils.equals(item.audioFile.uri.toString(), ringtonePath)) {
                    val newItem = item.copy()
                    newItem.isSelect = true
                    listCopy.set(index, newItem)
                    mAudioMediatorLiveData.value = listCopy
                    break
                }
                index++
            }
            checkIsChoseRingTone()
        }
    }

    fun getIndexSelectRingtone(ringtonePath: String): Int { // lay vi tri cua file audio la nhac chuong cua contact
        mAudioMediatorLiveData.value?.let {
            var index = 0
            for (item in it) {
                if (TextUtils.equals(item.audioFile.uri.toString(), ringtonePath)) {

                    return index
                }
                index++
            }
        }
        return 0
    }

    fun checkIsChoseRingTone() {
        mAudioMediatorLiveData.value?.let {
            if (it.any { it.isSelect }) {
                isChoseMusic.value = true
            } else {
                isChoseMusic.value = false
            }
        }
    }

    fun showPlayingAudio(filePath: String) {
        itemSelected = null
        // khi play nhac reset lai trang thai cac item khac
        mAudioMediatorLiveData.value?.let {
            val newAudioList = ArrayList(it)
            var index1 = 0
            for (item in newAudioList) {
                if (!TextUtils.equals(item.getFilePath(), filePath)) {
                    val newItem = item.copy()
                    newItem.isSelect = false
                    newItem.isExpanded = false
                    newAudioList[index1] = newItem
                } else {
                    val newItem = item.copy()
                    newItem.isSelect = true
                    newItem.isExpanded = !item.isExpanded
                    newAudioList[index1] = newItem
                    itemSelected = newItem
                }
                index1++
            }

            /*  var index = 0
              for (item in mListAudioFileView) {
                  if (!TextUtils.equals(item.getFilePath(), filePath)) {
                      val newItem = item.copy()
                      newItem.isSelect = false
                      newItem.isExpanded = false
                      mListAudioFileView[index] = newItem
                  } else {
                      val newItem = item.copy()
                      newItem.isSelect = true
  //                newItem.isExpanded = !item.isExpanded
                      mListAudioFileView[index] = newItem
                  }
                  index++
              }*/
            mAudioMediatorLiveData.value = newAudioList
            checkIsChoseRingTone()
        }
    }

    private fun getRingtoneDefault(list: List<SelectItemView>): List<SelectItemView> {
        val ringtoneDefault = Utils.getUriRingtoneDefault(mContext)
        for (item in list) {
            item.isRingtoneDefault = (item.audioFile.uri.toString() == ringtoneDefault)
        }
        return list
    }

    fun stopAudio(position: Int) {
        audioPlayer.stop()
        mAudioMediatorLiveData.value?.let {
            val listItemsCopy = ArrayList(it)
            val selectItemView = listItemsCopy.get(position).copy()
            val itemLoadStatus = selectItemView.selectItemStatus.copy()
            itemLoadStatus.playerState = PlayerState.IDLE
            selectItemView.selectItemStatus = itemLoadStatus

            listItemsCopy.set(position, selectItemView)

            mAudioMediatorLiveData.value = listItemsCopy
        }

    }


    override fun onCleared() {
        super.onCleared()
        audioPlayer.stop()
    }

    fun getAudioPlayer(): AudioPlayer {
        return audioPlayer
    }

    fun searchAudioFile(data: String) {
        _searchAudioName.value = data
    }

    fun setRingtone(phoneNumber: String): Boolean {
        mAudioMediatorLiveData.value?.let {
            if (phoneNumber != "") {
                for (audio in it) {
                    if (audio.isSelect) {
                        return ManagerFactory.getRingtoneManager()
                            .setRingToneWithContactNumberandFilePath(
                                audio.audioFile.file.absolutePath,
                                phoneNumber
                            )
                    }
                }
            }
        }

        return false
    }

    fun setRingtoneWithUri(phoneNumber: String, uri: String): Boolean {
        if (phoneNumber != "" && uri != "") {
            return ManagerFactory.getRingtoneManager()
                .setRingToneWithContactNumberAndUri(uri, phoneNumber)
        }
        return false
    }

    fun clickedOnSortButton() {
        _sortAudioValue.value?.let {
            _showSortAudioDialog.value = it
        }
    }

    fun sortAudioBy(sortValue: SortValue) {
        if (_sortAudioValue.value != sortValue) {
            _sortAudioValue.value = sortValue
        }
    }
}