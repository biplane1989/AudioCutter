package com.example.audiocutter.core.result

import androidx.lifecycle.LiveData
import com.example.audiocutter.functions.resultscreen.ConvertingItem
import com.example.audiocutter.functions.resultscreen.CuttingConfig
import com.example.audiocutter.functions.resultscreen.MergingConfig
import com.example.audiocutter.functions.resultscreen.MixingConfig
import com.example.audiocutter.objects.AudioFile
import java.io.File

interface AudioEditorManager {
    fun cutAudio(audioFile: AudioFile, cuttingConfig: CuttingConfig, outFile: File)
    fun mixAudio(audioFile1: AudioFile, audioFile2: AudioFile, mixingConfig: MixingConfig, outFile: File)
    fun mergeAudio(listAudioFiles: List<AudioFile>, mergingConfig: MergingConfig, outFile: File)
    fun cancel(int: Int)
    fun getCurrentProcessingItem(): LiveData<ConvertingItem>
    fun getListCuttingItems(): LiveData<List<ConvertingItem>>
    fun getListMergingItems(): LiveData<List<ConvertingItem>>
    fun getListMixingItems(): LiveData<List<ConvertingItem>>
}