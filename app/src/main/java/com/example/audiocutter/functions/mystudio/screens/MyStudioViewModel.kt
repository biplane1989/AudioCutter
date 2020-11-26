package com.example.audiocutter.functions.mystudio.screens

import android.app.Application
import android.os.Environment
import android.text.TextUtils
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.example.audiocutter.base.BaseAndroidViewModel
import com.example.audiocutter.core.audiomanager.Folder
import com.example.audiocutter.core.manager.AudioEditorManager
import com.example.audiocutter.core.manager.AudioPlayer
import com.example.audiocutter.core.manager.ManagerFactory
import com.example.audiocutter.ext.toListAudioFiles
import com.example.audiocutter.functions.mystudio.Constance
import com.example.audiocutter.functions.mystudio.ItemLoadStatus
import com.example.audiocutter.functions.mystudio.objects.ActionData
import com.example.audiocutter.functions.mystudio.objects.AudioFileView
import com.example.audiocutter.functions.mystudio.objects.DeleteState
import com.example.audiocutter.functions.resultscreen.objects.*
import com.example.audiocutter.objects.AudioFile
import com.example.audiocutter.objects.AudioFileScans
import com.example.audiocutter.objects.StateLoad
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import java.io.File

fun List<AudioFileView>.toMap(): HashMap<String, AudioFileView> {
    val hashMap = HashMap<String, AudioFileView>()
    this.forEach {
        hashMap.put(it.getFilePath(), it)
    }
    return hashMap
}

class MyStudioViewModel(application: Application) : BaseAndroidViewModel(application) {

    private val audioPlayer = ManagerFactory.newAudioPlayer()

    private val mAudioMediatorLiveData = MediatorLiveData<ArrayList<AudioFileView>>()

    private var mListAudio = ArrayList<AudioFileView>()

    private var mListScannedAudioFile = ArrayList<AudioFileView>()

    private var mListConvertingItems = ArrayList<AudioFileView>()

    private val actionLiveData: MutableLiveData<ActionData> = MutableLiveData()

    private val pathName = "${Environment.getExternalStorageDirectory()}/AudioCutter/mixer"

    private val TAG = "giangtd"

    // kiểm tra có đang ở trạng thái checkbox delete ko
    private var isDeleteStatus = false

    // kiem tra co o trang thai check all khong
    private var isCheckAllStatus = false

    private var loadingStatus: MutableLiveData<Boolean> = MutableLiveData()
    private var isEmptyStatus: MutableLiveData<Boolean> = MutableLiveData()
    private var loadingDone: MutableLiveData<Boolean> = MutableLiveData()
    private val mergingListAudioChannel = Channel<Any>(Channel.CONFLATED)
    private val viewModelScope = CoroutineScope(Dispatchers.Default)
    private val mainScope = MainScope()


    private var mergingListAudioJob: Job? = null

    init {
        audioPlayer.init(application.applicationContext)
    }

