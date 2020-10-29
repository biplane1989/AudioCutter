package com.example.audiocutter.functions.mystudio.screens

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.example.audiocutter.base.BaseViewModel
import com.example.audiocutter.core.manager.ManagerFactory
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

class MyStudioViewModel : BaseViewModel() {

    private val mAudioMediatorLiveData = MediatorLiveData<ArrayList<AudioFileView>>()

    private var mListAudio = ArrayList<AudioFileView>()

    private var mListAudioFileScans = ArrayList<AudioFileView>()

    private var mListFileLoading = ArrayList<AudioFileView>()

    val TAG = "giangtd"

    // kiểm tra có đang ở trạng thái checkbox delete ko
    var isDeleteStatus = false

    // kiểm tra playaudio đã được khởi tạo chưa
    var isPlayingStatus = false

    var loadingStatus: MutableLiveData<Boolean> = MutableLiveData()
    var isEmptyStatus: MutableLiveData<Boolean> = MutableLiveData()

    fun getListAudioFile(typeAudio: Int): MediatorLiveData<ArrayList<AudioFileView>> {
        val listScaners: LiveData<AudioFileScans>
        when (typeAudio) {
            Constance.AUDIO_CUTTER -> {
                listScaners = ManagerFactory.getAudioFileManager()
                    .getListAudioFileByType(Folder.TYPE_CUTTER)
            }
            Constance.AUDIO_MERGER -> {
                listScaners = ManagerFactory.getAudioFileManager()
                    .getListAudioFileByType(Folder.TYPE_MERGER)
            }
            else -> {
                listScaners = ManagerFactory.getAudioFileManager()
                    .getListAudioFileByType(Folder.TYPE_MIXER)
            }
        }
        val listLoading: LiveData<List<ConvertingItem>>
        when (typeAudio) {
            Constance.AUDIO_CUTTER -> {
                listLoading = ManagerFactory.getAudioEditorManager().getListCuttingItems()
            }
            Constance.AUDIO_MERGER -> {
                listLoading = ManagerFactory.getAudioEditorManager().getListMergingItems()
            }
            else -> {
                listLoading = ManagerFactory.getAudioEditorManager().getListMixingItems()
            }
        }
        mAudioMediatorLiveData.addSource(listScaners) {
            mListAudioFileScans.clear()

//            if (it.state == StateLoad.LOADING) {
//                loadingStatus.postValue(true)
//            }
//            if (it.state == StateLoad.LOADDONE) {
//                loadingStatus.postValue(false)
//            }
            // lan dau tien lay du lieu
            if (mListAudioFileScans.size == 0) {
                for (item in it.listAudioFiles) {
                    if (isDeleteStatus) {
                        val newAudioFileView = AudioFileView(item, false, ItemLoadStatus(), ConvertingState.SUCCESS, -1, -1)
                        newAudioFileView.itemLoadStatus.deleteState = DeleteState.UNCHECK
                        mListAudioFileScans.add(newAudioFileView)
                    } else {
                        mListAudioFileScans.add(AudioFileView(item, false, ItemLoadStatus(), ConvertingState.SUCCESS, -1, -1))
                    }
//                    mListAudioFileScans.add(AudioFileView(item, false, ItemLoadStatus(), ConvertingState.SUCCESS, -1, -1))
                }
            } else {
                /*  // khi thay doi du lieu update đồng bộ hóa list cũ và mới
                  val newListAudioFileView = ArrayList<AudioFileView>()
                  for (item in it.listAudioFiles) {
                      val audioFileView = getAudioFileView(item.file.absolutePath)
                      if (audioFileView != null) {
                          newListAudioFileView.add(audioFileView)
                      } else {
                          if (isDeleteStatus) {
                              val newAudioFileView = AudioFileView(item, false, ItemLoadStatus(), ConvertingState.SUCCESS, -1, -1)
                              newAudioFileView.itemLoadStatus.deleteState = DeleteState.UNCHECK
                              newListAudioFileView.add(newAudioFileView)
                          } else {
                              newListAudioFileView.add(AudioFileView(item, false, ItemLoadStatus(), ConvertingState.SUCCESS, -1, -1))
                          }
                      }
                  }
                  for (item in newListAudioFileView) {
                      mListAudioFileScans.add(item)
                  }*/
            }
            mergeList()
            mAudioMediatorLiveData.postValue(mListAudio)
        }

        mAudioMediatorLiveData.addSource(listLoading) {
            mListFileLoading.clear()
            if (!it.isEmpty()) {
                for (item in it) {
//                    if (isDeleteStatus) {
//                        val newAudioFileView = AudioFileView(item.audioFile, false, ItemLoadStatus(), item.state, -1, item.id)
//                        newAudioFileView.itemLoadStatus.deleteState = DeleteState.UNCHECK
//                        mListFileLoading.add(newAudioFileView)
//                    } else {
//                        mListFileLoading.add(AudioFileView(item.audioFile, false, ItemLoadStatus(), item.state, -1, item.id))
//                    }
                    //mListFileLoading.add(AudioFileView(item.audioFile, false, ItemLoadStatus(), item.state, item.percent, item.id)) // TODO
                }
            }
            mergeList()
            mAudioMediatorLiveData.postValue(mListAudio)
        }
        return mAudioMediatorLiveData
    }

