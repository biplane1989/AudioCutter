package com.example.audiocutter.functions.audiochooser.screens

import android.util.Log
import androidx.lifecycle.*
import com.example.audiocutter.base.BaseViewModel
import com.example.audiocutter.core.manager.ManagerFactory
import com.example.audiocutter.core.manager.PlayerInfo
import com.example.audiocutter.core.manager.PlayerState
import com.example.audiocutter.functions.audiochooser.objects.AudioCutterView
import com.example.audiocutter.functions.mystudio.screens.FragmentMeta
import com.example.audiocutter.objects.StateLoad
import java.io.File
import java.util.*
import kotlin.Comparator
import kotlin.collections.ArrayList

class MergeChooserModel : BaseViewModel() {
    private val TAG = MergeChooserModel::class.java.name
    private var currentAudioPlaying: File = File("")

    /* private var mListAudioPrev = ArrayList<AudioCutterView>()*/
    //private val mListAudios = ArrayList<AudioCutterView>()
    /* private var mListIndexChoose = ArrayList<Int>()
     private var mListAudioSearch = ArrayList<AudioCutterView>()*/

    var count = 0

    private var _stateLoadProgress = MutableLiveData<Int>()
    val stateLoadProgress: LiveData<Int>
        get() = _stateLoadProgress

    private var _countItem = MutableLiveData<Int>()
    val countItem: LiveData<Int>
        get() = _countItem
    private var filterText = ""

    private val sortListByName: Comparator<AudioCutterView> =
        Comparator { m1, m2 ->
            m1.audioFile.fileName.compareTo(m2.audioFile.fileName)
        }
    private val listAudioFiles = MediatorLiveData<List<AudioCutterView>>()

    init {

        listAudioFiles.addSource(ManagerFactory.getAudioFileManager().findAllAudioFiles()) {
            when (it.state) {
                StateLoad.LOADING -> {
                    _stateLoadProgress.postValue(1)
                }
                StateLoad.LOADDONE -> {
                    _stateLoadProgress.postValue(0)
                }
                StateLoad.LOADFAIL -> {
                    _stateLoadProgress.postValue(-1)
                }
            }
            val listAudioViews = ArrayList<AudioCutterView>()
            it.listAudioFiles.forEach {
                listAudioViews.add(AudioCutterView(it))
            }
            listAudioFiles.postValue(listAudioViews)

        }
    }

    private val listFilteredAudioFiles = liveData<List<AudioCutterView>> {
        emitSource(listAudioFiles.map {
            var result = ArrayList(it)
            if (!filterText.isEmpty()) {
                result.clear()
                it.forEach { item ->
                    val rs = item.audioFile.fileName.toLowerCase(Locale.getDefault()).contains(
                        filterText.toLowerCase(
                            Locale.getDefault()
                        )
                    )
                    if (rs) {
                        result.add(item)
                    }
                }
            }

            result
        })
    }


    fun getStateLoading(): LiveData<Int> {
        return stateLoadProgress
    }


    fun getAllAudioFile(): LiveData<List<AudioCutterView>> {
        return listFilteredAudioFiles /*Transformations.map(ManagerFactory.getAudioFileManager().findAllAudioFiles()) {
            when (it.state) {
                StateLoad.LOADING -> {
                    _stateLoadProgress.postValue(1)
                }
                StateLoad.LOADDONE -> {
                    _stateLoadProgress.postValue(0)
                }
                StateLoad.LOADFAIL -> {
                    _stateLoadProgress.postValue(-1)
                }
            }

            it.listAudioFiles.forEach { }


            mListAudioPrev.clear()

            it.listAudioFiles.forEach {
                mListAudioPrev.add(AudioCutterView(it))
            }
            Collections.sort(mListAudioPrev, sortListByName)
            mListAudios.clear()
            mListAudios.addAll(mListAudioPrev)
            for (item in mListIndexChoose) {
                mListAudioPrev.removeAt(item)
                mListAudioPrev.add(
                    item,
                    AudioCutterView(
                        mListAudioSearch[item].audioFile,
                        PlayerState.IDLE,
                        true
                    )
                )
                Log.d(TAG, "getAllAudioFile: ${mListIndexChoose.size}")
            }
            mListAudioPrev
        }*/
    }


    private fun getPreviewAudioCutterView(): ArrayList<AudioCutterView> {
        return ArrayList(listFilteredAudioFiles.value ?: ArrayList())
    }

    private fun getAllAudioCutterView(): ArrayList<AudioCutterView> {
        return ArrayList(listAudioFiles.value ?: ArrayList())
    }

