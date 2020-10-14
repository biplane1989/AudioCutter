package com.example.core.core

import androidx.lifecycle.LiveData

enum class Effect(val time: Int) {
    OFF(0), AFTER_1_S(1), AFTER_2_S(2), AFTER_3_S(3), AFTER_4_S(4), AFTER_5_S(5), AFTER_6_S(6)
}

enum class BitRate(val value: Int) {
    _32kb(32), _64kb(64), _128kb(128), _192kb(192), _256kb(256), _320kb(320)
}

enum class AudioFormat(val type: String) {
    MP3(".mp3"), ACC(".m4a")
}

enum class MixSelector(val type: String) {
    LONGEST("longest"), SHORTEST("shortest")
}

enum class FFMpegState {
    IDE,RUNNING, CANCEL, FAIL
}

data class AudioCutConfig(
    var startPosition: Float,
    var endPosition: Float,
    var volumePercent: Int = 300,
    var fileName: String,
    var inEffect: Effect = Effect.OFF,
    var outEffect: Effect = Effect.OFF,
    var bitRate: BitRate = BitRate._128kb,
    var format: AudioFormat = AudioFormat.MP3,
    var pathFolder: String
)

data class AudioMixConfig(
    val fileName: String,
    val selector: MixSelector = MixSelector.LONGEST,
    val volumePercent1: Int = 100,
    val volumePercent2: Int = 100,
    val format: AudioFormat = AudioFormat.MP3,
    val pathFolder: String
)

data class OutputAudioInfo(val audioFile: AudioCore, var percent: Int)
data class AudioMergingInfo(var audioFile: AudioCore?, var percent: Int, var state: FFMpegState)
interface AudioCutter {
    suspend fun cut(audioFile: AudioCore, audioCutConfig: AudioCutConfig): AudioCore
    suspend fun merge(
        listAudioFile: List<AudioCore>,
        fileName: String,
        audioFormat: AudioFormat, pathFolder: String
    ): AudioCore

    suspend fun mix(
        audioFile1: AudioCore,
        audioFile2: AudioCore,
        audioMixConfig: AudioMixConfig
    ): AudioCore

    suspend fun cancelTask(): Boolean
    fun getAudioMergingInfo(): LiveData<AudioMergingInfo>
}