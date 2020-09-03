package com.example.audiocutter.core.manager.fake

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.audiocutter.core.manager.*
import com.example.audiocutter.objects.AudioFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File

class FakeAudioCutter : AudioCutter {
    private val audioCuttingLiveData = MutableLiveData<List<OutputAudioInfo>>()
    private val audioMergingLiveData = MutableLiveData<List<OutputAudioInfo>>()
    private val audioMixingLiveData = MutableLiveData<List<OutputAudioInfo>>()

    init {
        val listCuttingAudio = ArrayList<OutputAudioInfo>()
        val listMergingAudio = ArrayList<OutputAudioInfo>()
        val listMixingAudio = ArrayList<OutputAudioInfo>()

        listCuttingAudio.add(OutputAudioInfo(AudioFile(File(""), "file_name1", 10000, 128), 50))
        listCuttingAudio.add(OutputAudioInfo(AudioFile(File(""), "file_name2", 10000, 128), 100))
        audioCuttingLiveData.postValue(listCuttingAudio)

        listMergingAudio.add(OutputAudioInfo(AudioFile(File(""), "file_name3", 10000, 128), 50))
        listMergingAudio.add(OutputAudioInfo(AudioFile(File(""), "file_name4", 10000, 128), 100))
        audioMergingLiveData.postValue(listMergingAudio)

        listMixingAudio.add(OutputAudioInfo(AudioFile(File(""), "file_name5", 10000, 128), 50))
        listMixingAudio.add(OutputAudioInfo(AudioFile(File(""), "file_name6", 10000, 128), 100))
        audioMixingLiveData.postValue(listMixingAudio)

    }


    override suspend fun cut(audioFile: AudioFile, audioCutConfig: AudioCutConfig, audioProcessListener: AudioProcessListener): AudioFile {
        return withContext(Dispatchers.Default) {
            audioProcessListener.onStart()
            var count = 0
            while (count < 10) {
                audioProcessListener.onProcessing(count * 10)
                delay(1000)
                count++
            }

            audioProcessListener.onFinish()
            audioFile
        }
    }

    override suspend fun mix(audioFile1: AudioFile, audioFile2: AudioFile, audioMixConfig: AudioMixConfig, audioProcessListener: AudioProcessListener): AudioFile {
        return withContext(Dispatchers.Default) {
            audioProcessListener.onStart()
            var count = 0
            while (count < 10) {
                audioProcessListener.onProcessing(count * 10)
                delay(1000)
                count++
            }

            audioProcessListener.onFinish()
            audioFile1
        }
    }

    override suspend fun merge(listAudioFile: List<AudioFile>, fileName: String, audioProcessListener: AudioProcessListener): AudioFile {
        return withContext(Dispatchers.Default) {
            audioProcessListener.onStart()
            var count = 0
            while (count < 10) {
                audioProcessListener.onProcessing(count * 10)
                delay(1000)
                count++
            }

            audioProcessListener.onFinish()
            listAudioFile[0]
        }
    }

    override fun getListAudioCuttingInfo(): LiveData<List<OutputAudioInfo>> {
        return audioCuttingLiveData
    }

    override fun getListAudioMergingInfo(): LiveData<List<OutputAudioInfo>> {
        return audioMergingLiveData
    }

    override fun getListAudioMixingInfo(): LiveData<List<OutputAudioInfo>> {
        return audioMixingLiveData
    }
}