package com.example.audiocutter.functions.audiochooser.screens

import android.app.Application
import androidx.lifecycle.*
import com.example.audiocutter.base.BaseAndroidViewModel
import com.example.audiocutter.core.manager.AudioPlayer
import com.example.audiocutter.core.manager.ManagerFactory
import com.example.audiocutter.functions.audiochooser.objects.AudioCutterView
import com.example.audiocutter.objects.StateLoad
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

class MergeChooserModel(application: Application) : BaseAndroidViewModel(application) {

    private var indexChoose: Int = 0
    private lateinit var listAudioChooser: MutableList<AudioCutterView>
    private val audioPlayer = ManagerFactory.newAudioPlayer()
    private val TAG = MergeChooserModel::class.java.name
    private var currentAudioPlaying: File = File("")

    private var _stateLoadProgress = MutableLiveData<Int>()
    val stateLoadProgress: LiveData<Int>
        get() = _stateLoadProgress

    private var _stateChecked = MutableLiveData<Int>()
    val stateChecked: LiveData<Int>
        get() = _stateChecked

    private var mListPath = ArrayList<String>()

    private var filterText = ""

    private var _isEmptyState = MutableLiveData<Boolean>()
    val isEmptyState: LiveData<Boolean>
        get() = _isEmptyState
    private val _listAudioFiles = MediatorLiveData<List<AudioCutterView>?>()

    fun getAudioPlayer(): AudioPlayer {
        return audioPlayer
    }

    init {
        audioPlayer.init(application.applicationContext)
        _listAudioFiles.addSource(ManagerFactory.getAudioFileManager().findAllAudioFiles()) {
            var listAudioFiles: List<AudioCutterView>? = null
            when (it.state) {
                StateLoad.LOADING -> {
                    _stateLoadProgress.postValue(1)
                }
                StateLoad.LOADDONE -> {
                    _stateLoadProgress.postValue(0)
                    val tmpList = ArrayList<AudioCutterView>()
                    it.listAudioFiles.forEach {
                        tmpList.add(AudioCutterView(it))
                    }
                    listAudioFiles = tmpList
                }
                StateLoad.LOADFAIL -> {
                    _stateLoadProgress.postValue(-1)
                }
                else -> {
                    //do nothing
                }
            }
            _listAudioFiles.postValue(listAudioFiles)
        }
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

    fun getStateEmpty(): LiveData<Boolean> {
        return isEmptyState
    }

    @JvmName("getStateChecked1")
    fun getStateChecked(): LiveData<Int> {
        return stateChecked
    }


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

    fun chooseItemAudioFile(audioCutterView: AudioCutterView, rs: Boolean, count: Int) {
        try {
            val mListAudios = getListAllAudio()
            val pos = mListAudios.indexOf(audioCutterView)
            val itemAudio: AudioCutterView = mListAudios[pos].copy()

            mListAudios[pos].isCheckChooseItem = rs
            mListAudios[pos].no = count


            indexChoose = 0
            mListAudios.forEach {
                if (it.isCheckChooseItem) {
                    indexChoose++
                }
            }
            _stateChecked.postValue(indexChoose)
            _listAudioFiles.postValue(mListAudios)

        } catch (e: ArrayIndexOutOfBoundsException) {
            e.printStackTrace()
        }
    }


    fun getListItemChoose(): List<AudioCutterView> {
        val mListAudios = getListAllAudio()
        listAudioChooser = mutableListOf<AudioCutterView>()
        for (item in mListAudios) {
            if (item.isCheckChooseItem) {
                listAudioChooser.add(item)
            }
        }
        return listAudioChooser
    }

    fun getListAudioChoose(): MutableList<AudioCutterView> {
        return listAudioChooser
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

    private fun getlistAfterReceive(item: AudioCutterView) {
        val mListAudios = ArrayList(_listAudioFiles.value)

        for (index in mListAudios.indices) {
            if (mListAudios[index].audioFile.file.absolutePath.equals(item.audioFile.file.absolutePath)) {
                mListAudios.remove(mListAudios[index])
                mListAudios.add(index, AudioCutterView(item.audioFile, isCheckChooseItem = false))
            }
        }
        /* var count = 0
         for (item in mListAudios) {
             if (item.isCheckChooseItem) {
                 count++
             }
         }
         _stateChecked.postValue(count)*/
        _listAudioFiles.postValue(mListAudios)
    }

    fun moveItemAudio(prePos: Int, nextPos: Int): List<AudioCutterView> {
        val preItem = listAudioChooser[prePos].copy()
        listAudioChooser.remove(preItem)
        listAudioChooser.add(nextPos, preItem)
        return listAudioChooser
    }

    fun removeItemAudio(item: AudioCutterView): List<AudioCutterView> {
        listAudioChooser.remove(item)
        getlistAfterReceive(item)
        indexChoose--
        _stateChecked.postValue(indexChoose)
        return listAudioChooser
    }


}