package com.example.audiocutter.core.manager

import androidx.lifecycle.LiveData
import com.example.audiocutter.functions.resultscreen.objects.ConvertingItem
import com.example.audiocutter.functions.resultscreen.objects.CuttingConfig
import com.example.audiocutter.functions.resultscreen.objects.MergingConfig
import com.example.audiocutter.objects.AudioFile
import com.example.core.core.AudioMixConfig
import java.io.File

interface AudioEditorManager {
    fun cutAudio(audioFile: AudioFile, cuttingConfig: CuttingConfig, outFile: File)
    fun mixAudio(audioFile1: AudioFile, audioFile2: AudioFile, mixingConfig: AudioMixConfig, outFile: AudioFile)
    fun mergeAudio(listAudioFiles: List<AudioFile>, mergingConfig: MergingConfig, outFile: AudioFile)
    fun cancel(int: Int)
    fun getCurrentProcessingItem(): LiveData<ConvertingItem>
    fun getListCuttingItems(): LiveData<List<ConvertingItem>>
    fun getListMergingItems(): LiveData<List<ConvertingItem>>
    fun getListMixingItems(): LiveData<List<ConvertingItem>>
    fun getIDProcessingItem(): Int
    fun getConvertingItem(): ConvertingItem
}