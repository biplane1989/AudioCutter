package com.example.audiocutter.core.audioCutter

import androidx.lifecycle.LiveData
import com.example.audiocutter.core.manager.*
import com.example.audiocutter.objects.AudioFile

class AudioCutterImpl : AudioCutter {
    override suspend fun cut(audioFile: AudioFile, audioCutConfig: AudioCutConfig): AudioFile {
        TODO("Not yet implemented")
    }

    override suspend fun mix(audioFile1: AudioFile, audioFile2: AudioFile, audioMixConfig: AudioMixConfig): AudioFile {
        TODO("Not yet implemented")
    }

    override suspend fun merge(listAudioFile: List<AudioFile>, fileName: String): AudioFile {
        TODO("Not yet implemented")
    }

    override fun getListAudioCuttingInfo(): LiveData<List<OutputAudioInfo>> {
        TODO("Not yet implemented")
    }

    override fun getListAudioMergingInfo(): LiveData<List<OutputAudioInfo>> {
        TODO("Not yet implemented")
    }

    override fun getListAudioMixingInfo(): LiveData<List<OutputAudioInfo>> {
        TODO("Not yet implemented")
    }

    override fun getAudioMergingInfo(): LiveData<AudioMergingInfo> {
        TODO("Not yet implemented")
    }
}