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

//    fun getPathSaveFile(): String {
//
//        return "Save Path: " + _listMusic.get(0).audioFile.file.absoluteFile.toString()
//    }
}