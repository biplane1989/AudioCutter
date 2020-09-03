package com.example.audiocutter.core.manager

import androidx.lifecycle.LiveData
import com.example.audiocutter.objects.AudioFile
import java.text.FieldPosition

enum class Effect {
    OFF, AFTER_1_S, AFTER_2_S, AFTER_3_S, AFTER_4_S, AFTER_5_S, AFTER_6_S
}

enum class BitRate {
    _128kb, _64_kb
}

enum class AudioFormat {
    MP3, ACC
}

enum class MixSelector {
    LONGEST, SHORTEST
}

data class AudioCutConfig(val startPosition: Int, val endPosition: Int, val volumePercent: Int = 100, val fileName: String, val inEffect: Effect = Effect.OFF, val outEffect: Effect = Effect.OFF, val bitRate: BitRate = BitRate._128kb, val format: AudioFormat = AudioFormat.MP3)
data class AudioMixConfig(val selector: MixSelector = MixSelector.LONGEST)
data class OutputAudioInfo(val audioFile: AudioFile, var percent: Int)
interface AudioProcessListener {
    fun onStart()
    fun onProcessing(percent: Int)
    fun onFinish()
}

interface AudioCutter {
    suspend fun cut(audioFile: AudioFile, audioCutConfig: AudioCutConfig, audioProcessListener: AudioProcessListener): AudioFile
    suspend fun merge(listAudioFile: List<AudioFile>, fileName: String, audioProcessListener: AudioProcessListener): AudioFile
    suspend fun mix(audioFile1: AudioFile, audioFile2: AudioFile, audioMixConfig: AudioMixConfig, audioProcessListener: AudioProcessListener): AudioFile

    fun getListAudioCuttingInfo(): LiveData<List<OutputAudioInfo>>
    fun getListAudioMergingInfo(): LiveData<List<OutputAudioInfo>>
    fun getListAudioMixingInfo(): LiveData<List<OutputAudioInfo>>
}