    fun updateMediaInfo(playerInfo: PlayerInfo) {
        Log.d(TAG, "updateMediaInfo: ${playerInfo.playerState}")
        val listAudioViews = getPreviewAudioCutterView()

        if (playerInfo.currentAudio != null) {
            if (currentAudioPlaying.absoluteFile != playerInfo.currentAudio!!.file.absoluteFile) {

                val oldPos = getAudioFilePos(currentAudioPlaying)
                val newPos = getAudioFilePos(playerInfo.currentAudio!!.file)
                if (oldPos != -1) {
                    val audioFile = listAudioViews[oldPos].copy()
                    audioFile.state = PlayerState.IDLE
                    audioFile.isCheckDistance = false
                    audioFile.currentPos = playerInfo.posision.toLong()
                    audioFile.duration = playerInfo.duration.toLong()
                    listAudioViews[oldPos] = audioFile
                }
                if (newPos != -1) {
                    val audioCutterView = listAudioViews[newPos].copy()
                    updateState(audioCutterView, playerInfo, true)
                    listAudioViews[newPos] = audioCutterView
                }
            } else {
                val atPos = getAudioFilePos(currentAudioPlaying)
                if (atPos != -1) {
                    Log.d(TAG, "updateMediaInfo: atPOs   ${listAudioViews.get(atPos).state}")
                    val audioCutterView = listAudioViews[atPos].copy()
                    updateState(audioCutterView, playerInfo, true)
                    listAudioViews[atPos] = audioCutterView
                }
            }
            currentAudioPlaying = playerInfo.currentAudio!!.file

        }

        listAudioFiles.postValue(listAudioViews)
    }


    private fun updateState(audioCutterView: AudioCutterView, playerInfo: PlayerInfo, rs: Boolean) {

        audioCutterView.state = playerInfo.playerState
        audioCutterView.isCheckDistance = rs
        audioCutterView.currentPos = playerInfo.posision.toLong()
        audioCutterView.duration = playerInfo.duration.toLong()

    }


    private fun getAudioFilePos(file: File): Int {
        val listAudioViews = getPreviewAudioCutterView()
        var i = 0
        while (i < listAudioViews.size) {
            if (listAudioViews[i].audioFile.file == file) {
                return i
            }
            i++
        }
        return -1
    }

    fun searchAudio(yourTextSearch: String) {
        filterText = yourTextSearch
        listAudioFiles.postValue(listAudioFiles.value)

        /* val listAudioViews = ArrayList(listAudioFiles.value ?: ArrayList())
         val mListAudioSearch = ArrayList<AudioCutterView>()
         listAudioViews.forEach {
             val rs = it.audioFile.fileName.toLowerCase(Locale.getDefault()).contains(
                 yourTextSearch.toLowerCase(
                     Locale.getDefault()
                 )
             )
             if (rs) {
                 mListAudioSearch.add(it)
             }
         }
         listFilteredAudioFiles.postValue(mListAudioSearch)*/
    }

    fun chooseItemAudioFile(audioCutterView: AudioCutterView, rs: Boolean) {
        val mListAudios = getAllAudioCutterView()
        val pos = mListAudios.indexOf(audioCutterView)
        val itemAudio: AudioCutterView = mListAudios[pos].copy()

        if (!rs) {
            itemAudio.isCheckChooseItem = true
            count++
            mListAudios[pos] = itemAudio
            _countItem.postValue(count)
        } else {
            itemAudio.isCheckChooseItem = false
            count--
            mListAudios[pos] = itemAudio
            _countItem.postValue(count)
        }
        listAudioFiles.postValue(mListAudios)
    }

    fun getCountItemChoose(): LiveData<Int> {

        return countItem
    }


    fun getListItemChoose(): List<AudioCutterView> {
        val mListAudios = getAllAudioCutterView()
        var listAudio = mutableListOf<AudioCutterView>()
        for (item in mListAudios) {
            if (item.isCheckChooseItem) {
                listAudio.add(item)
            }
        }
        return listAudio
    }

    suspend fun play(pos: Int) {
        val audioItem = getPreviewAudioCutterView()[pos]
        ManagerFactory.getDefaultAudioPlayer().play(audioItem.audioFile)

    }

    fun pause() {
        ManagerFactory.getDefaultAudioPlayer().pause()
    }

    fun resume() {
        ManagerFactory.getDefaultAudioPlayer().resume()
    }


    override fun onReceivedAction(fragmentMeta: FragmentMeta) {
        Log.d("TAG", "onReceivedAction: receive data")
        if (fragmentMeta.action.equals("ACTION_DELETE")) {
            val audio = fragmentMeta.data as AudioCutterView
            //getlistAfterReceive(audio)
            Log.d("TAG", "onReceivedAction: ${audio.audioFile.fileName}")
            super.onReceivedAction(fragmentMeta)
        }
    }

   /* private fun getlistAfterReceive(item: AudioCutterView): List<AudioCutterView>? {

        val rs1 = getPreviewAudioCutterView().indexOf(item)
        var index = 0
        mListAudioPrev.forEach {
            if (item.audioFile.fileName == it.audioFile.fileName) {
                index = mListAudioPrev.indexOf(it)
            }
        }
        mListAudioPrev.removeAt(index)
        mListAudioPrev.add(
            index,
            AudioCutterView(item.audioFile, PlayerState.IDLE, false)
        )
        val rs2 = mListAudioPrev.indexOf(item)
        mListIndexChoose.remove(mListAudioPrev.indexOf(item))
        count--
        _countItem.postValue(count)
        Log.d(TAG, "getlistAfterReceive: listTmp ${rs1}  -- rs2 $rs2")


        return mListAudioPrev
    }*/


}