    fun init(typeAudio: Int) {

        var listScaners: LiveData<AudioFileScans> = MutableLiveData()
        Log.d(TAG, "init 1:  ${Thread.currentThread().name}")
        when (typeAudio) {
            Constance.AUDIO_CUTTER -> {
                listScaners = ManagerFactory.getAudioFileManager()
                    .getListAudioFileByType(Folder.TYPE_CUTTER)
            }
            Constance.AUDIO_MERGER -> {
                listScaners = ManagerFactory.getAudioFileManager()
                    .getListAudioFileByType(Folder.TYPE_MERGER)
            }
            Constance.AUDIO_MIXER -> {
                listScaners = ManagerFactory.getAudioFileManager()
                    .getListAudioFileByType(Folder.TYPE_MIXER)
            }
        }
        var listConvertingItems: LiveData<List<ConvertingItem>> = MutableLiveData()
        when (typeAudio) {
            Constance.AUDIO_CUTTER -> {
                listConvertingItems = ManagerFactory.getAudioEditorManager().getListCuttingItems()
            }
            Constance.AUDIO_MERGER -> {
                listConvertingItems = ManagerFactory.getAudioEditorManager().getListMergingItems()
            }
            Constance.AUDIO_MIXER -> {
                listConvertingItems = ManagerFactory.getAudioEditorManager().getListMixingItems()
            }
        }
        mAudioMediatorLiveData.addSource(listScaners) { // khi data co su thay doi thi se goi vao ham nay
            if (it.state == StateLoad.LOADING) {
                isEmptyStatus.postValue(false)
                loadingStatus.postValue(true)
            }
            if (it.state == StateLoad.LOADDONE) {       // khi loading xong thi check co data hay khong de show man hinh empty data
                loadingStatus.postValue(false)
                mListScannedAudioFile.clear()
                for (item in it.listAudioFiles) {
                    mListScannedAudioFile.add(AudioFileView(item, false, ItemLoadStatus(), ConvertingState.SUCCESS, -1, -1))
                }
                notifyMergingListAudio()
            }
        }

        mAudioMediatorLiveData.addSource(listConvertingItems) {

            mListConvertingItems.clear()
            if (!it.isEmpty()) {
                for (item in it) {
                    if (item is CuttingConvertingItem) {
                        mListConvertingItems.add(AudioFileView(AudioFile(File(item.cuttingConfig.pathFolder), item.cuttingConfig.fileName, 100), false, ItemLoadStatus(), item.state, item.percent, item.id))
                    }
                    if (item is MergingConvertingItem) {
                        mListConvertingItems.add(AudioFileView(AudioFile(File(item.mergingConfig.pathFolder), item.mergingConfig.fileName, 100), false, ItemLoadStatus(), item.state, item.percent, item.id))
                    }
                    if (item is MixingConvertingItem) {
                        mListConvertingItems.add(AudioFileView(AudioFile(File(item.mixingConfig.pathFolder), item.mixingConfig.fileName, 100), false, ItemLoadStatus(), item.state, item.percent, item.id))
                    }
                }
            }

            Log.d(TAG, "mergeList: 33 stauts: old listConvertingItems")
            Log.d(TAG, "mergeList: listConvertingItems  ${this@MyStudioViewModel}")
            notifyMergingListAudio()
        }
    }

    fun getAudioEditorManager(): AudioEditorManager {
        return ManagerFactory.getAudioEditorManager()
    }

    fun showPlayingAudio(position: Int) {
        //   khi play nhạc reset lại trạng thái các item khác
        var index = 0
        for (item in mListAudio) {
            if (index != position) {
                val newItem = item.copy()
                newItem.isExpanded = false
                val itemLoadStatus = newItem.itemLoadStatus.copy()
                newItem.itemLoadStatus = itemLoadStatus
                mListAudio[index] = newItem
                audioPlayer.stop()
            }
            index++
        }

        if (mListAudio.get(position).isExpanded) {
            val audioFileView = mListAudio.get(position).copy()
            audioFileView.isExpanded = false
            mListAudio.set(position, audioFileView)
        } else {
            val audioFileView = mListAudio.get(position).copy()
            audioFileView.isExpanded = true
            mListAudio.set(position, audioFileView)
        }

        mAudioMediatorLiveData.postValue(mListAudio)
    }

    fun getListAudioFile(): MediatorLiveData<ArrayList<AudioFileView>> {
        return mAudioMediatorLiveData
    }

    fun getAudioPlayer(): AudioPlayer {
        return audioPlayer
    }

