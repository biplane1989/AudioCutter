package com.example.audiocutter.functions.resultscreen.objects

import com.example.audiocutter.objects.AudioFile
import com.example.core.core.*

enum class ConvertingState {
    WAITING, PROGRESSING, SUCCESS, ERROR
}

class CuttingConvertingItem(id: Int, state: ConvertingState, percent: Int, val audioFile: AudioFile, val cuttingConfig: AudioCutConfig, outputAudioFile: AudioFile? = null, typeAudio: Int? = 0) : ConvertingItem(id, state, percent, cuttingConfig.bitRate, outputAudioFile, typeAudio)

class MixingConvertingItem(id: Int, state: ConvertingState, percent: Int, val audioFile1: AudioFile, val audioFile2: AudioFile, val mixingConfig: AudioMixConfig, outputAudioFile: AudioFile? = null, typeAudio: Int? = 2) : ConvertingItem(id, state, percent, mixingConfig.bitRate, outputAudioFile, typeAudio)

class MergingConvertingItem(id: Int, state: ConvertingState, percent: Int, val listAudioFiles: List<AudioFile>, val mergingConfig: AudioMergingConfig, outputAudioFile: AudioFile? = null, typeAudio: Int? = 1) : ConvertingItem(id, state, percent, mergingConfig.bitRate, outputAudioFile, typeAudio)

abstract class ConvertingItem(var id: Int = 0, var state: ConvertingState, var percent: Int, var bitRate: BitRate, var outputAudioFile: AudioFile? = null, var typeAudio: Int?) {
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