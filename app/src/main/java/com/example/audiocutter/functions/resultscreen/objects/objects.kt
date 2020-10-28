package com.example.audiocutter.functions.resultscreen.objects

import com.example.audiocutter.objects.AudioFile
import com.example.core.core.AudioCutConfig
import com.example.core.core.AudioFormat
import com.example.core.core.AudioMixConfig
import java.io.File

enum class ConvertingState {
    WAITING, PROGRESSING, SUCCESS, ERROR
}

class CuttingConvertingItem(id: Int, state: ConvertingState, percent: Int, outFile: AudioFile, val cuttingConfig: AudioCutConfig, var fileName1: String) : ConvertingItem(id, state, percent, outFile, fileName1)
class MixingConvertingItem(id: Int, state: ConvertingState, percent: Int, outFile: AudioFile, val audioFile1: AudioFile, val audioFile2: AudioFile, val mixingConfig: AudioMixConfig, var filename: String) : ConvertingItem(id, state, percent, outFile, filename)
class MergingConvertingItem(id: Int, state: ConvertingState, percent: Int, outFile: AudioFile, val listAudioFiles: List<AudioFile>, val audioFormat: AudioFormat, var fileName1: String) : ConvertingItem(id, state, percent, outFile, fileName1)

open class ConvertingItem(var id: Int = 0, var state: ConvertingState, var percent: Int, var audioFile: AudioFile, var fileName: String)