    private suspend fun mergeList() = coroutineScope {

        Log.d(TAG, "mergeList audio 1 ${this@MyStudioViewModel} size " + mListAudio.size)
        val filePathMapItemView = mListAudio.toMap()
        val newListAudio = ArrayList<AudioFileView>()
        newListAudio.addAll(mListConvertingItems)

        val filePathMapConvertingItem = mListConvertingItems.toMap()
        val listAudioFileExcludedConvertingItems = mListScannedAudioFile.filter { !filePathMapConvertingItem.containsKey(it.getFilePath()) }
        Log.d(TAG, "mergeList audio 2 ${this@MyStudioViewModel} mListConvertingItems size " + mListConvertingItems.size + "  mListScannedAudioFile size " + mListScannedAudioFile.size)

        run lst@{
            listAudioFileExcludedConvertingItems.forEach {
                if (!isActive) {              // khi nguoi dung cancel
                    return@lst
                }
                if (filePathMapItemView.containsKey(it.getFilePath())) {
                    val audioViewItem = filePathMapItemView.get(it.getFilePath())!!
                    newListAudio.add(audioViewItem)
                } else {
                    if (isDeleteStatus) {                           // khi dang o trang thai delete
                        it.itemLoadStatus.deleteState = DeleteState.UNCHECK
                    }
//                    if (isCheckAllStatus) {
////                        it.itemLoadStatus.deleteState = DeleteState.CHECKED
//                        it.itemLoadStatus.deleteState = DeleteState.UNCHECK
//                    }
                    newListAudio.add(it)
                }
            }
        }

        if (isActive) {                 // o trang thai ton tai thi moi dc update
            mListAudio.clear()
            mListAudio.addAll(newListAudio)
            Log.d(TAG, "mergeList audio 3 ${this@MyStudioViewModel} size " + newListAudio.size + "  mListScannedAudioFile " + mListScannedAudioFile.size)

            mAudioMediatorLiveData.postValue(mListAudio)

            Log.d(TAG, "mergeList: list size: " + mListAudio.size + " ${this@MyStudioViewModel}")

            Log.d(TAG, "mergeList: mListConvertingItems.size : " + mListConvertingItems.size + " mListScannedAudioFile.size: " + mListScannedAudioFile.size)
            if (mListConvertingItems.size <= 0 && mListScannedAudioFile.size <= 0) {
                isEmptyStatus.postValue(true)
            } else {
                isEmptyStatus.postValue(false)
            }

        }


        /*  mListAudio.clear()                      //  merger cu
          if (!mListFileLoading.isNullOrEmpty()) {
              mListAudio.addAll(mListFileLoading)
          }
          if (!mListAudioFileScans.isNullOrEmpty()) {
              for (item in mListAudioFileScans) {
                  if (!isActive) {              // khi nguoi dung cancel
                      break
                  }
                  if (isDeleteStatus) {                           // khi dang o trang thai delete
                      item.itemLoadStatus.deleteState = DeleteState.UNCHECK
                  }
                  if (isCheckAllStatus) {
                      item.itemLoadStatus.deleteState = DeleteState.CHECKED
                  }
                  if (!isDoubleDisplay(item.audioFile.file.absolutePath.toString())) {
                      mListAudio.add(item)
                  }
              }
          }
          if (isActive) {                 // o trang thai ton tai thi moi dc update
              mAudioMediatorLiveData.postValue(mListAudio)

          }*/
    }

    private fun isDoubleDisplay(filePath: String): Boolean {        // kiem tra xem item o listloading co ton tai trong list scan hay khong
        for (item in mListConvertingItems) {
            if (TextUtils.equals(item.audioFile.file.absolutePath.toString() + item.audioFile.fileName + item.audioFile.mimeType, filePath)) {
                return true
            }
        }
        return false
    }

    fun getLoadingStatus(): LiveData<Boolean> {
        return loadingStatus
    }

    fun getIsEmptyStatus(): LiveData<Boolean> {
        return isEmptyStatus
    }

    fun getLoadingDone(): LiveData<Boolean> {
        return loadingDone
    }

    // xử lý button check delete
    fun checkItemPosition(pos: Int) {
        val audioFileView = mListAudio.get(pos).copy()
        if (audioFileView.itemLoadStatus.deleteState == DeleteState.UNCHECK) {
            val itemLoadStatus = audioFileView.itemLoadStatus.copy()
            itemLoadStatus.deleteState = DeleteState.CHECKED
            audioFileView.itemLoadStatus = itemLoadStatus
        } else {
            val itemLoadStatus = audioFileView.itemLoadStatus.copy()
            itemLoadStatus.deleteState = DeleteState.UNCHECK
            audioFileView.itemLoadStatus = itemLoadStatus
        }
        mListAudio[pos] = audioFileView

        mAudioMediatorLiveData.postValue(mListAudio)
    }

    // chuyển trạng thái all item -> delete status
    fun changeAutoItemToDelete() {
        val copy = ArrayList<AudioFileView>()
        mListAudio.forEach {
            val audioFileView = it.copy()
            val itemLoadStatus = audioFileView.itemLoadStatus.copy()
            itemLoadStatus.deleteState = DeleteState.UNCHECK
            audioFileView.itemLoadStatus = itemLoadStatus
            copy.add(audioFileView)
        }
        // update trang thai isDelete
        isDeleteStatus = true

        mListAudio = copy
        mAudioMediatorLiveData.postValue(mListAudio)
    }

