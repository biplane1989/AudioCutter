package com.example.audiocutter.functions.mystudio.screens

import android.app.Application
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Environment
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseAndroidViewModel
import com.example.audiocutter.core.manager.ManagerFactory
import com.example.audiocutter.core.audiomanager.Folder
import com.example.audiocutter.core.manager.PlayerInfo
import com.example.audiocutter.core.manager.PlayerState
import com.example.audiocutter.functions.mystudio.objects.AudioFileView
import com.example.audiocutter.functions.mystudio.Constance
import com.example.audiocutter.functions.mystudio.ItemLoadStatus
import com.example.audiocutter.functions.mystudio.dialog.DeleteSuccessfullyDialog
import com.example.audiocutter.functions.mystudio.objects.ActionData
import com.example.audiocutter.functions.mystudio.objects.DeleteState
import com.example.audiocutter.functions.resultscreen.objects.*
import com.example.audiocutter.objects.AudioFile
import com.example.audiocutter.objects.AudioFileScans
import com.example.audiocutter.objects.StateLoad
import kotlinx.android.synthetic.main.my_studio_fragment.*
import kotlinx.coroutines.delay
import java.io.File
import java.util.*

class MyStudioViewModel(application: Application) : BaseAndroidViewModel(application) {
    private val audioPlayer = ManagerFactory.newAudioPlayer()

    private val mAudioMediatorLiveData = MediatorLiveData<ArrayList<AudioFileView>>()

    private var mListAudio = ArrayList<AudioFileView>()

    private var mListAudioFileScans = ArrayList<AudioFileView>()

    private var mListFileLoading = ArrayList<AudioFileView>()

    val TAG = "giangtd"

    var isSeekBarStatus = false         // trang thai seekbar co dang duoc keo hay khong

    // kiểm tra có đang ở trạng thái checkbox delete ko
    var isDeleteStatus = false

    // kiem tra co o trang thai check all khong
    var isCheckAllStatus = false

    var loadingStatus: MutableLiveData<Boolean> = MutableLiveData()
    var isEmptyStatus: MutableLiveData<Boolean> = MutableLiveData()
    var loadingDone: MutableLiveData<Boolean> = MutableLiveData()

    init {
        audioPlayer.init(application.applicationContext)
    }

