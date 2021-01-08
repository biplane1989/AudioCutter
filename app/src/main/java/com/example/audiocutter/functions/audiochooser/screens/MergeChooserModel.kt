package com.example.audiocutter.functions.audiochooser.screens

import android.app.Application
import android.text.TextUtils
import android.util.Log
import androidx.lifecycle.*
import com.example.audiocutter.base.BaseAndroidViewModel
import com.example.audiocutter.core.manager.AudioPlayer
import com.example.audiocutter.core.manager.ManagerFactory
import com.example.audiocutter.core.result.AudioEditorManagerlmpl
import com.example.audiocutter.functions.audiochooser.objects.AudioCutterView
import com.example.audiocutter.objects.AudioFileScans
import com.example.audiocutter.objects.StateLoad
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

fun List<AudioCutterView>.findAudioCutterView(filePath: String): AudioCutterView? {
    this.forEach {
        if (it.audioFile.getFilePath() == filePath) {
            return it
        }
    }
    return null
}

class MergeChooserModel(application: Application) : BaseAndroidViewModel(application) {

    private var indexChoose: Int = 0
    private lateinit var listAudioChooser: MutableList<AudioCutterView>
//    private val listAudioChooserLiveData = MutableLiveData<List<AudioCutterView>>()

    private val audioPlayer = ManagerFactory.newAudioPlayer()
    private val TAG = "NmcheckScrMer"
    private var currentAudioPlaying: File = File("")

    private var _stateLoadProgress = MutableLiveData<Int>()
    val stateLoadProgress: LiveData<Int>
        get() = _stateLoadProgress

/*
    private var _stateChecked = MutableLiveData<Int>()
    val stateChecked: LiveData<Int>
        get() = _stateChecked
*/

    private var mListPath = ArrayList<String>()

    private var filterText = ""
    private var count = 0

    private var _isEmptyState = MutableLiveData<Boolean>()
    val isEmptyState: LiveData<Boolean>
        get() = _isEmptyState
    private val _listAudioFiles = MediatorLiveData<List<AudioCutterView>?>()
    fun getAudioPlayer(): AudioPlayer {
        return audioPlayer
    }

    val countItemSelected = liveData<Int> {
        emitSource(_listAudioFiles.map {
            it?.sumBy { if (it.isCheckChooseItem) 1 else 0 } ?: 0
        })
    }

//    fun getListChooserLiveData(): LiveData<List<AudioCutterView>> {
//        return listAudioChooserLiveData
//    }

    private var sortByNo: Comparator<AudioCutterView> = Comparator { o1, o2 -> o1.no - o2.no }

    private var listAudioFiles = ArrayList<AudioCutterView>()

    init {
        audioPlayer.init(application.applicationContext)
        _listAudioFiles.addSource(ManagerFactory.getAudioFileManager().findAllAudioFiles()) {
//            var listAudioFiles: List<AudioCutterView>? = null
            when (it.state) {
                StateLoad.LOADING -> {
                    _stateLoadProgress.postValue(1)
                }
                StateLoad.LOADDONE -> {

                    _stateLoadProgress.postValue(0)

//                    val tmpList = ArrayList<AudioCutterView>()
//                    it.listAudioFiles.forEach {
//                        tmpList.add(AudioCutterView(it))
//                    }
//                    listAudioFiles = tmpList

                    synchronizationData(it)
                    Log.d(TAG, "list size 22:  ${listAudioFiles.size}")
                    _listAudioFiles.postValue(listAudioFiles)

                }
                StateLoad.LOADFAIL -> {
                    _stateLoadProgress.postValue(-1)
                }
                else -> {
                    //do nothing
                }
            }
        }

    }

    private fun synchronizationData(audioFileScans: AudioFileScans) {
        val resultListAudio = ArrayList<AudioCutterView>()
        val newListAudio = audioFileScans.listAudioFiles
        var isInstance = false
        if (listAudioFiles.isEmpty()) {
            newListAudio.forEach { audioFile ->
                resultListAudio.add(AudioCutterView(audioFile))
            }
        } else {
            for (newItem in newListAudio) {
                isInstance = false
                for (oldItem in listAudioFiles) {
                    if (TextUtils.equals(newItem.getFilePath(), oldItem.audioFile.getFilePath())) {
                        resultListAudio.add(oldItem)
                        isInstance = true
                        break
                    }
                }
                if (!isInstance) {
                    resultListAudio.add(AudioCutterView(newItem))
                }
            }
        }

        listAudioFiles.clear()
        listAudioFiles.addAll(resultListAudio)
    }

    private val _listFilteredAudioFiles = liveData<List<AudioCutterView>?> {
        emitSource(_listAudioFiles.map {
            it?.let { filterList(it) }
        })
    }

    private fun filterList(it: List<AudioCutterView>): List<AudioCutterView> {
        var listResult: List<AudioCutterView>? = null
        listResult = ArrayList(it)
        val listEmpty = ArrayList<Boolean>()
        if (filterText.isNotEmpty()) {
            listResult.clear()
            it.forEach { item ->
                val rs = item.audioFile.fileName.toLowerCase(Locale.getDefault())
                    .contains(filterText.toLowerCase(Locale.getDefault()))
                listEmpty.add(rs)
                if (rs) {
                    listResult.add(item)
                }
            }
            if (!listEmpty.contains(true)) {
                _isEmptyState.postValue(false)
            } else {
                _isEmptyState.postValue(true)
            }
        }

        return listResult
    }

    fun getStateLoading(): LiveData<Int> {
        return stateLoadProgress
    }

