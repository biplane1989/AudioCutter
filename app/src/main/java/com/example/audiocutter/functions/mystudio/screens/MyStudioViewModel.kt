package com.example.audiocutter.functions.mystudio.screens

import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.example.audiocutter.base.BaseViewModel
import com.example.audiocutter.core.ManagerFactory
import com.example.audiocutter.core.audioManager.Folder
import com.example.audiocutter.core.manager.PlayerInfo
import com.example.audiocutter.core.manager.PlayerState
import com.example.audiocutter.functions.mystudio.objects.AudioFileView
import com.example.audiocutter.functions.mystudio.Constance
import com.example.audiocutter.functions.mystudio.ItemLoadStatus
import com.example.audiocutter.functions.mystudio.objects.DeleteState
import com.example.audiocutter.functions.resultscreen.objects.ConvertingItem
import com.example.audiocutter.functions.resultscreen.objects.ConvertingState
import com.example.audiocutter.objects.AudioFile
import com.example.audiocutter.objects.AudioFileScans
import com.example.audiocutter.objects.StateLoad
import java.io.File

class MyStudioViewModel : BaseViewModel() {

    private var mListAudioFileView = ArrayList<AudioFileView>()

    private var mListLoading = ArrayList<ConvertingItem>()

    var statusResultDelete: MutableLiveData<Boolean> = MutableLiveData()
    var listDemo: MutableLiveData<List<ConvertingItem>> = MutableLiveData()
    var listDemo2: MutableLiveData<List<AudioFileView>> = MutableLiveData()

    init {
        val file = File(Environment.getExternalStorageDirectory()
            .toString() + "/Download/doihoamattroi.mp3")
        val audioFile = AudioFile(file, "hjhjhjh", 1000, 128, uri = Uri.parse(file.absolutePath))
        /* mListLoading.add(ConvertingItem(1, ConvertingState.WAITING, 1, audioFile))
         mListLoading.add(ConvertingItem(2, ConvertingState.WAITING, 1, audioFile))
         mListLoading.add(ConvertingItem(3, ConvertingState.WAITING, 1, audioFile))
         mListLoading.add(ConvertingItem(4, ConvertingState.WAITING, 1, audioFile))
         listDemo.postValue(mListLoading)*/
        mListLoading.add(ConvertingItem(4, ConvertingState.WAITING, 1, audioFile))


        mListAudioFileView.add(AudioFileView(audioFile, true, ItemLoadStatus()))
        mListAudioFileView.add(AudioFileView(audioFile, true, ItemLoadStatus()))
        mListAudioFileView.add(AudioFileView(audioFile, true, ItemLoadStatus()))
        mListAudioFileView.add(AudioFileView(audioFile, true, ItemLoadStatus()))
//        listDemo2.postValue(mListAudioFileView)

    }

    fun getStatusResultDelete(): LiveData<Boolean> {
        return statusResultDelete
    }

    val TAG = "giangtd"

    // kiểm tra có đang ở trạng thái checkbox delete ko
    var isDeleteStatus = false

    // kiểm tra playaudio đã được khởi tạo chưa
    var isPlayingStatus = false

    var loadingStatus: MutableLiveData<Boolean> = MutableLiveData()
    var isEmptyStatus: MutableLiveData<Boolean> = MutableLiveData()

