package com.example.audiocutter.functions.contacts.screens

import android.app.Application
import android.media.MediaMetadataRetriever
import android.text.TextUtils
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.example.audiocutter.base.BaseAndroidViewModel
import com.example.audiocutter.core.manager.ManagerFactory
import com.example.audiocutter.core.manager.PlayerInfo
import com.example.audiocutter.core.manager.PlayerState
import com.example.audiocutter.functions.contacts.objects.SelectItemStatus
import com.example.audiocutter.functions.contacts.objects.SelectItemView
import com.example.audiocutter.util.Utils

class ListSelectAudioViewModel(application: Application) : BaseAndroidViewModel(application) {

    private val mContext = getApplication<Application>().applicationContext
    var isPlayingStatus = false
    private var mListAudioFileView = ArrayList<SelectItemView>()
    val TAG = "giangtd"

    var loadingStatus: MutableLiveData<Boolean> = MutableLiveData()

    fun getData(): LiveData<List<SelectItemView>> {
        return Transformations.map(ManagerFactory.getAudioFileManager().findAllAudioFiles()) { listAudio ->
            Log.d("nmcode", "live data size: " + listAudio.listAudioFiles.size)
            // lan dau tien lay du lieu
            if (mListAudioFileView.size == 0) {

                    listAudio.listAudioFiles.forEach { audioFile ->
                        val duration = ManagerFactory.getAudioFileManager()
                            .getInfoAudioFile(audioFile.file, MediaMetadataRetriever.METADATA_KEY_DURATION)
                        duration?.let {
                            mListAudioFileView.add(SelectItemView(audioFile, false, false, SelectItemStatus(), false, duration))
                        }
                    }
                    mListAudioFileView = getRingtoneDefault(mListAudioFileView) as ArrayList<SelectItemView>

//                mListAudioFileView

            } else { // khi thay doi du lieu update
                // dong bo hoa du lieu list cu va moi
                val newListSelectItemView = ArrayList<SelectItemView>()
                listAudio.listAudioFiles.forEach {audioFile ->
                    val audioFileView = getAudioFileView(audioFile.file.absolutePath)
                    if (audioFileView != null) {
                        newListSelectItemView.add(audioFileView)
                    } else {
                        val duration = ManagerFactory.getAudioFileManager()
                            .getInfoAudioFile(audioFile.file, MediaMetadataRetriever.METADATA_KEY_DURATION)
                        duration?.let {
                            newListSelectItemView.add(SelectItemView(audioFile, false, false, SelectItemStatus(), false, duration))
                        }
//                        newListSelectItemView.add(SelectItemView(audioFile))
                    }
                }

                mListAudioFileView = getRingtoneDefault(newListSelectItemView) as ArrayList<SelectItemView>
//                mListAudioFileView
            }
            mListAudioFileView
        }
    }

    // tim ra nhung file da ton tai trong list cu
    private fun getAudioFileView(filePath: String): SelectItemView? {
        mListAudioFileView.forEach {
            if (it.audioFile.file.absolutePath.equals(filePath)) {
                return it
            }
        }
        return null
    }

    fun setSelectRingtone(fileName: String): List<SelectItemView> {
        var index = 0
        for (item in mListAudioFileView) {
            if (TextUtils.equals(item.audioFile.fileName + ".mp3", fileName)) {
                val newItem = item.copy()
                newItem.isSelect = true
                mListAudioFileView.set(index, newItem)
                break
            }
            index++
        }
        return mListAudioFileView
    }

    fun getIndexSelectRingtone(fileName: String): Int {
        var index = 0
        for (item in mListAudioFileView) {
            if (TextUtils.equals(item.audioFile.fileName + ".mp3", fileName)) {

                return index
            }
            index++
        }
        return 0
    }