    fun init(typeAudio: Int) {
//        runOnBackground {
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
        mAudioMediatorLiveData.addSource(listScaners) {
            mListAudioFileScans.clear()
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
            for (item in it.listAudioFiles) {
                mListAudioFileScans.add(AudioFileView(item, false, ItemLoadStatus(), ConvertingState.SUCCESS, -1, -1))

            }
            mergeList()
            mAudioMediatorLiveData.postValue(mListAudio)
        }

        mAudioMediatorLiveData.addSource(listLoading) {
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
            mergeList()
            mAudioMediatorLiveData.postValue(mListAudio)
        }
        mAudioMediatorLiveData.addSource(audioPlayer.getPlayerInfo()) {
            if (!isSeekBarStatus) {
                Log.d(TAG, "init: onStopTrackingTouch updatePlayerInfo --->    isSeekBarStatus " + isSeekBarStatus)
                updatePlayerInfo(it)
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

    fun getListAudioFile(): MediatorLiveData<ArrayList<AudioFileView>> {
        return mAudioMediatorLiveData
    }

    private fun mergeList() {
        mListAudio.clear()
        if (!mListFileLoading.isNullOrEmpty()) {
            mListAudio.addAll(mListFileLoading)
            Log.d(TAG, "mergeList: mListFileLoading " + mListFileLoading.size)
        }
        if (!mListAudioFileScans.isNullOrEmpty()) {
            for (item in mListAudioFileScans) {
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
    fun updateLoadingProgressbar(newItem: ConvertingItem) {
        val newItemConverting = AudioFileView(AudioFile(File("${Environment.getExternalStorageDirectory()}/AudioCutter/mixer"), newItem.getFileName(), 100), false, ItemLoadStatus(), newItem.state, newItem.percent, newItem.id)
        if (!mListAudio.isEmpty()) {
            var index = 0
            for (item in mListAudio) {
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
    fun checkItemPosition(pos: Int): List<AudioFileView> {
        Log.d(TAG, "checkItemPosition: pos " + pos)
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
        return mListAudio
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

        Log.d(TAG, "override changeAutoItemToDelete: isDeleteStatus: " + isDeleteStatus)
        mListAudio = copy
        mAudioMediatorLiveData.postValue(mListAudio)
    }

    // chuyển trạng thái từ delete status -> more
    fun changeAutoItemToMore(): List<AudioFileView> {
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
        return mListAudio
    }

    private fun isCheckAllUpdateData(): Boolean {  // khi loading xong item kiem tra xem co o trang thai check all hay khong
        var index = 0
        while (mListAudioFileScans.size > index) {
            if (mListAudioFileScans.get(index).itemLoadStatus.deleteState == DeleteState.UNCHECK) {
                return false
            }
            index++
        }
        return true
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

        Log.d(TAG, " override isAllChecked: isCheckAllStatus " + isCheckAllStatus)
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

        val listAudioItems = ArrayList<AudioFile>()
        return runAndWaitOnBackground {
            var result: Boolean
            mListAudio.forEach {
                if (it.itemLoadStatus.deleteState == DeleteState.CHECKED) {
                    if (it.itemLoadStatus.playerState != PlayerState.IDLE) {
                        audioPlayer.stop()
                    }
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
            if (ManagerFactory.getAudioFileManager().deleteFile(listAudioItems, folder)) {
                result = true
            } else {
                result = false
            }
            result
        }
    }

    suspend fun deleteItem(pathFolder: String, typeAudio: Int): Boolean {
        val listAudioItems = ArrayList<AudioFile>()
        return runAndWaitOnBackground {
            var result = false
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
            if (ManagerFactory.getAudioFileManager().deleteFile(listAudioItems, folder)) {
                result = true
            }
            result
        }
    }

    fun showPlayingAudio(position: Int): List<AudioFileView> {

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
        return mListAudio
    }

    // chuyen trang thai play nhac
    fun playingAudioAndchangeStatus(position: Int) {
        runOnBackground {               //  truong hop ket qua cua file loadding uri se bi null
            if (mListAudio.get(position).audioFile.uri == null) {
                val uri = Uri.parse(mListAudio.get(position).audioFile.file.absolutePath)
                mListAudio.get(position).audioFile.uri = uri
            }
            audioPlayer.play(mListAudio.get(position).audioFile)
        }
    }

    fun pauseAudioAndChangeStatus(position: Int) {
        audioPlayer.pause()
    }

    fun stopAudioAndChangeStatus(position: Int): List<AudioFileView> {
        audioPlayer.stop()

        val audioFileView = mListAudio.get(position).copy()
        val itemLoadStatus = audioFileView.itemLoadStatus.copy()
        itemLoadStatus.playerState = PlayerState.IDLE
        audioFileView.itemLoadStatus = itemLoadStatus

        mListAudio.set(position, audioFileView)
        return mListAudio
    }

    // khi chuyển sang tab khác thì stop audio
    fun stopMediaPlayerWhenTabSelect() {
        //   khi play nhạc reset lại trạng thái các item khác
        var index = 0
        for (item in mListAudio) {
            val newItem = item.copy()
            newItem.isExpanded = false
            val itemLoadStatus = newItem.itemLoadStatus.copy()
            newItem.itemLoadStatus = itemLoadStatus
            mListAudio[index] = newItem
            index++
        }
        audioPlayer.stop()
    }

    fun resumeAudioAndChangeStatus(position: Int) {
        audioPlayer.resume()
    }

    fun seekToAudio(cusorPos: Int) {
        audioPlayer.seek(cusorPos)
        isSeekBarStatus = false
    }

    fun startSeekBar() {
        isSeekBarStatus = true
    }

    private fun updatePlayerInfo(playerInfo: PlayerInfo) {
        var selectedPosition = -1
        var i = 0
        while (i < mListAudio.size) {
            if (mListAudio.get(i).audioFile.file.absoluteFile.equals(playerInfo.currentAudio!!.file.absoluteFile)) {
                selectedPosition = i
                break
            }
            i++
        }

        if (selectedPosition == -1) {
            audioPlayer.stop()
        } else {
            val audioFileView = mListAudio.get(selectedPosition).copy()
            // update
            val itemLoadStatus = audioFileView.itemLoadStatus.copy()
            itemLoadStatus.duration = playerInfo.duration
            itemLoadStatus.currPos = playerInfo.posision
            itemLoadStatus.playerState = playerInfo.playerState
            audioFileView.itemLoadStatus = itemLoadStatus

            mListAudio[selectedPosition] = audioFileView
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

    fun getAudioFileByUri(uri: String): AudioFile? {
        for (item in mListAudio) {
            if (TextUtils.equals(item.audioFile.uri.toString(), uri)) return item.audioFile
        }
        return null
    }

    fun renameAudio(newName: String, typeFolder: Folder, filePath: String) {
        val audioFile = ManagerFactory.getAudioFileManager().buildAudioFile(filePath)
        ManagerFactory.getAudioFileManager().reNameToFileAudio(newName, audioFile, typeFolder)

    }

    val actionLiveData: MutableLiveData<ActionData> = MutableLiveData()

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