    // chuyển trạng thái từ delete status -> more
    fun changeAutoItemToMore() {
        val copy = ArrayList<AudioFileView>()
        mListAudio.forEach {
            val audioFileView = it.copy()
            val itemLoadStatus = audioFileView.itemLoadStatus.copy()
            itemLoadStatus.deleteState = DeleteState.HIDE
            audioFileView.itemLoadStatus = itemLoadStatus
            copy.add(audioFileView)
        }
        // update trang thai undelete
        isDeleteStatus = false
        mListAudio = copy

        mAudioMediatorLiveData.postValue(mListAudio)
    }

    // check trạng thái có phải check all status ko
    fun isAllChecked(): Boolean {
        mListAudio.forEach {
            if (it.itemLoadStatus.deleteState == DeleteState.UNCHECK) {
                isCheckAllStatus = false                          // dang o trang thai all select ma du lieu loading xong
                return false
            }
        }
        isCheckAllStatus = true

        return true
    }

    fun isExitItemSelectDelete(): Boolean {
        if (!mListAudio.isEmpty() && isDeleteStatus) {
            return true
        } else {
            return false
        }
    }

    // check xem đã có ít nhất 1 item nào được check delete hay chưa
    fun isChecked(): Boolean {
        mListAudio.forEach {
            if (it.itemLoadStatus.deleteState == DeleteState.CHECKED) {
                return true
            }
        }
        return false
    }

    fun clickSelectAllBtn(): List<AudioFileView> {
        if (isAllChecked()) {
            return unselectAllItems()
        } else {
            return selectAllItems()
        }
    }

    // sự kiện clickall delete button
    private fun selectAllItems(): List<AudioFileView> {
//        TODO()
        val copy = ArrayList<AudioFileView>()
        mListAudio.forEach {
            val audioFileView = it.copy()
            val itemLoadStatus = audioFileView.itemLoadStatus.copy()
            itemLoadStatus.deleteState = DeleteState.CHECKED
            audioFileView.itemLoadStatus = itemLoadStatus
            copy.add(audioFileView)
        }
        mListAudio = copy

        return mListAudio
    }

    private fun unselectAllItems(): List<AudioFileView> {
//        TODO()
        val copy = ArrayList<AudioFileView>()
        mListAudio.forEach {
            val audioFileView = it.copy()
            val itemLoadStatus = audioFileView.itemLoadStatus.copy()
            itemLoadStatus.deleteState = DeleteState.UNCHECK
            audioFileView.itemLoadStatus = itemLoadStatus
            copy.add(audioFileView)
        }
        mListAudio = copy

        return mListAudio
    }

    suspend fun deleteAllItemSelected(typeAudio: Int): Boolean {
//        TODO()
        return runAndWaitOnBackground {
            val listAudioItems: List<AudioFile> = mListAudio.filter { it.itemLoadStatus.deleteState == DeleteState.CHECKED }        // list<AudioFile> da duoc viet dang exstent funtion
                .toListAudioFiles()
            var folder = Folder.TYPE_MIXER
            when (typeAudio) {
                0 -> {
                    folder = Folder.TYPE_CUTTER
                }
                1 -> {
                    folder = Folder.TYPE_MERGER
                }
                2 -> {
                    folder = Folder.TYPE_MIXER
                }
            }

            if (listAudioItems.any { it.getFilePath() == audioPlayer.getPlayerInfoData().currentAudio?.getFilePath() }) {       // kiem tra neu dang phat nhac thi stop laiFload
                audioPlayer.stop()
            }
            ManagerFactory.getAudioFileManager().deleteFile(listAudioItems, folder)
        }
    }

    suspend fun deleteItem(pathFolder: String, typeAudio: Int): Boolean {
//        TODO()
        val listAudioItems = ArrayList<AudioFile>()
        return runAndWaitOnBackground {
            mListAudio.forEach {
                if (TextUtils.equals(it.audioFile.file.absoluteFile.toString(), pathFolder)) {
                    listAudioItems.add(it.audioFile)
                }
            }

            var folder = Folder.TYPE_MIXER
            when (typeAudio) {
                0 -> {
                    folder = Folder.TYPE_CUTTER
                }
                1 -> {
                    folder = Folder.TYPE_MERGER
                }
                2 -> {
                    folder = Folder.TYPE_MIXER
                }
            }
            if (listAudioItems.any { it.getFilePath() == audioPlayer.getPlayerInfoData().currentAudio?.getFilePath() }) {
                audioPlayer.stop()
            }
            ManagerFactory.getAudioFileManager().deleteFile(listAudioItems, folder)
        }
    }

