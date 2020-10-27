package com.example.audiocutter.functions.resultscreen.objects

import com.example.audiocutter.objects.AudioFile
import com.example.core.core.AudioCutConfig
import com.example.core.core.AudioFormat
import com.example.core.core.AudioMixConfig
import java.io.File

enum class ConvertingState {
    WAITING, PROGRESSING, SUCCESS, ERROR
}

class CuttingConvertingItem(id: Int, state: ConvertingState, percent: Int, outFile: File, val cuttingConfig: AudioCutConfig) : ConvertingItem(id, state, percent, outFile)
class MixingConvertingItem(id: Int, state: ConvertingState, percent: Int, outFile: File, val audioFile1: AudioFile, val audioFile2: AudioFile, val mixingConfig: AudioMixConfig) : ConvertingItem(id, state, percent, outFile)
class MergingConvertingItem(id: Int, state: ConvertingState, percent: Int, outFile: File, val listAudioFiles: List<AudioFile>, val audioFormat: AudioFormat, val fileName: String) : ConvertingItem(id, state, percent, outFile)

open class ConvertingItem(var id: Int = 0, var state: ConvertingState, var percent: Int, var outFile: File)