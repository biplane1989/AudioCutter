package com.example.audiocutter.core.manager.fake

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.core.core.*
import java.io.File

class FakeAudioCutter : AudioCutter {
    private val audioCuttingLiveData = MutableLiveData<List<OutputAudioInfo>>()
    private val audioMergingLiveData = MutableLiveData<List<OutputAudioInfo>>()
    private val audioMixingLiveData = MutableLiveData<List<OutputAudioInfo>>()

    init {
        val listCuttingAudio = ArrayList<OutputAudioInfo>()
        val listMergingAudio = ArrayList<OutputAudioInfo>()
        val listMixingAudio = ArrayList<OutputAudioInfo>()

        listCuttingAudio.add(OutputAudioInfo(AudioCore(File(""), "file_name1", 10000, 128), 50))
        listCuttingAudio.add(OutputAudioInfo(AudioCore(File(""), "file_name2", 10000, 128), 100))
        audioCuttingLiveData.postValue(listCuttingAudio)

        listMergingAudio.add(OutputAudioInfo(AudioCore(File(""), "file_name3", 10000, 128), 50))
        listMergingAudio.add(OutputAudioInfo(AudioCore(File(""), "file_name4", 10000, 128), 100))
        audioMergingLiveData.postValue(listMergingAudio)

        listMixingAudio.add(OutputAudioInfo(AudioCore(File(""), "file_name5", 10000, 128), 50))
        listMixingAudio.add(OutputAudioInfo(AudioCore(File(""), "file_name6", 10000, 128), 100))
        audioMixingLiveData.postValue(listMixingAudio)

    }

    override suspend fun cut(audioFile: AudioCore, audioCutConfig: AudioCutConfig): AudioCore {
        TODO("Not yet implemented")
    }

    override suspend fun merge(listAudioFile: List<AudioCore>, fileName: String, audioFormat: AudioFormat, pathFolder: String): AudioCore {
        TODO("Not yet implemented")
    }

    override suspend fun mix(audioFile1: AudioCore, audioFile2: AudioCore, audioMixConfig: AudioMixConfig): AudioCore {
        TODO("Not yet implemented")
    }

    override suspend fun cancelTask(): Boolean {
        TODO("Not yet implemented")
    }


    override fun getAudioMergingInfo(): LiveData<AudioMergingInfo> {
        TODO("Not yet implemented")
    }
}