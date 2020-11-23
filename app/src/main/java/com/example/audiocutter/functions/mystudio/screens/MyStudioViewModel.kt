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
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.isActive
import java.io.File
import kotlin.collections.ArrayList

class MyStudioViewModel(application: Application) : BaseAndroidViewModel(application) {

    private val audioPlayer = ManagerFactory.newAudioPlayer()

    private val loadingProcessingItem = ManagerFactory.getAudioEditorManager()
        .getCurrentProcessingItem()

    private val mAudioMediatorLiveData = MediatorLiveData<ArrayList<AudioFileView>>()

    private var mListAudio = ArrayList<AudioFileView>()

    private var mListAudioFileScans = ArrayList<AudioFileView>()

    private var mListFileLoading = ArrayList<AudioFileView>()

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

    private var mergingJob: Job? = null

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
        var listLoading: LiveData<List<ConvertingItem>> = MutableLiveData()
        when (typeAudio) {
            Constance.AUDIO_CUTTER -> {
                listLoading = ManagerFactory.getAudioEditorManager().getListCuttingItems()
            }
            Constance.AUDIO_MERGER -> {
                listLoading = ManagerFactory.getAudioEditorManager().getListMergingItems()
            }
            Constance.AUDIO_MIXER -> {
                listLoading = ManagerFactory.getAudioEditorManager().getListMixingItems()
            }
        }
        mAudioMediatorLiveData.addSource(listScaners) { // hi data co su thay doi thi se goi vao ham nay
            waitingForMergingJobCompleted {
                mergingJob = runOnBackground {

                    if (it.state == StateLoad.LOADING) {
                        isEmptyStatus.postValue(false)
                        loadingStatus.postValue(true)
                    }
                    if (it.state == StateLoad.LOADDONE) {       // khi loading xong thi check co data hay khong de show man hinh empty data
                        loadingStatus.postValue(false)
                        if (!it.listAudioFiles.isEmpty()) {
                            isEmptyStatus.postValue(false)
                        } else {
                            isEmptyStatus.postValue(true)
                        }
                    }

                    mListAudioFileScans.clear()
                    for (item in it.listAudioFiles) {
                        mListAudioFileScans.add(AudioFileView(item, false, ItemLoadStatus(), ConvertingState.SUCCESS, -1, -1))
                    }
                    mergeList()
                }
            }


        }

        mAudioMediatorLiveData.addSource(listLoading) {
            waitingForMergingJobCompleted {
                mergingJob = runOnBackground {
                    mListFileLoading.clear()
                    if (!it.isEmpty()) {
                        for (item in it) {
                            if (item is CuttingConvertingItem) {
                                mListFileLoading.add(AudioFileView(AudioFile(File(item.cuttingConfig.pathFolder), item.cuttingConfig.fileName, 100), false, ItemLoadStatus(), item.state, item.percent, item.id))
                            }
                            if (item is MergingConvertingItem) {
                                mListFileLoading.add(AudioFileView(AudioFile(File(item.mergingConfig.pathFolder), item.mergingConfig.fileName, 100), false, ItemLoadStatus(), item.state, item.percent, item.id))
                            }
                            if (item is MixingConvertingItem) {
                                mListFileLoading.add(AudioFileView(AudioFile(File(item.mixingConfig.pathFolder), item.mixingConfig.fileName, 100), false, ItemLoadStatus(), item.state, item.percent, item.id))
                            }
                        }
                    }
                    if (it.size > 0) {
                        isEmptyStatus.postValue(false)
                    }
                    mergeList()
                    Log.d("001", "init: listLoading : " + Thread.currentThread())
                }
            }
        }
        mAudioMediatorLiveData.addSource(ManagerFactory.getAudioEditorManager()
            .getCurrentProcessingItem()) {
            if (it != null) {
                updateLoadingProgressbar(it)
            }
        }
//        }
    }

    private fun waitingForMergingJobCompleted(pendingFunction: () -> Unit) {        // khi mergingJob da chay xong thi moi chay ham pendingFunction
        runOnBackground {
            mergingJob?.cancelAndJoin()
            pendingFunction()
        }
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

    fun getLoadingProcessingItem(): LiveData<ConvertingItem> {
        return loadingProcessingItem
    }

    private suspend fun mergeList() = coroutineScope {
        mListAudio.clear()                      //  merger cu
        if (!mListFileLoading.isNullOrEmpty()) {
            mListAudio.addAll(mListFileLoading)
        }
        if (!mListAudioFileScans.isNullOrEmpty()) {
            for (item in mListAudioFileScans) {
                if (!isActive) {                // khi nguoi dung cancel
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
        }
        Log.d("001", "init: mergeList : " + Thread.currentThread())
    }

    private fun isDoubleDisplay(filePath: String): Boolean {        // kiem tra xem item o listloading co ton tai trong list scan hay khong
        for (item in mListFileLoading) {
            if (TextUtils.equals(item.audioFile.file.absolutePath.toString() + item.audioFile.fileName + item.audioFile.mimeType, filePath)) {
                return true
            }
        }
        return false
    }

    // update loading item khi editor
    private fun updateLoadingProgressbar(newItem: ConvertingItem) {

        Log.d(TAG, "updateLoadingProgressbar: pathName : " + newItem.getFileName() + " percent : " + newItem.percent + "id : "+ newItem.id)
        val newItemConverting = AudioFileView(AudioFile(File(pathName), newItem.getFileName(), 100), false, ItemLoadStatus(), newItem.state, newItem.percent, newItem.id)
        if (!mListAudio.isEmpty()) {
            var index = 0
            for (item in mListAudio) {              // TODO
                Log.d(TAG, "updateLoadingProgressbar: id "+ item.id + " new ID : "+ newItem.id)
                if (item.id == newItem.id) {
                    mListAudio[index] = newItemConverting
                }
                index++
            }
        }
        if (!mListFileLoading.isNullOrEmpty()) {
            val index = 0
            for (item in mListFileLoading) {
                if (item.id == newItem.id) {
                    mListFileLoading[index] = newItemConverting
                }
            }
        }
        mAudioMediatorLiveData.postValue(mListAudio)

        if (newItem.state == ConvertingState.SUCCESS) {
            loadingDone.postValue(true)
        } else {
            loadingDone.postValue(false)
        }
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
                Constance.ACTION_UNCHECK -> { // trang thai isdelete
                    actionLiveData.postValue(ActionData(Constance.ACTION_UNCHECK, typeAudio))
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

}