    suspend fun getData(typeAudio: Int): LiveData<List<AudioFileView>> {

        /* val mAudioScaners: LiveData<AudioFileScans>

         when (typeAudio) {
             Constance.AUDIO_CUTTER -> {
                 mAudioScaners = ManagerFactory.getAudioFileManager()
                     .getListAudioFileByType(Folder.TYPE_CUTTER)
             }
             Constance.AUDIO_MERGER -> {
                 mAudioScaners = ManagerFactory.getAudioFileManager()
                     .getListAudioFileByType(Folder.TYPE_MERGER)
             }
             else -> {
                 mAudioScaners = ManagerFactory.getAudioFileManager()
                     .getListAudioFileByType(Folder.TYPE_MIXER)
             }
         }
         return Transformations.map(mAudioScaners) {
             if (it.state == StateLoad.LOADING) {
                 loadingStatus.postValue(true)
             }
             if (it.state == StateLoad.LOADDONE) {
                 loadingStatus.postValue(false)
             }
             Log.d(TAG, "StateLoad: " + it.state)

             if (it.listAudioFiles.isEmpty() && mListLoading.isEmpty()) {
                 isEmptyStatus.postValue(true)
             } else {
                 isEmptyStatus.postValue(false)
             }
             // lan dau tien lay du lieu
             if (mListAudioFileView.size == 0) {
                 it.listAudioFiles.forEach {
                     mListAudioFileView.add(AudioFileView(it))
                 }

             } else { // khi thay doi du lieu update
                 // đồng bộ hóa list cũ và mới
                 val newListAudioFileView = ArrayList<AudioFileView>()
                 it.listAudioFiles.forEach {
                     val audioFileView = getAudioFileView(it.file.absolutePath)
                     if (audioFileView != null) {
                         newListAudioFileView.add(audioFileView)
                     } else {
                         if (isDeleteStatus) {
                             val newAudioFileView = AudioFileView(it)
                             newAudioFileView.itemLoadStatus.deleteState = DeleteState.UNCHECK
                             newListAudioFileView.add(newAudioFileView)
                         } else {
                             newListAudioFileView.add(AudioFileView(it))
                         }
                     }
                 }
                 mListAudioFileView = newListAudioFileView
             }
             mListAudioFileView
         }*/

        return listDemo2
    }

    // tìm ra những file đã tồn tại trong list cũ
    private fun getAudioFileView(filePath: String): AudioFileView? {
        mListAudioFileView.forEach {
            if (it.audioFile.file.absolutePath.equals(filePath)) {
                return it
            }
        }
        return null
    }

    fun getListLoading(typeAudio: Int): LiveData<List<ConvertingItem>> {
        val listLoadingItem: LiveData<List<ConvertingItem>>

        when (typeAudio) {
            Constance.AUDIO_CUTTER -> {
                listLoadingItem = ManagerFactory.getAudioEditorManager().getListCuttingItems()
            }
            Constance.AUDIO_MERGER -> {
                listLoadingItem = ManagerFactory.getAudioEditorManager().getListMergingItems()
            }
            else -> {
                listLoadingItem = ManagerFactory.getAudioEditorManager().getListMixingItems()
            }
        }
        return Transformations.map(listLoadingItem) {
            mListLoading.clear()
            if (!it.isEmpty()) {
                it.forEach {
                    mListLoading.add(it)
                }
            }
            mListLoading
        }

//        return listDemo
    }

    fun updateLoadingEditor(convetingItem: ConvertingItem): List<ConvertingItem> {

        if (!mListLoading.isEmpty()) {

            val newItem = ConvertingItem(convetingItem.id, convetingItem.state, convetingItem.percent, convetingItem.audioFile)
            mListLoading[0] = newItem
        }
        var index = 0
        for (item in mListLoading) {
            if (item.id == convetingItem.id) {
                val newItem = ConvertingItem(item.id, item.state, item.percent, item.audioFile)
//                mListLoading.set(index, convetingItem)
                mListLoading[index] = newItem
                Log.d(TAG, "updateLoadingEditor: "+item.percent)
            }
            index++
        }
        return mListLoading
    }

    fun getLoadingStatus(): LiveData<Boolean> {
        return loadingStatus
    }

    fun getIsEmptyStatus(): LiveData<Boolean> {
        return isEmptyStatus
    }

