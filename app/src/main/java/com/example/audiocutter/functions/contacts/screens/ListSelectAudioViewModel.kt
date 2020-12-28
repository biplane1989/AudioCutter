package com.example.audiocutter.functions.contacts.screens

import android.app.Application
import android.text.TextUtils
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.example.audiocutter.base.BaseAndroidViewModel
import com.example.audiocutter.core.manager.AudioPlayer
import com.example.audiocutter.core.manager.ManagerFactory
import com.example.audiocutter.core.manager.PlayerState
import com.example.audiocutter.functions.contacts.objects.SelectItemStatus
import com.example.audiocutter.functions.contacts.objects.SelectItemView
import com.example.audiocutter.objects.StateLoad
import com.example.audiocutter.util.Utils
import java.util.*
import kotlin.collections.ArrayList

class ListSelectAudioViewModel(application: Application) : BaseAndroidViewModel(application) {

    private val mContext = getApplication<Application>().applicationContext
    private val audioPlayer = ManagerFactory.newAudioPlayer()
    private var mListAudioFileView = ArrayList<SelectItemView>()
    private var mListSearch = ArrayList<SelectItemView>()

    private val mAudioMediatorLiveData = MediatorLiveData<ArrayList<SelectItemView>>()
    private var loadingStatus: MutableLiveData<Boolean> = MutableLiveData()
    private var isEmptyStatus: MutableLiveData<Boolean> = MutableLiveData()
    private var isChoseMusic: MutableLiveData<Boolean> = MutableLiveData()

    fun getLoadingStatus(): LiveData<Boolean> {
        return loadingStatus
    }

    fun getIsEmptyStatus(): LiveData<Boolean> {
        return isEmptyStatus
    }

    fun getListAudioFile(): MediatorLiveData<ArrayList<SelectItemView>> {
        return mAudioMediatorLiveData
    }

    fun getChoseMusic(): LiveData<Boolean> {
        return isChoseMusic
    }

    init {
        audioPlayer.init(application.applicationContext)
    }

    fun init(fileUri: String) {
        mAudioMediatorLiveData.addSource(ManagerFactory.getAudioFileManager().findAllAudioFiles()) {
            runOnBackground {
                loadingStatus.postValue(true)
                if (it.state == StateLoad.LOADING) {
                    //isEmptyStatus.postValue(false)
                    loadingStatus.postValue(true)
                }
                if (it.state == StateLoad.LOADDONE) {
//                    loadingStatus.postValue(true)
                    // khi loading xong thi check co data hay khong de show man hinh empty data
                    if (!it.listAudioFiles.isEmpty()) {
                        it.listAudioFiles.forEach { audioFile ->
                            mListAudioFileView.add(SelectItemView(audioFile, false, false, SelectItemStatus(), false))
                        }
                        mListAudioFileView = getRingtoneDefault(mListAudioFileView) as ArrayList<SelectItemView>
                        loadingStatus.postValue(false)

                    } else {
                        loadingStatus.postValue(false)
                        isEmptyStatus.postValue(true)
                    }
                }
                selectRingtone(fileUri)
                mAudioMediatorLiveData.postValue(mListAudioFileView)

            }
        }
    }

    private fun selectRingtone(ringtonePath: String) {
        var index = 0
        for (item in mListAudioFileView) {
            if (TextUtils.equals(item.audioFile.uri.toString(), ringtonePath)) {
                val newItem = item.copy()
                newItem.isSelect = true
                mListAudioFileView.set(index, newItem)
                break
            }
            index++
        }
        checkIsChoseRingTone()
    }

    fun getIndexSelectRingtone(ringtonePath: String): Int {         // lay vi tri cua file audio la nhac chuong cua contact
        var index = 0
//         for (item in mListAudioFileView) {
//             if (TextUtils.equals(item.audioFile.fileName + item.audioFile.mimeType, fileName)) {
//
//                 return index
//             }
//             index++
//         }
        for (item in mListAudioFileView) {
            if (TextUtils.equals(item.audioFile.uri.toString(), ringtonePath)) {

                return index
            }
            index++
        }
        return 0
    }

    fun checkIsChoseRingTone() {
        if (checkIsSelect()) {
            isChoseMusic.postValue(true)
        } else {
            isChoseMusic.postValue(false)
        }
    }

