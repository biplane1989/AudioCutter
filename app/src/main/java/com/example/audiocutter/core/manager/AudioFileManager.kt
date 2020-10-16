package com.example.audiocutter.core.manager

import android.content.Context
import androidx.lifecycle.LiveData
import com.example.audiocutter.core.audioManager.Folder
import com.example.audiocutter.core.audioManager.StateFile
import com.example.audiocutter.objects.AudioFile
import com.example.audiocutter.objects.AudioFileScans
import java.io.File

interface AudioFileManager {
    fun init(appContext: Context)
    fun findAllAudioFiles(): LiveData<AudioFileScans>
    fun buildAudioFile(filePath: String): AudioFile
    suspend fun saveFile(audioFile: AudioFile, typeFile: Folder): StateFile
    fun deleteFile(listAudioFile: List<AudioFile>, typeFile: Folder): Boolean
    fun getListAudioFileByType(typeFile: Folder): LiveData<AudioFileScans>
    fun getInfoAudioFile(itemFile: File?, type: Int): String?
    fun getDateCreatFile(file:File?): String?
    fun getParentFile(typeFile: Folder): String
    fun getPathParentFileByName(name:String, typeFile: Folder): String

}