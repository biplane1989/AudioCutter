package com.example.audiocutter.core.manager

import androidx.lifecycle.LiveData
import com.example.audiocutter.core.audioManager.Folder
import com.example.audiocutter.objects.AudioFile

interface AudioFileManager {
    fun findAllAudioFiles(): LiveData<List<AudioFile>>
    fun buildAudioFile(filePath: String): AudioFile
    fun saveFile(audioFile: AudioFile, typeFile: Folder): Boolean
    suspend fun deleteFile(listAudioFile: List<AudioFile>, typeFile: Folder): Boolean
    suspend fun getListAudioFileByType(typeFile: Folder): LiveData<List<AudioFile>>
}