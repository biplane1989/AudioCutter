package com.example.audiocutter.core.manager

import android.content.Context
import android.content.pm.ResolveInfo
import android.net.Uri
import androidx.lifecycle.LiveData
import com.example.audiocutter.functions.audiochooser.objects.ItemAppShare
import com.example.audiocutter.core.audiomanager.Folder
import com.example.audiocutter.core.audiomanager.StateFile
import com.example.audiocutter.objects.AudioFile
import com.example.audiocutter.objects.AudioFileScans
import java.io.File

interface AudioFileManager {
    fun init(appContext: Context)
    fun findAllAudioFiles(): LiveData<AudioFileScans>
    fun buildAudioFile(filePath: String): AudioFile
    fun deleteFile(listAudioFile: List<AudioFile>, typeFile: Folder): Boolean
    fun getListAudioFileByType(typeFile: Folder): LiveData<AudioFileScans>
    fun getInfoAudioFile(itemFile: File?, type: Int): String?
    fun getFolderPath(typeFile: Folder): String
    fun reNameToFileAudio(name: String, audioFile: AudioFile, typeFile: Folder): Boolean
    fun checkFileNameDuplicate(name: String, typeFile: Folder): Boolean
}