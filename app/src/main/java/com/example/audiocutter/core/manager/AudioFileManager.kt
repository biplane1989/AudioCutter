package com.example.audiocutter.core.manager

import android.content.Context
import androidx.lifecycle.LiveData
import com.example.audiocutter.objects.AudioFile

interface AudioFileManager {
    suspend fun findAllAudioFiles(context: Context): LiveData<List<AudioFile>>

    fun buildAudioFile(filePath: String): AudioFile

    suspend fun getListAudioCutter(): LiveData<List<AudioFile>>
    suspend fun getListAudioMerger(): LiveData<List<AudioFile>>
    suspend fun getListAudioMixer(): LiveData<List<AudioFile>>
}