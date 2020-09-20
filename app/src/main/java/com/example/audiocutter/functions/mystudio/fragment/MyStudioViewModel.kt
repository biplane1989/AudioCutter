package com.example.audiocutter.functions.mystudio.fragment

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.example.audiocutter.base.BaseViewModel
import com.example.audiocutter.core.ManagerFactory
import com.example.audiocutter.core.manager.PlayerInfo
import com.example.audiocutter.core.manager.PlayerState
import com.example.audiocutter.functions.mystudio.AudioFileView
import com.example.audiocutter.functions.mystudio.Constance
import com.example.audiocutter.functions.mystudio.DeleteState
import com.example.audiocutter.objects.AudioFile

class MyStudioViewModel : BaseViewModel() {

    private var mListAudioFileView = ArrayList<AudioFileView>()

    val TAG = "giangtd"

    // kiểm tra có đang ở trạng thái checkbox delete ko
    var isDeleteStatus = false

    // kiểm tra playaudio đã được khởi tạo chưa
    var isPlayingStatus = false

    suspend fun getData(typeAudio: Int): LiveData<List<AudioFileView>> {
        val listAudioFiles: LiveData<List<AudioFile>>
        when (typeAudio) {
            Constance.AUDIO_CUTTER -> {

                listAudioFiles = ManagerFactory.getAudioFileManager().getListAudioCutter()
                Log.d(TAG, "getData: AUDIO_CUTTER : " + typeAudio)
            }
            Constance.AUDIO_MERGER -> {
                listAudioFiles = ManagerFactory.getAudioFileManager().getListAudioMerger()
                Log.d(TAG, "getData: AUDIO_MERGER : " + typeAudio)
            }
            else -> {
                listAudioFiles = ManagerFactory.getAudioFileManager().getListAudioMixer()
                Log.d(TAG, "getData: AUDIO_Mixer: " + typeAudio)
            }
        }
        return Transformations.map(
            listAudioFiles
        ) { items ->
            // lan dau tien lay du lieu
            if (mListAudioFileView.size == 0) {
                items.forEach {
                    mListAudioFileView.add(AudioFileView(it))
                }

            } else { // khi thay doi du lieu update
                // đồng bộ hóa list cũ và mới
                val newListAudioFileView = ArrayList<AudioFileView>()
                items.forEach {
                    val audioFileView = getAudioFileView(it.file.absolutePath)
                    if (audioFileView != null) {
                        newListAudioFileView.add(audioFileView)
                    } else {
                        if (isDeleteStatus) {
                            val newAudioFileView = AudioFileView(it)
                            newAudioFileView.itemLoadStatus.deleteState =
                                DeleteState.UNCHECK
                            newListAudioFileView.add(newAudioFileView)


                        } else {
                            newListAudioFileView.add(AudioFileView(it))
                        }
                    }
                }
                mListAudioFileView = newListAudioFileView
            }
            mListAudioFileView
        }
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

    fun deleteAllItemSelected() {
        val listAudioItems = ArrayList<AudioFile>()
        runOnBackground {
            mListAudioFileView.forEach {
                if (it.itemLoadStatus.deleteState == DeleteState.CHECKED) {
                    if (it.itemLoadStatus.playerState != PlayerState.IDLE) {
                        ManagerFactory.getAudioPlayer().stop()
                    }
                    listAudioItems.add(it.audioFile)
                }
            }
            ManagerFactory.getAudioFileManager().deleteFile(listAudioItems)
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

    fun stopAudioAndChangeStatus(position: Int) {

        runOnBackground {
            ManagerFactory.getAudioPlayer().stop()
        }
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
            itemLoadStatus.currPos = playerInfo.position
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