    // khi chuyển sang tab khác thì stop audio va reset trang thai audio
    fun stopMediaPlayerWhenTabSelect() {
        audioPlayer.stop()
        //   khi play nhạc reset lại trạng thái các item khác
        var index = 0
        for (item in mListAudio) {
            val newItem = item.copy()
            newItem.isExpanded = false
            mListAudio.set(index, newItem)
            index++
        }
        mAudioMediatorLiveData.postValue(mListAudio)
    }

    fun cancelLoading(id: Int) {
        ManagerFactory.getAudioEditorManager().cancel(id)
    }

    override fun onCleared() {
        super.onCleared()
        audioPlayer.stop()
        viewModelScope.cancel()
    }

    fun setRingTone(uri: String): Boolean {
        uri.let {
            val audioFile = getAudioFileByUri(uri)
            audioFile?.let {
                return ManagerFactory.getRingtoneManager().setRingTone(audioFile)
            }
        }
        return false
    }

    fun setAlarm(uri: String): Boolean {
        uri.let {
            val audioFile = getAudioFileByUri(uri)
            audioFile?.let {
                return ManagerFactory.getRingtoneManager().setAlarmManager(audioFile)
            }
        }
        return false
    }

    fun setNotification(uri: String): Boolean {
        uri.let {
            val audioFile = getAudioFileByUri(uri)
            audioFile?.let {
                return ManagerFactory.getRingtoneManager().setNotificationSound(audioFile)
            }
        }
        return false
    }

    private fun getAudioFileByUri(uri: String): AudioFile? {
        for (item in mListAudio) {
            if (TextUtils.equals(item.audioFile.uri.toString(), uri)) return item.audioFile
        }
        return null
    }

    fun renameAudio(newName: String, typeFolder: Folder, filePath: String) {
        val audioFile = ManagerFactory.getAudioFileManager().findAudioFile(filePath)
        audioFile?.let {
            ManagerFactory.getAudioFileManager().renameToFileAudio(newName, it, typeFolder)
        }
    }

    fun getAction(): LiveData<ActionData> {
        return actionLiveData
    }

    override fun onReceivedAction(fragmentMeta: FragmentMeta) {
        super.onReceivedAction(fragmentMeta)
        Log.d(TAG, "onReceivedAction: 1")
        fragmentMeta.data?.let {
            Log.d(TAG, "onReceivedAction: 2")
            val typeAudio = fragmentMeta.data as Int
            when (fragmentMeta.action) {
                Constance.ACTION_DELETE_STATUS -> { // trang thai isdelete
                    actionLiveData.postValue(ActionData(Constance.ACTION_DELETE_STATUS, typeAudio))
                }
                Constance.ACTION_HIDE -> {  // trang thai undelete
                    actionLiveData.postValue(ActionData(Constance.ACTION_HIDE, typeAudio))
                }
                Constance.ACTION_DELETE_ALL -> {
                    actionLiveData.postValue(ActionData(Constance.ACTION_DELETE_ALL, typeAudio))
                }
                Constance.ACTION_STOP_MUSIC -> {
                    actionLiveData.postValue(ActionData(Constance.ACTION_STOP_MUSIC, typeAudio))
                }
                Constance.ACTION_CHECK_DELETE -> {
                    actionLiveData.postValue(ActionData(Constance.ACTION_CHECK_DELETE, typeAudio))
                }
            }
        }
    }

    private fun notifyMergingListAudio() {
        mainScope.launch {
            mergingListAudioChannel.send(true)
        }
    }

    init {
        viewModelScope.launch {
            while (true && isActive) {
                val signal = mergingListAudioChannel.receive()
                mergingListAudioJob?.cancelAndJoin()

                mergingListAudioJob = viewModelScope.launch {
                    Log.d("taihhhhh", "start Merging List Audio  ${this@MyStudioViewModel}")
                    mergeList()
                    Log.d("taihhhhh", "end Merging List Audio ${this@MyStudioViewModel}")
                }
            }
        }
    }
}