    // xử lý button check delete
    fun checkItemPosition(pos: Int): List<AudioFileView> {
        val audioFileView = mListAudioFileView.get(pos).copy()

        if (audioFileView.itemLoadStatus.deleteState == DeleteState.UNCHECK) {
            val itemLoadStatus = audioFileView.itemLoadStatus.copy()
            itemLoadStatus.deleteState = DeleteState.CHECKED
            audioFileView.itemLoadStatus = itemLoadStatus

        } else {
            val itemLoadStatus = audioFileView.itemLoadStatus.copy()
            itemLoadStatus.deleteState = DeleteState.UNCHECK
            audioFileView.itemLoadStatus = itemLoadStatus
        }
        mListAudioFileView[pos] = audioFileView
        return mListAudioFileView
    }


    // chuyển trạng thái all item -> delete status
    fun changeAutoItemToDelete(): List<AudioFileView> {
        val copy = ArrayList<AudioFileView>()
        mListAudioFileView.forEach {
            val audioFileView = it.copy()
            val itemLoadStatus = audioFileView.itemLoadStatus.copy()
            itemLoadStatus.deleteState = DeleteState.UNCHECK
            audioFileView.itemLoadStatus = itemLoadStatus
            copy.add(audioFileView)
        }

        // update trang thai isDelete
        isDeleteStatus = true

        mListAudioFileView = copy
        return mListAudioFileView
    }

    // chuyển trạng thái từ delete status -> more
    fun changeAutoItemToMore(): List<AudioFileView> {
        val copy = ArrayList<AudioFileView>()
        mListAudioFileView.forEach {
            val audioFileView = it.copy()

            val itemLoadStatus = audioFileView.itemLoadStatus.copy()
            itemLoadStatus.deleteState = DeleteState.HIDE
            audioFileView.itemLoadStatus = itemLoadStatus

            copy.add(audioFileView)
        }

        // update trang thai undelete
        isDeleteStatus = false

        mListAudioFileView = copy
        return mListAudioFileView
    }

    // check trạng thái có phải check all status ko
    fun isAllChecked(): Boolean {
        mListAudioFileView.forEach {
            if (it.itemLoadStatus.deleteState == DeleteState.UNCHECK) {
                return false
            }
        }
        return true
    }

