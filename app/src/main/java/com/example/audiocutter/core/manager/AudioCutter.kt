package com.example.audiocutter.core.manager

import androidx.lifecycle.LiveData
import com.example.audiocutter.objects.AudioFile

enum class Effect(val time: Int) {
    OFF(0), AFTER_1_S(1), AFTER_2_S(2), AFTER_3_S(3), AFTER_4_S(4), AFTER_5_S(5), AFTER_6_S(6)
}

enum class BitRate(val value: Int) {
    _128kb(128), _64kb(64), _32kb(32), _192kb(192), _256kb(256), _320kb(320)
}

enum class AudioFormat {
    MP3, ACC
}

enum class MixSelector(val type: String) {
    LONGEST("longest"), SHORTEST("shortest")
}

data class AudioCutConfig(
    val startPosition: Int,
    val endPosition: Int,
    val volumePercent: Int = 300,
    val fileName: String,
    val inEffect: Effect = Effect.OFF,
    val outEffect: Effect = Effect.OFF,
    val bitRate: BitRate = BitRate._128kb,
    val format: AudioFormat = AudioFormat.MP3
)

data class AudioMixConfig(
    val fileName: String,
    val selector: MixSelector = MixSelector.LONGEST,
    val volumePercent1: Int = 100,
    val volumePercent2: Int = 100
)

data class OutputAudioInfo(val audioFile: AudioFile, var percent: Int)
data class AudioMergingInfo(var audioFile: AudioFile?, var percent: Int)
interface AudioCutter {
    suspend fun cut(audioFile: AudioFile, audioCutConfig: AudioCutConfig): AudioFile
    suspend fun merge(listAudioFile: List<AudioFile>, fileName: String): AudioFile
    suspend fun mix(
        audioFile1: AudioFile,
        audioFile2: AudioFile,
        audioMixConfig: AudioMixConfig
    ): AudioFile

    suspend fun cancelTask()
    fun getAudioMergingInfo(): LiveData<AudioMergingInfo>
}