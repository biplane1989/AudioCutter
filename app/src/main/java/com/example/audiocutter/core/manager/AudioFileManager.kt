package com.example.audiocutter.core.manager

import android.content.Context
import androidx.lifecycle.LiveData
import com.example.audiocutter.objects.AudioFile

interface AudioFileManager {
    suspend fun findAllAudioFiles(context: Context): LiveData<List<AudioFile>>

    fun buildAudioFile(filePath: String): AudioFile
}