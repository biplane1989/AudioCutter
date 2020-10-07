package com.example.audiocutter.core.manager

import androidx.lifecycle.LiveData
import com.example.audiocutter.core.audioManager.Folder
import com.example.audiocutter.core.audioManager.StateFile
import com.example.audiocutter.objects.AudioFile
import java.io.File
interface AudioFileManager {
    fun findAllAudioFiles(): LiveData<List<AudioFile>>
    fun buildAudioFile(filePath: String): AudioFile
    suspend fun saveFile(audioFile: AudioFile, typeFile: Folder): StateFile
    suspend fun deleteFile(listAudioFile: List<AudioFile>, typeFile: Folder): Boolean
    suspend fun getListAudioFileByType(typeFile: Folder): LiveData<List<AudioFile>>
    suspend fun getAllListByType()
            : LiveData<List<AudioFile>>
    fun getDurationByPath(itemFile: File?): String
}