package com.example.audiocutter.functions.resultscreen.objects

import com.example.audiocutter.objects.AudioFile

data class CuttingConfig(val a: Int)

data class MixingConfig(val a: Int)

data class MergingConfig(val a: Int)

enum class ConvertingState {
    WAITING, PROGRESSING, SUCCESS, ERROR
}

class CuttingConvertingItem(id: Int, state: ConvertingState, percent: Int, outAudioFile: AudioFile, val cuttingConfig: CuttingConfig) : ConvertingItem(id, state, percent, outAudioFile)
class MixingConvertingItem(id: Int, state: ConvertingState, percent: Int, outAudioFile: AudioFile, val audioFile1: AudioFile, val audioFile2: AudioFile, val mixingConfig: MixingConfig) : ConvertingItem(id, state, percent, outAudioFile)
class MergingConvertingItem(id: Int, state: ConvertingState, percent: Int, outAudioFile: AudioFile, val listAudioFiles: List<AudioFile>, val mergingConfig: MergingConfig) : ConvertingItem(id, state, percent, outAudioFile)

open class ConvertingItem(var id: Int = 0, var state: ConvertingState, var percent: Int, var audioFile: AudioFile)