    fun showPlayingAudio(position: Int): List<SelectItemView> {

        // khi play nhac reset lai trang thai cac item khac
        var index = 0
        stopAudio(position)
        for (item in mListAudioFileView) {
            if (index != position) {
                val newItem = item.copy()
                newItem.isExpanded = false
                mListAudioFileView[index] = newItem
            }
            index++
        }
        val selectItemView = mListAudioFileView.get(position).copy()
        selectItemView.isExpanded = !mListAudioFileView.get(position).isExpanded

//        selectItemView.audioFile.time = 1000

        /*   ManagerFactory.getAudioFileManager()        // lay time total cho file audio
               .getDurationByPath(mListAudioFileView[position].audioFile.file)*/

        mListAudioFileView.set(position, selectItemView)
        return mListAudioFileView
    }

    fun selectAudio(position: Int): List<SelectItemView> {
        var index = 0
        for (item in mListAudioFileView) {
            if (index != position) {
                val newItem = item.copy()
                newItem.isSelect = false
                mListAudioFileView[index] = newItem
            }
            index++
        }
        val selectItemView = mListAudioFileView.get(position).copy()
        selectItemView.isSelect = true
        mListAudioFileView.set(position, selectItemView)
        return mListAudioFileView
    }

    private fun getRingtoneDefault(list: List<SelectItemView>): List<SelectItemView> {
        for (item in list) {
            item.isRingtoneDefault = Utils.checkRingtoneDefault(mContext, item.audioFile.uri.toString())
        }
        return list
    }

    // chuyen trang thai play nhac
    fun playAudio(position: Int) {
        runOnBackground {
            ManagerFactory.getDefaultAudioPlayer().play(mListAudioFileView.get(position).audioFile)
        }

        // trang thai phat nhac
        isPlayingStatus = true
    }

    fun pauseAudio() {

        runOnBackground {
            ManagerFactory.getDefaultAudioPlayer().pause()
        }
    }

    fun stopAudio(position: Int) {
        runOnBackground {
            ManagerFactory.getDefaultAudioPlayer().stop()
        }
        val selectItemView = mListAudioFileView.get(position).copy()
        val itemLoadStatus = selectItemView.selectItemStatus.copy()
        itemLoadStatus.playerState = PlayerState.IDLE
        selectItemView.selectItemStatus = itemLoadStatus

        mListAudioFileView.set(position, selectItemView)
    }

    fun resumeAudio() {

        runOnBackground {
            ManagerFactory.getDefaultAudioPlayer().resume()
        }
    }

    fun seekToAudio(cusorPos: Int) {
        runOnBackground {
            ManagerFactory.getDefaultAudioPlayer().seek(cusorPos)
        }
    }

    fun updatePlayerInfo(playerInfo: PlayerInfo): List<SelectItemView> {
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
                ManagerFactory.getDefaultAudioPlayer().stop()
            }

        } else {
            val selectItemView = mListAudioFileView.get(selectedPosition).copy()
            // update
            val itemLoadStatus = selectItemView.selectItemStatus.copy()
            itemLoadStatus.duration = playerInfo.duration
            itemLoadStatus.currPos = playerInfo.posision
            itemLoadStatus.playerState = playerInfo.playerState
            selectItemView.selectItemStatus = itemLoadStatus

            mListAudioFileView[selectedPosition] = selectItemView

        }
        return mListAudioFileView
    }

    fun searchAudioFile(data: String): ArrayList<SelectItemView> {
        val newListAudio = ArrayList<SelectItemView>()
        for (audio in mListAudioFileView) {
            if (audio.audioFile.fileName.toUpperCase().contains(data.toUpperCase())) {
                newListAudio.add(audio)
            }
        }
        return newListAudio
    }

    fun setRingtone(phoneNumber: String): Boolean {
        if (phoneNumber != "") {
            for (audio in mListAudioFileView) {
                if (audio.isSelect) {
                    return ManagerFactory.getRingtoneManager()
                        .setRingToneWithContactNumber(audio.audioFile, phoneNumber)
                }
            }
        }
        return false
    }

    fun setRingtoneWithUri(phoneNumber: String, uri: String): Boolean {
        if (phoneNumber != null && uri != null) {
            return ManagerFactory.getRingtoneManager()
                .setRingToneWithContactNumberAndUri(uri, phoneNumber)
        }
        return false
    }
}