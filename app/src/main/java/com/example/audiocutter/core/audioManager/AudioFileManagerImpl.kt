package com.example.audiocutter.core.audioManager

import androidx.lifecycle.LiveData
import com.example.audiocutter.core.manager.AudioFileManager
import com.example.audiocutter.objects.AudioFile

class AudioFileManagerImpl : AudioFileManager{
    override suspend fun findAllAudioFiles(): LiveData<List<AudioFile>> {
        TODO("Not yet implemented")
    }

    override fun buildAudioFile(filePath: String): AudioFile {
        TODO("Not yet implemented")
    }
}