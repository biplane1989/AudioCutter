package com.example.audiocutter.functions.mystudio.audiocutter

import androidx.lifecycle.MutableLiveData
import com.example.audiocutter.base.BaseViewModel
import com.example.audiocutter.core.ManagerFactory
import com.example.audiocutter.functions.mystudio.AudioFileView

class AudioCutterViewModel : BaseViewModel() {

    private var listMusic: MutableLiveData<ArrayList<AudioFileView>> = MutableLiveData()
    private var _listMusic = ArrayList<AudioFileView>()

    init {
        listMusic.value = _listMusic
    }

    fun getListMusic(): MutableLiveData<ArrayList<AudioFileView>>? {
        return listMusic
    }

    fun getListSize(): Int {
        return listMusic.value!!.size
    }

    suspend fun getData() {
        val listsAudio = ManagerFactory.getAudioFileManager().getListAudioCutter().value
        _listMusic.clear()
        if (listsAudio!!.size > 0) {
            for (item in listsAudio) {
                _listMusic.add(AudioFileView(item, false))
            }
            listMusic.postValue(_listMusic)
        }
    }

    fun showPlayMusic(position: Int) {
        val audioFileView = _listMusic.get(position).copy()
        if (audioFileView.isExpanded == false) {
            audioFileView.isExpanded = true
        } else {
            audioFileView.isExpanded = false
        }
        _listMusic.set(position, audioFileView)
        listMusic.postValue(_listMusic)
    }

    fun playMusic(position: Int) {
        runOnBackground {
//            when (AudioPlayerImpl.mPlayerState) {
//                PlayerState.PAUSE -> {
//                    AudioPlayerImpl.play(_listMusic.get(position).audioFile)
//                }
//                PlayerState.IDLE -> {
//                }
//                PlayerState.PLAYING -> {
//                }
//            }

        }
    }
}