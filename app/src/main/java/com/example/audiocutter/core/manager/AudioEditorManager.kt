package com.example.audiocutter.core.manager

import androidx.lifecycle.LiveData
import com.example.audiocutter.functions.resultscreen.objects.ConvertingItem
import com.example.audiocutter.objects.AudioFile
import com.example.core.core.AudioCutConfig
import com.example.core.core.AudioFormat
import com.example.core.core.AudioMixConfig
import java.io.File

interface AudioEditorManager {
    fun cutAudio(audioFile: AudioFile, cuttingConfig: AudioCutConfig, outFile: File, fileName: String)
    fun mixAudio(audioFile1: AudioFile, audioFile2: AudioFile, mixingConfig: AudioMixConfig, outFile: AudioFile, fileName: String)
    fun mergeAudio(listAudioFiles: List<AudioFile>, autoFormat: AudioFormat, outFilePath: AudioFile, fileName: String)
    fun cancel(int: Int)
    fun getCurrentProcessingItem(): LiveData<ConvertingItem>
    fun getListCuttingItems(): LiveData<List<ConvertingItem>>
    fun getListMergingItems(): LiveData<List<ConvertingItem>>
    fun getListMixingItems(): LiveData<List<ConvertingItem>>
    fun getIDProcessingItem(): Int
    fun getConvertingItem(): LiveData<ConvertingItem>
}