    private fun checkIsSelect(): Boolean {
        for (item in mListAudioFileView) {
            if (item.isSelect) {
                return true
            }
        }
        return false
    }

    fun showPlayingAudio(filePath: String) {
        // khi play nhac reset lai trang thai cac item khac

        var newAudioList = ArrayList<SelectItemView>()
        if (mListSearch.size > 0) {
            newAudioList = mListSearch
        } else {
            newAudioList = mListAudioFileView
        }

        var index1 = 0
        for (item in newAudioList) {
            if (!TextUtils.equals(item.getFilePath(), filePath)) {
                val newItem = item.copy()
                newItem.isSelect = false
                newItem.isExpanded = false
                newAudioList[index1] = newItem
            } else {
                val newItem = item.copy()
                newItem.isSelect = true
                newItem.isExpanded = !item.isExpanded
                newAudioList[index1] = newItem
            }
            index1++
        }

        var index = 0
        for (item in mListAudioFileView) {
            if (!TextUtils.equals(item.getFilePath(), filePath)) {
                val newItem = item.copy()
                newItem.isSelect = false
                newItem.isExpanded = false
                mListAudioFileView[index] = newItem
            } else {
                val newItem = item.copy()
                newItem.isSelect = true
//                newItem.isExpanded = !item.isExpanded
                mListAudioFileView[index] = newItem
            }
            index++
        }

        mAudioMediatorLiveData.postValue(newAudioList)
        checkIsChoseRingTone()
    }

    private fun getRingtoneDefault(list: List<SelectItemView>): List<SelectItemView> {
        val ringtoneDefault = Utils.getUriRingtoneDefault(mContext)
        for (item in list) {
            item.isRingtoneDefault = (item.audioFile.uri.toString() == ringtoneDefault)
        }
        return list
    }

    fun stopAudio(position: Int) {

        audioPlayer.stop()

        val selectItemView = mListAudioFileView.get(position).copy()
        val itemLoadStatus = selectItemView.selectItemStatus.copy()
        itemLoadStatus.playerState = PlayerState.IDLE
        selectItemView.selectItemStatus = itemLoadStatus

        mListAudioFileView.set(position, selectItemView)

        mAudioMediatorLiveData.postValue(mListAudioFileView)    //TODO
    }


    override fun onCleared() {
        super.onCleared()
        audioPlayer.stop()
    }

    fun getAudioPlayer(): AudioPlayer {
        return audioPlayer
    }

    fun searchAudioFile(data: String) {

        Log.d("TAG", "searchAudioFile: data search:  " + data)
        var index = 0                   // dong bo hoa mListSearch va mListAudioFileView
        if (mListSearch.size > 0) {
            for (item in mListAudioFileView) {
                for (searchItem in mListSearch) {
                    if (TextUtils.equals(item.audioFile.uri.toString(), searchItem.audioFile.uri.toString())) {
                        mListAudioFileView.set(index, searchItem)
                        break
                    }
                }
                index++
            }
        }

        mListSearch.clear()
        if (data.equals("")) {
            mAudioMediatorLiveData.postValue(mListAudioFileView)
        } else {
            for (item in mListAudioFileView) {
                if (item.audioFile.fileName.toUpperCase(Locale.ROOT)
                        .contains(data.toUpperCase(Locale.ROOT))) {
                    mListSearch.add(item)
                }
            }
            mAudioMediatorLiveData.postValue(mListSearch)
        }

        if (mListAudioFileView.size > 0) {
            isEmptyStatus.postValue(false)
            if (mListSearch.size == 0 && !data.equals("")) {
                isEmptyStatus.postValue(true)
            }
        } else {
            isEmptyStatus.postValue(true)
        }
    }

    fun setRingtone(phoneNumber: String): Boolean {
        if (phoneNumber != "") {
            for (audio in mListAudioFileView) {
                if (audio.isSelect) {
                    return ManagerFactory.getRingtoneManager()
                        .setRingToneWithContactNumberandFilePath(audio.audioFile.file.absolutePath, phoneNumber)
                }
            }
        }
        return false
    }

    fun setRingtoneWithUri(phoneNumber: String, uri: String): Boolean {
        if (phoneNumber != "" && uri != "") {
            return ManagerFactory.getRingtoneManager()
                .setRingToneWithContactNumberAndUri(uri, phoneNumber)
        }
        return false
    }
}