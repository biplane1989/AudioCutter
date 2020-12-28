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
import java.lang.reflect.Array
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

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
    private var listScansIsEmptyStatus = false
    private var listConvertingIsEmptyStatus = false

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

            listScansIsEmptyStatus = it.listAudioFiles.size <= 0
            Log.d(
                TAG,
                "init: update data: listScaners ${this@MyStudioViewModel} :" + it.listAudioFiles.size + " status: " + listScansIsEmptyStatus
            )

            mListScannedAudioFile.clear()

            if (it.state == StateLoad.LOADING) {
                isEmptyStatus.postValue(false)
                loadingStatus.postValue(true)
                loadingDone.postValue(false)
            }
            if (it.state == StateLoad.LOADDONE) {       // khi loading xong thi check co data hay khong de show man hinh empty data
                loadingStatus.postValue(false)
                loadingDone.postValue(true)
                for (item in it.listAudioFiles) {
                    mListScannedAudioFile.add(
                        AudioFileView(
                            item,
                            false,
                            ItemLoadStatus(),
                            ConvertingState.SUCCESS,
                            -1,
                            -1
                        )
                    )
                }
                notifyMergingListAudio()
            }

            Log.d(TAG, "bug tiem an scaners list size : " + it.listAudioFiles.size)
            Log.d(TAG, "init: addSource(listScaners)")
        }

        mAudioMediatorLiveData.addSource(listConvertingItems) {
            listConvertingIsEmptyStatus = it.size <= 0
            mListConvertingItems.clear()
            if (!it.isEmpty()) {
                for (item in it) {
                    if (item is CuttingConvertingItem) {
                        val fileCutting = File(
                            item.cuttingConfig.pathFolder + "/" + item.cuttingConfig.fileName + "." + item.cuttingConfig.format.toString()
                                .toLowerCase(Locale.ROOT)
                        )
                        mListConvertingItems.add(
                            AudioFileView(
                                AudioFile(
                                    fileCutting,
                                    item.cuttingConfig.fileName,
                                    100
                                ), false, ItemLoadStatus(), item.state, item.percent, item.id
                            )
                        )
                    }
                    if (item is MergingConvertingItem) {
                        val fileConverting = File(
                            item.mergingConfig.pathFolder + "/" + item.mergingConfig.fileName + "." + item.mergingConfig.audioFormat.toString()
                                .toLowerCase(Locale.ROOT)
                        )
                        mListConvertingItems.add(
                            AudioFileView(
                                AudioFile(
                                    fileConverting,
                                    item.mergingConfig.fileName,
                                    100
                                ), false, ItemLoadStatus(), item.state, item.percent, item.id
                            )
                        )
                    }
                    if (item is MixingConvertingItem) {
                        val fileMixing = File(
                            item.mixingConfig.pathFolder + "/" + item.mixingConfig.fileName + "." + item.mixingConfig.format.toString()
                                .toLowerCase(Locale.ROOT)
                        )
                        mListConvertingItems.add(
                            AudioFileView(
                                AudioFile(
                                    fileMixing,
                                    item.mixingConfig.fileName,
                                    100
                                ), false, ItemLoadStatus(), item.state, item.percent, item.id
                            )
                        )
                    }
                }
            }
            Log.d(TAG, "bug tiem an converting list size : " + it.size)
            notifyMergingListAudio()
            Log.d(TAG, "init: addSource(listConvertingItems)")
        }
    }

    fun getAudioEditorManager(): AudioEditorManager {
        return ManagerFactory.getAudioEditorManager()
    }

    private suspend fun mergeList() = coroutineScope {

        for (item in mListConvertingItems) {
            Log.d(TAG, "mergeList: 002 : " + item.getFilePath())
        }

        val filePathMapConvertingItem = mListConvertingItems.toMap()
        val filePathMapItemView = mListAudio.toMap()
        val newListAudio = ArrayList<AudioFileView>()
        newListAudio.addAll(mListConvertingItems)

        val listAudioFileExcludedConvertingItems =
            mListScannedAudioFile.filter { !filePathMapConvertingItem.containsKey(it.getFilePath()) }

        Log.d(TAG, "bug tiem an: list converting : " + filePathMapConvertingItem.size)
        Log.d(TAG, "bug tiem an: list scanner: " + listAudioFileExcludedConvertingItems.size)
        for (item in listAudioFileExcludedConvertingItems) {
            Log.d(
                TAG,
                "bug tiem an: status : " + item.convertingState + " filePath: " + item.getFilePath() + " id: " + item.id
            )
        }
        Log.d(
            TAG,
            "mergeList: convertingItems size ${mListConvertingItems.size} mListScannedAudioFile size ${mListScannedAudioFile.size}"
        )

        run lst@{
            listAudioFileExcludedConvertingItems.forEach {
                if (!isActive) {              // khi nguoi dung cancel
                    return@lst
                }
//                if (it.convertingState in arrayListOf(ConvertingState.WAITING, ConvertingState.PROGRESSING) && filePathMapConvertingItem.containsKey(it.getFilePath()) && filePathMapItemView.containsKey(it.getFilePath())) {
                if (filePathMapItemView.containsKey(it.getFilePath())) {
                    filePathMapItemView.get(it.getFilePath())?.let {
                        if (it.convertingState == ConvertingState.SUCCESS) {
                            val audioViewItem = filePathMapItemView.get(it.getFilePath())
                            audioViewItem?.let {
                                newListAudio.add(audioViewItem.copy())
                            }
                            Log.d(TAG, "mergeList: aloha 1")
                        } else {
                            if (isDeleteStatus) {                           // khi dang o trang thai delete
                                it.itemLoadStatus.deleteState = DeleteState.UNCHECK
                            }
                            newListAudio.add(it.copy())
                            Log.d(TAG, "mergeList: aloha 2")
                        }
                    }

                } else {
                    if (isDeleteStatus) {                           // khi dang o trang thai delete
                        it.itemLoadStatus.deleteState = DeleteState.UNCHECK
                    }
                    newListAudio.add(it.copy())
                    Log.d(TAG, "mergeList: aloha 3")
                }

            }
        }


        if (isActive) {                 // o trang thai ton tai thi moi dc update
            mListAudio.clear()
            mListAudio.addAll(newListAudio)
            mAudioMediatorLiveData.postValue(mListAudio)
            notificationIsEmptyStatus()
        }
    }

    private fun notificationIsEmptyStatus() {
        if (mListConvertingItems.size <= 0 && mListScannedAudioFile.size <= 0) {
            isEmptyStatus.postValue(true)
        } else {
            isEmptyStatus.postValue(false)
        }
    }

    private fun isDoubleDisplay(filePath: String): Boolean {        // kiem tra xem item o listloading co ton tai trong list scan hay khong
        for (item in mListConvertingItems) {
//            if (TextUtils.equals(item.audioFile.file.absolutePath.toString() + item.audioFile.fileName + item.audioFile.mimeType, filePath)) {
            if (TextUtils.equals(item.getFilePath(), filePath)) {
                Log.d(TAG, "isDoubleDisplay: trueeeeee")
                return true
            }
            Log.d(
                TAG,
                "isDoubleDisplay: item.getFilePath() : \n" + item.getFilePath() + "\n :  filePath " + filePath
            )

        }
        return false
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
                isCheckAllStatus =
                    false                          // dang o trang thai all select ma du lieu loading xong
                return false
            }
        }
        isCheckAllStatus = true

        return true
    }

    fun isExitItemSelectDelete(): Boolean {
        return !mListAudio.isEmpty() && isDeleteStatus
    }

    // check xem đã có ít nhất 1 item nào được check delete hay chưa
    fun isChecked(): Boolean {
        mListAudio.forEach {
            if (it.itemLoadStatus.deleteState == DeleteState.CHECKED && it.convertingState == ConvertingState.SUCCESS) {
                return true
            }
        }
        return false
    }

    fun getListShare(): List<String> {
        val listShare = ArrayList<String>()
        for (item in mListAudio) {
            if (item.itemLoadStatus.deleteState == DeleteState.CHECKED) {
                listShare.add(item.audioFile.uri.toString())
            }
        }
        return listShare
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
            val listAudioItems: List<AudioFile> =
                mListAudio.filter { it.itemLoadStatus.deleteState == DeleteState.CHECKED }        // list<AudioFile> da duoc viet dang exstent funtion
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

    fun shareAllFile(typeAudio: Int) {
        val listAudioItems: List<AudioFile> =
            mListAudio.filter { it.itemLoadStatus.deleteState == DeleteState.CHECKED }        // list<AudioFile> da duoc viet dang exstent funtion
                .toListAudioFiles()


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

    fun renameAudio(newName: String, typeFolder: Folder, filePath: String): Boolean {
        val audioFile = ManagerFactory.getAudioFileManager().findAudioFile(filePath)
        audioFile?.let {
            return ManagerFactory.getAudioFileManager().renameToFileAudio(newName, it, typeFolder)
        }
        return false
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
                Constance.ACTION_STOP_MUSIC -> {
                    actionLiveData.postValue(ActionData(Constance.ACTION_STOP_MUSIC, typeAudio))
                }
                Constance.ACTION_DELETE_ALL -> {
                    actionLiveData.postValue(ActionData(Constance.ACTION_DELETE_ALL, typeAudio))
                }
                Constance.ACTION_CHECK_DELETE -> {
                    actionLiveData.postValue(ActionData(Constance.ACTION_CHECK_DELETE, typeAudio))
                }
                Constance.ACTION_SHARE -> {
                    actionLiveData.postValue(ActionData(Constance.ACTION_SHARE, typeAudio))
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