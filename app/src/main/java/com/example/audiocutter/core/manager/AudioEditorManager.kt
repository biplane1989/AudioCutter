package com.example.audiocutter.core.manager

import androidx.lifecycle.LiveData
import com.example.audiocutter.functions.resultscreen.objects.ConvertingItem
import com.example.audiocutter.objects.AudioFile
import com.example.core.core.AudioCutConfig
import com.example.core.core.AudioFormat
import com.example.core.core.AudioMergingConfig
import com.example.core.core.AudioMixConfig

interface AudioEditorManager {
    fun cutAudio(audioFile: AudioFile, cuttingConfig: AudioCutConfig)
    fun mixAudio(audioFile1: AudioFile, audioFile2: AudioFile, mixingConfig: AudioMixConfig)
    fun mergeAudio(listAudioFiles: List<AudioFile>, mergingConfig: AudioMergingConfig)
    fun cancel(int: Int)
    fun getCurrentProcessingItem(): LiveData<ConvertingItem?>
    fun getListCuttingItems(): LiveData<List<ConvertingItem>>
    fun getListMergingItems(): LiveData<List<ConvertingItem>>
    fun getListMixingItems(): LiveData<List<ConvertingItem>>
    fun getLatestConvertingItem(): ConvertingItem?
}