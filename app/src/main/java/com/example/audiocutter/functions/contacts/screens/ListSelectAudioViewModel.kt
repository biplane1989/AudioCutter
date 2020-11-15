package com.example.audiocutter.functions.contacts.screens

import android.app.Application
import android.media.MediaMetadataRetriever
import android.text.TextUtils
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.example.audiocutter.base.BaseAndroidViewModel
import com.example.audiocutter.core.manager.ManagerFactory
import com.example.audiocutter.core.manager.PlayerInfo
import com.example.audiocutter.core.manager.PlayerState
import com.example.audiocutter.functions.contacts.objects.SelectItemStatus
import com.example.audiocutter.functions.contacts.objects.SelectItemView
import com.example.audiocutter.functions.mystudio.objects.AudioFileView
import com.example.audiocutter.objects.StateLoad
import com.example.audiocutter.util.Utils
import kotlinx.coroutines.launch

class ListSelectAudioViewModel(application: Application) : BaseAndroidViewModel(application) {

    private val mContext = getApplication<Application>().applicationContext
    var isPlayingStatus = false
    private var mListAudioFileView = ArrayList<SelectItemView>()
    var newListSearch = ArrayList<SelectItemView>()         // su dung de search
    var isSearchStatus = false
    var newListSetRingtone = ArrayList<SelectItemView>()        // dung de set ringtone khi co su kien search hoac khong
    val TAG = "giangtd"

    private val audioPlayer = ManagerFactory.newAudioPlayer()

    private val mAudioMediatorLiveData = MediatorLiveData<ArrayList<SelectItemView>>()

    var loadingStatus: MutableLiveData<Boolean> = MutableLiveData()
    var isEmptyStatus: MutableLiveData<Boolean> = MutableLiveData()

    fun getLoadingStatus(): LiveData<Boolean> {
        return loadingStatus
    }

    fun getIsEmptyStatus(): LiveData<Boolean> {
        return isEmptyStatus
    }

    fun getListAudioFile(): MediatorLiveData<ArrayList<SelectItemView>> {
        return mAudioMediatorLiveData
    }

    init {
        audioPlayer.init(application.applicationContext)
    }

    fun init(fileUri: String) {

        mAudioMediatorLiveData.addSource(ManagerFactory.getAudioFileManager().findAllAudioFiles()) {
            runOnBackground {
                if (it.state == StateLoad.LOADING) {
                    isEmptyStatus.postValue(false)
                    loadingStatus.postValue(true)
                }
                if (it.state == StateLoad.LOADDONE) {       // khi loading xong thi check co data hay khong de show man hinh empty data
                    if (!it.listAudioFiles.isEmpty()) {
                        it.listAudioFiles.forEach { audioFile ->
                            mListAudioFileView.add(SelectItemView(audioFile, false, false, SelectItemStatus(), false))
                        }
                        mListAudioFileView = getRingtoneDefault(mListAudioFileView) as ArrayList<SelectItemView>
                        loadingStatus.postValue(false)
                    } else {
                        isEmptyStatus.postValue(true)
                    }
                }
                mAudioMediatorLiveData.postValue(setSelectRingtone(fileUri))
            }
            mAudioMediatorLiveData.addSource(audioPlayer.getPlayerInfo()) {
                updatePlayerInfo(it)
            }
        }
    }

    private fun setSelectRingtone(ringtonePath: String): ArrayList<SelectItemView> {
        var index = 0
//        for (item in mListAudioFileView) {
////            if (TextUtils.equals(item.audioFile.fileName + ".mp3", fileUri) || TextUtils.equals(item.audioFile.fileName + ".m4a", fileUri)) {
//            if (TextUtils.equals(item.audioFile.fileName + item.audioFile.mimeType, ringtonePath)) {
//                val newItem = item.copy()
//                newItem.isSelect = true
//                mListAudioFileView.set(index, newItem)
//                break
//            }

        for (item in mListAudioFileView) {
            if (TextUtils.equals(item.audioFile.uri.toString(), ringtonePath)) {
                val newItem = item.copy()
                newItem.isSelect = true
                mListAudioFileView.set(index, newItem)
                break
            }
            index++
        }

        return mListAudioFileView
    }

    fun getIndexSelectRingtone(ringtonePath: String): Int {
        var index = 0
        /* for (item in mListAudioFileView) {
             if (TextUtils.equals(item.audioFile.fileName + item.audioFile.mimeType, fileName)) {

                 return index
             }
             index++
         }*/
        for (item in mListAudioFileView) {
            if (TextUtils.equals(item.audioFile.uri.toString(), ringtonePath)) {

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

        mListAudioFileView.set(position, selectItemView)
        return mListAudioFileView
    }

    fun selectAudio(position: Int): List<SelectItemView> {

//        var newList = ArrayList<SelectItemView>()
        if (isSearchStatus) {
            newListSetRingtone = newListSearch
        } else {
            newListSetRingtone = mListAudioFileView
        }

        var index = 0
        for (item in newListSetRingtone) {
            if (index != position) {
                val newItem = item.copy()
                newItem.isSelect = false
                newListSetRingtone[index] = newItem
            }
            index++
        }
        val selectItemView = newListSetRingtone.get(position).copy()
        selectItemView.isSelect = true
        newListSetRingtone.set(position, selectItemView)
        return newListSetRingtone
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
            audioPlayer.play(mListAudioFileView.get(position).audioFile)
        }
        isPlayingStatus = true
    }

    fun pauseAudio() {
        audioPlayer.pause()
    }

    fun stopAudio(position: Int) {
        audioPlayer.stop()

        val selectItemView = mListAudioFileView.get(position).copy()
        val itemLoadStatus = selectItemView.selectItemStatus.copy()
        itemLoadStatus.playerState = PlayerState.IDLE
        selectItemView.selectItemStatus = itemLoadStatus

        mListAudioFileView.set(position, selectItemView)
    }

    fun resumeAudio() {
        audioPlayer.resume()
    }

    fun seekToAudio(cusorPos: Int) {
        audioPlayer.seek(cusorPos)
    }

    override fun onCleared() {
        super.onCleared()
        audioPlayer.stop()
    }

    fun updatePlayerInfo(playerInfo: PlayerInfo) {
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
            audioPlayer.stop()
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

        mAudioMediatorLiveData.postValue(mListAudioFileView)
    }

    fun searchAudioFile(data: String): ArrayList<SelectItemView> {
//        val newListAudio = ArrayList<SelectItemView>()

        if (data.equals("")) {
            isSearchStatus = false
        } else {
            isSearchStatus = true
        }
        newListSearch.clear()

        for (audio in mListAudioFileView) {
            if (audio.audioFile.fileName.toUpperCase().contains(data.toUpperCase())) {
                newListSearch.add(audio)
            }
        }
        return newListSearch
    }

    fun setRingtone(phoneNumber: String): Boolean {
        if (phoneNumber != "") {
//            for (audio in mListAudioFileView) {
            for (audio in newListSetRingtone) {
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