    private fun mergeList() {
        mListAudio.clear()
        if (!mListFileLoading.isNullOrEmpty()) {
            mListAudio.addAll(mListFileLoading)
        }
        if (!mListAudioFileScans.isNullOrEmpty()) {
            mListAudio.addAll(mListAudioFileScans)
        }

        if (!mListAudio.isEmpty()) {
            isEmptyStatus.postValue(false)
        } else {
            isEmptyStatus.postValue(true)
        }
    }

    // tìm ra những file đã tồn tại trong list cũ
    private fun getAudioFileView(filePath: String): AudioFileView? {
        mListAudio.forEach {
            if (it.audioFile.file.absolutePath.equals(filePath)) {
                return it
            }
        }
        return null
    }

    // update loading item khi editor
    fun updateLoadingProgressbar(newItem: ConvertingItem): List<AudioFileView> {
        TODO()
       /* var newItemConverting = AudioFileView(newItem.audioFile, false, ItemLoadStatus(), newItem.state, newItem.percent, newItem.id)
        if (!mListAudio.isEmpty()) {
            var index = 0
            for (item in mListAudio) {
                if (item.id == newItem.id) {
                    mListAudio[index] = newItemConverting
                }
                index++
            }
        }*/
      /*  if (!mListFileLoading.isNullOrEmpty()) {
            var index = 0
            for (item in mListFileLoading) {
                if (item.id == newItem.id) {
                    mListFileLoading[index] = newItemConverting
                }
            }
        }*/
        return mListAudio
    }

    fun getLoadingStatus(): LiveData<Boolean> {
        return loadingStatus
    }

    fun getIsEmptyStatus(): LiveData<Boolean> {
        return isEmptyStatus
    }

    // xử lý button check delete
    fun checkItemPosition(pos: Int): List<AudioFileView> {
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
    fun changeAutoItemToDelete(): List<AudioFileView> {
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
        return mListAudio
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

    // check trạng thái có phải check all status ko
    fun isAllChecked(): Boolean {
        mListAudio.forEach {
            if (it.itemLoadStatus.deleteState == DeleteState.UNCHECK) {
                return false
            }
        }
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
            var result = false
            mListAudio.forEach {
//            mListAudioFileScans.forEach {
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
        for (item in mListAudio) {
            if (index != position) {
                val newItem = item.copy()
                newItem.isExpanded = false
                val itemLoadStatus = newItem.itemLoadStatus.copy()
                newItem.itemLoadStatus = itemLoadStatus
                mListAudio[index] = newItem

                // nếu audio đang playing thì stop
                runOnBackground {
                    if (isPlayingStatus) {
                        ManagerFactory.getAudioPlayer().stop()
                    }
                }
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
        runOnBackground {
            ManagerFactory.getAudioPlayer().play(mListAudio.get(position).audioFile)
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
        val audioFileView = mListAudio.get(position).copy()
        val itemLoadStatus = audioFileView.itemLoadStatus.copy()
        itemLoadStatus.playerState = PlayerState.IDLE
        audioFileView.itemLoadStatus = itemLoadStatus

        mListAudio.set(position, audioFileView)
        return mListAudio
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
        while (i < mListAudio.size) {
            if (mListAudio.get(i).audioFile.file.absoluteFile.equals(playerInfo.currentAudio!!.file.absoluteFile)) {
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
            val audioFileView = mListAudio.get(selectedPosition).copy()
            // update
            val itemLoadStatus = audioFileView.itemLoadStatus.copy()
            itemLoadStatus.duration = playerInfo.duration
            itemLoadStatus.currPos = playerInfo.posision
            itemLoadStatus.playerState = playerInfo.playerState
            audioFileView.itemLoadStatus = itemLoadStatus

            mListAudio[selectedPosition] = audioFileView
        }
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
        // nếu audio đang playing thì stop
        runOnBackground {
            if (isPlayingStatus) {
                ManagerFactory.getAudioPlayer().stop()
            }
        }
    }

    fun cancelLoading(id: Int) {
        ManagerFactory.getAudioEditorManager().cancel(id)
    }

}