    /* @JvmName("getStateChecked1")
     fun getStateChecked(): LiveData<Int> {
         return stateChecked
     }*/


    fun getAllAudioFile(): LiveData<List<AudioCutterView>?> {
        return _listFilteredAudioFiles
    }


    private fun getListFilteredAudio(): ArrayList<AudioCutterView> {
        return ArrayList(_listFilteredAudioFiles.value ?: ArrayList())
    }

    private fun getListAllAudio(): ArrayList<AudioCutterView> {
        return ArrayList(_listAudioFiles.value ?: ArrayList())
    }


    fun searchAudio(yourTextSearch: String) {
        filterText = yourTextSearch
        _listAudioFiles.postValue(_listAudioFiles.value)

    }

    fun chooseItemAudioFile(audioCutterView: AudioCutterView, rs: Boolean) {
        try {
            if (rs && mListPath.indexOf(audioCutterView.audioFile.getFilePath()) == -1) {
                count++
            }

            val mListAudios = getListAllAudio()
            val pos = mListAudios.indexOf(audioCutterView)
            mListAudios[pos].isCheckChooseItem = rs
            if (mListAudios[pos].no == -1) {
                mListAudios[pos].no = count
            }
            indexChoose = 0
            mListAudios.forEach {
                if (it.isCheckChooseItem) {
                    indexChoose++
                }
            }
            /*         _stateChecked.postValue(indexChoose)*/
            _listAudioFiles.postValue(mListAudios)


        } catch (e: ArrayIndexOutOfBoundsException) {
            e.printStackTrace()
        }
    }


    suspend fun play(pos: Int) {
        val listAudios = getListFilteredAudio()
        val audioItem = listAudios[pos]
        audioPlayer.play(audioItem.audioFile)

    }

    fun pause() {
        audioPlayer.pause()
    }

    fun stop() {
        audioPlayer.stop()
    }

    fun resume() {
        audioPlayer.resume()
    }


    /*override fun onReceivedAction(fragmentMeta: FragmentMeta) {
        Log.d("TAG", "onReceivedAction: receive data")
        if (fragmentMeta.action.equals("ACTION_DELETE")) {
            val audio = fragmentMeta.data as AudioCutterView
            getlistAfterReceive(audio)

        }
        if (fragmentMeta.action.equals("ACTION_SEND_LISTPATH")) {
            val listPath = fragmentMeta.data as ArrayList<String>
            mListPath.addAll(listPath)
        }
        super.onReceivedAction(fragmentMeta)
    }*/

    fun getListPathReceiver(): ArrayList<String> {
        return mListPath
    }

    fun getListItemChoose(): List<AudioCutterView> {
        mListPath.clear()
        val mListAudios = getListAllAudio()
        listAudioChooser = mutableListOf()
        for (item in mListAudios) {
            if (item.isCheckChooseItem) {
                mListPath.add(item.audioFile.getFilePath())
                listAudioChooser.add(item)
            }
            if (!item.isCheckChooseItem) {
                item.no = -1
            }
        }
        mListPath.forEach {
            Log.d(TAG, "getListItemChoose: path $it")
        }
        _listAudioFiles.postValue(mListAudios)

//        listAudioChooserLiveData.value = listAudioChooser
        return listAudioChooser
    }

    fun getListAudioChoose(): MutableList<AudioCutterView> {
        Collections.sort(listAudioChooser, sortByNo)
//        listAudioChooserLiveData.value = listAudioChooser
        return listAudioChooser
    }

    private fun swapNo(filePath1: String, filePath2: String) {
        //sync list AudioItems are selected and all AudioFile

        val audioCutterView1 = getListAllAudio().findAudioCutterView(filePath1)
        val audioCutterView2 = getListAllAudio().findAudioCutterView(filePath2)
        audioCutterView1?.swapNo(audioCutterView2)

        val selectedAudioFile1 = listAudioChooser.findAudioCutterView(filePath1)
        val selectedAudioFile2 = listAudioChooser.findAudioCutterView(filePath2)
        selectedAudioFile1?.swapNo(selectedAudioFile2)

    }


    fun swapItemAudio(index1: Int, index2: Int): List<AudioCutterView> {
        listAudioChooser[index1] = listAudioChooser[index1].copy()
        listAudioChooser[index2] = listAudioChooser[index2].copy()
        swapNo(listAudioChooser[index1].audioFile.getFilePath(), listAudioChooser[index2].audioFile.getFilePath())
        Collections.sort(listAudioChooser, sortByNo)
        return listAudioChooser
    }

    fun removeItemAudio(item: AudioCutterView): List<AudioCutterView> {
        indexChoose--

        listAudioChooser.remove(item)

//        synchronized(listAudioChooser) {
//            listAudioChooser.remove(item)
//        }

//        listAudioChooserLiveData.value = listAudioChooser
        getlistAfterReceive(item)

        /* _stateChecked.postValue(indexChoose)*/
        return listAudioChooser
    }

    private fun getlistAfterReceive(item: AudioCutterView) {
        val mListAudios = ArrayList(_listAudioFiles.value)

        for (index in mListAudios.indices) {
            if (mListAudios[index].audioFile.file.absolutePath.equals(item.audioFile.file.absolutePath)) {
                mListAudios.remove(mListAudios[index])
                mListPath.remove(mListAudios[index].audioFile.getFilePath())
                mListAudios.add(index, AudioCutterView(item.audioFile, isCheckChooseItem = false, no = -1))
            }
        }
        _listAudioFiles.postValue(mListAudios)
    }

}