    // check xem đã có ít nhất 1 item nào được check delete hay chưa
    fun isChecked(): Boolean {
        mListAudioFileView.forEach {
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
        mListAudioFileView.forEach {
            val audioFileView = it.copy()

            val itemLoadStatus = audioFileView.itemLoadStatus.copy()
            itemLoadStatus.deleteState = DeleteState.CHECKED
            audioFileView.itemLoadStatus = itemLoadStatus

            copy.add(audioFileView)
        }

        mListAudioFileView = copy
        return mListAudioFileView
    }

    private fun unselectAllItems(): List<AudioFileView> {
        val copy = ArrayList<AudioFileView>()
        mListAudioFileView.forEach {
            val audioFileView = it.copy()

            val itemLoadStatus = audioFileView.itemLoadStatus.copy()
            itemLoadStatus.deleteState = DeleteState.UNCHECK
            audioFileView.itemLoadStatus = itemLoadStatus


            copy.add(audioFileView)
        }

        mListAudioFileView = copy
        return mListAudioFileView
    }

    suspend fun deleteAllItemSelected(typeAudio: Int): Boolean {

        val listAudioItems = ArrayList<AudioFile>()
        return runAndWaitOnBackground {
            var result = false
            mListAudioFileView.forEach {
                if (it.itemLoadStatus.deleteState == DeleteState.CHECKED) {
                    if (it.itemLoadStatus.playerState != PlayerState.IDLE) {
                        ManagerFactory.getAudioPlayer().stop()
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
            }
            result
        }
    }

    fun showPlayingAudio(position: Int): List<AudioFileView> {

        //   khi play nhạc reset lại trạng thái các item khác
        var index = 0
        for (item in mListAudioFileView) {
            if (index != position) {
                val newItem = item.copy()
                newItem.isExpanded = false

                val itemLoadStatus = newItem.itemLoadStatus.copy()

                newItem.itemLoadStatus = itemLoadStatus
                mListAudioFileView[index] = newItem

                // nếu audio đang playing thì stop
                runOnBackground {
                    if (isPlayingStatus) {
                        ManagerFactory.getAudioPlayer().stop()
                    }
                }
            }
            index++
        }

        if (mListAudioFileView.get(position).isExpanded) {
            val audioFileView = mListAudioFileView.get(position).copy()
            audioFileView.isExpanded = false
            mListAudioFileView.set(position, audioFileView)
        } else {
            val audioFileView = mListAudioFileView.get(position).copy()
            audioFileView.isExpanded = true
            mListAudioFileView.set(position, audioFileView)
        }
        return mListAudioFileView
    }

    // chuyen trang thai play nhac
    fun playingAudioAndchangeStatus(position: Int) {

        runOnBackground {
            ManagerFactory.getAudioPlayer().play(mListAudioFileView.get(position).audioFile)
        }

        // trang thai phat nhac
        isPlayingStatus = true
    }

    fun pauseAudioAndChangeStatus(position: Int) {

        runOnBackground {
            ManagerFactory.getAudioPlayer().pause()
        }
    }

    fun stopAudioAndChangeStatus(position: Int): List<AudioFileView> {

        runOnBackground {
            ManagerFactory.getAudioPlayer().stop()
        }
        val audioFileView = mListAudioFileView.get(position).copy()
        val itemLoadStatus = audioFileView.itemLoadStatus.copy()
        itemLoadStatus.playerState = PlayerState.IDLE
        audioFileView.itemLoadStatus = itemLoadStatus

        mListAudioFileView.set(position, audioFileView)

        return mListAudioFileView
    }

    fun resumeAudioAndChangeStatus(position: Int) {

        runOnBackground {
            ManagerFactory.getAudioPlayer().resume()
        }
    }

    fun seekToAudio(cusorPos: Int) {
        runOnBackground {
            ManagerFactory.getAudioPlayer().seek(cusorPos)
        }
    }

    fun updatePlayerInfo(playerInfo: PlayerInfo): List<AudioFileView> {
        var selectedPosition = -1
        var i = 0
        while (i < mListAudioFileView.size) {
            if (mListAudioFileView.get(i).audioFile.file.absoluteFile.equals(playerInfo.currentAudio!!.file.absoluteFile)) {
                selectedPosition = i
                break
            }
            i++
        }

        if (selectedPosition == -1) {
            //audio bi nguoi dùng xóa
            runOnBackground {
                ManagerFactory.getAudioPlayer().stop()
            }

        } else {
            val audioFileView = mListAudioFileView.get(selectedPosition).copy()
            // update
            val itemLoadStatus = audioFileView.itemLoadStatus.copy()
            itemLoadStatus.duration = playerInfo.duration
            itemLoadStatus.currPos = playerInfo.posision
            itemLoadStatus.playerState = playerInfo.playerState
            audioFileView.itemLoadStatus = itemLoadStatus

            mListAudioFileView[selectedPosition] = audioFileView

        }
        return mListAudioFileView
    }

    // khi chuyển sang tab khác thì stop audio
    fun stopMediaPlayerWhenTabSelect() {
        //   khi play nhạc reset lại trạng thái các item khác
        var index = 0
        for (item in mListAudioFileView) {

            val newItem = item.copy()
            newItem.isExpanded = false
            val itemLoadStatus = newItem.itemLoadStatus.copy()
            newItem.itemLoadStatus = itemLoadStatus
            mListAudioFileView[index] = newItem
            index++
        }
        // nếu audio đang playing thì stop
        runOnBackground {
            if (isPlayingStatus) {
                ManagerFactory.getAudioPlayer().stop()
            }
        }
    }

}