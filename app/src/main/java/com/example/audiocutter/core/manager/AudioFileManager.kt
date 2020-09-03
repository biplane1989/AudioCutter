package com.example.audiocutter.core.manager

import androidx.lifecycle.LiveData
import com.example.audiocutter.objects.AudioFile

interface AudioFileManager {
    suspend fun findAllAudioFiles(): LiveData<List<AudioFile>>
}