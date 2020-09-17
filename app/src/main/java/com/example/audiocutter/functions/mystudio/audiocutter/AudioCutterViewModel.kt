package com.example.audiocutter.functions.mystudio.audiocutter

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.example.audiocutter.base.BaseViewModel
import com.example.audiocutter.core.ManagerFactory
import com.example.audiocutter.core.manager.PlayerInfo
import com.example.audiocutter.core.manager.PlayerState
import com.example.audiocutter.functions.mystudio.AudioFileView
import com.example.audiocutter.functions.mystudio.DeleteState
import com.example.audiocutter.objects.AudioFile

class AudioCutterViewModel : BaseViewModel() {

    private var mListAudioFileView = ArrayList<AudioFileView>()

    // kiểm tra có đang ở trạng thái checkbox delete ko
    var isDeleteStatus = false

    // kiểm tra nhạc có đang chạy hoặc pause ko để update seekbar và timelife
    var isPlayingStatus = false

    // kiểm tra lần đầu phát nhạc có tồn tại ko
    var mediaFirstPlay = false

    // vị trí cuối cùng được playing trước update data
    var positionLastPlaying = -1

    // vị trí mới của bài hát đang playing
    var OldselectedPosition = -1


    suspend fun getData(): LiveData<List<AudioFileView>> {

        var oldAudioFileView: AudioFileView

        return Transformations.map(
            ManagerFactory.getAudioFileManager().getListAudioCutter()
        ) { listAudioFiles ->
            val listAudioFileView = ArrayList<AudioFileView>()
            // lan dau tien lay du lieu
            if (mListAudioFileView.size == 0) {
                listAudioFiles.forEach {
                    listAudioFileView.add(AudioFileView(it))
                }
            } else { // khi thay doi du lieu update

//                nếu đang có 1 audio playing thì gán cho biến mới
//                if (positionLastPlaying > -1) {

//                    oldAudioFileView = mListAudioFileView.get(positionLastPlaying).copy()
//                }

                if (isDeleteStatus) {
                    var index = 0
                    listAudioFiles.forEach {
                        var audioFileView = AudioFileView(it)

                        // nếu tồn tại audio cũ -> gán lại trạng thái của cũ -> mới  NOTE: luôn luôn chỉ có 1 audio playing nhạc duy nhất
                        if (index == OldselectedPosition) {  // selectedPosition là vị trí mới mà audio đang playing
//                            audioFileView = oldAudioFileView
                            audioFileView.isExpanded = true
                        } else {
                            val itemLoadStatus = audioFileView.itemLoadStatus.copy()
                            itemLoadStatus.deleteState = DeleteState.UNCHECK
                            audioFileView.itemLoadStatus = itemLoadStatus
                        }
                        listAudioFileView.add(audioFileView)


                        index++
                    }
                } else {
                    listAudioFiles.forEach {
                        listAudioFileView.add(AudioFileView(it))
                    }
                }
            }
            mListAudioFileView = listAudioFileView
            mListAudioFileView
        }
    }

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
        mListAudioFileView.forEach {
            if (it.itemLoadStatus.deleteState == DeleteState.CHECKED) {
                listAudioItems.add(it.audioFile)
            }
        }
        runOnBackground {
            ManagerFactory.getAudioFileManager().deleteFile(listAudioItems)
        }
    }

    fun showPlayingAudio(position: Int): List<AudioFileView> {
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
    fun playingAudioAndchangeStatus(position: Int): List<AudioFileView> {

        val item = mListAudioFileView.get(position).copy()
        val itemLoadStatus = item.itemLoadStatus.copy()
        itemLoadStatus.playerState = PlayerState.PLAYING
        item.itemLoadStatus = itemLoadStatus
        mListAudioFileView[position] = item

        // khi play nhạc reset lại trạng thái các item khác
        var index = 0
        for (item in mListAudioFileView) {
            if (index != position) {
                val newItem = item.copy()
                newItem.isExpanded = false

                val itemLoadStatus = newItem.itemLoadStatus.copy()
                itemLoadStatus.playerState = PlayerState.IDLE
                newItem.itemLoadStatus = itemLoadStatus

                mListAudioFileView[index] = newItem
            }
            index++
        }

        runOnBackground {
            // nếu lần đầu tiên chưa khởi tạo mediaplay thì chưa stop
            // mỗi lần phát nhạc sẽ tự động stop mediaplayer
            if (mediaFirstPlay)
                ManagerFactory.getAudioPlayer().stop()
            ManagerFactory.getAudioPlayer().play(mListAudioFileView.get(position).audioFile)
            mediaFirstPlay = true

            // lần cuối cùng trước khi delete -> udpate lại status
//            positionLastPlaying = position
        }

        // trang thai phat nhac
        isPlayingStatus = true
        return mListAudioFileView
    }

    fun pauseAudioAndChangeStatus(position: Int): List<AudioFileView> {

        val item = mListAudioFileView.get(position).copy()
        val itemLoadStatus = item.itemLoadStatus.copy()
        itemLoadStatus.playerState = PlayerState.PAUSE
        item.itemLoadStatus = itemLoadStatus
        mListAudioFileView[position] = item

        runOnBackground {
            ManagerFactory.getAudioPlayer().pause()
        }
        // trang thai phat nhac
        isPlayingStatus = false
        return mListAudioFileView
    }

    fun stopAudioAndChangeStatus(position: Int): List<AudioFileView> {
        val item = mListAudioFileView.get(position).copy()
        val itemLoadStatus = item.itemLoadStatus.copy()
        itemLoadStatus.playerState = PlayerState.IDLE
        item.itemLoadStatus = itemLoadStatus
        mListAudioFileView[position] = item

        runOnBackground {
            ManagerFactory.getAudioPlayer().stop()
        }
        // trang thai phat nhac
        isPlayingStatus = false
        return mListAudioFileView
    }

    fun resumeAudioAndChangeStatus(position: Int): List<AudioFileView> {


        val item = mListAudioFileView.get(position).copy()
        val itemLoadStatus = item.itemLoadStatus.copy()
        itemLoadStatus.playerState = PlayerState.PLAYING
        item.itemLoadStatus = itemLoadStatus
        mListAudioFileView[position] = item

        runOnBackground {
            ManagerFactory.getAudioPlayer().resume()
        }

        // trang thai phat nhac
        isPlayingStatus = true

        return mListAudioFileView
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
                OldselectedPosition = selectedPosition
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
            itemLoadStatus.playerState = PlayerState.PLAYING
            audioFileView.itemLoadStatus = itemLoadStatus

            mListAudioFileView[selectedPosition] = audioFileView
        }
        return mListAudioFileView
    }

}