package com.example.audiocutter.functions.resultscreen.objects

import com.example.audiocutter.objects.AudioFile
import com.example.core.core.*

enum class ConvertingState {
    WAITING, PROGRESSING, SUCCESS, ERROR
}

class CuttingConvertingItem(
    id: Int,
    state: ConvertingState,
    percent: Int,
    val audioFile: AudioFile,
    val cuttingConfig: AudioCutConfig,
    outputAudioFile: AudioFile? = null
) : ConvertingItem(id, state, percent, cuttingConfig.bitRate, outputAudioFile)

class MixingConvertingItem(
    id: Int,
    state: ConvertingState,
    percent: Int,
    val audioFile1: AudioFile,
    val audioFile2: AudioFile,
    val mixingConfig: AudioMixConfig,
    outputAudioFile: AudioFile? = null
) : ConvertingItem(id, state, percent, mixingConfig.bitRate, outputAudioFile)

class MergingConvertingItem(
    id: Int,
    state: ConvertingState,
    percent: Int,
    val listAudioFiles: List<AudioFile>,
    val mergingConfig: AudioMergingConfig,
    outputAudioFile: AudioFile? = null
) : ConvertingItem(id, state, percent, mergingConfig.bitRate, outputAudioFile)

abstract class ConvertingItem(
    var id: Int = 0,
    var state: ConvertingState,
    var percent: Int,
    var bitRate: BitRate,
    var outputAudioFile: AudioFile? = null
) {
    fun getFileName(): String {
        var fileName = ""
        if (this is CuttingConvertingItem) {
            fileName = cuttingConfig.fileName
        }
        if (this is MixingConvertingItem) {
            fileName = mixingConfig.fileName
        }
        if (this is MergingConvertingItem) {
            fileName = mergingConfig.fileName
        }
        return fileName
    }
}