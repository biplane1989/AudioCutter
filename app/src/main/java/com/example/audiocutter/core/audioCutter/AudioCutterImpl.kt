package com.example.audiocutter.core.audioCutter

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.audiocutter.core.manager.AudioCutConfig
import com.example.audiocutter.core.manager.AudioCutter
import com.example.audiocutter.core.manager.AudioMergingInfo
import com.example.audiocutter.core.manager.AudioMixConfig
import com.example.audiocutter.objects.AudioFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class AudioCutterImpl : AudioCutter {
    private var audioFileUpdate = MutableLiveData<AudioMergingInfo>()
    private val itemMergeInfo = AudioMergingInfo(null, 0)

    override suspend fun cut(audioFile: AudioFile, audioCutConfig: AudioCutConfig): AudioFile {
        return dataProcessing(audioFile)
    }

    override suspend fun mix(
        audioFile1: AudioFile,
        audioFile2: AudioFile,
        audioMixConfig: AudioMixConfig
    ): AudioFile {
       return dataProcessing(audioFile1)
    }

    override suspend fun merge(listAudioFile: List<AudioFile>, fileName: String): AudioFile {
        return dataProcessing(listAudioFile[0])
    }

    override fun getAudioMergingInfo(): LiveData<AudioMergingInfo> {
        return audioFileUpdate
    }

    private suspend fun dataProcessing(audioFile: AudioFile): AudioFile {
        return withContext(Dispatchers.Default) {
            var count = 0
            while (count <= 10) {
                updateItemLiveData(audioFile, count * 10)
                delay(1000)
                count++
            }
            audioFile
        }
    }

    private fun updateItemLiveData(audioFile: AudioFile, percent: Int) {
        itemMergeInfo.audioFile = audioFile
        itemMergeInfo.percent = percent
        audioFileUpdate.postValue(itemMergeInfo)
    }
}