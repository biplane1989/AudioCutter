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

typealias BuildAudioCompleted = (AudioFile?) -> Unit

interface AudioFileManager {
    fun init(appContext: Context)
    fun findAllAudioFiles(): LiveData<AudioFileScans>
    fun findAudioFile(filePath: String): AudioFile?
    fun buildAudioFile(filePath: String, listener: BuildAudioCompleted)
    fun deleteFile(listAudioFile: List<AudioFile>, typeFile: Folder): Boolean
    fun getListAudioFileByType(typeFile: Folder): LiveData<AudioFileScans>
    fun getFolderPath(typeFile: Folder): String
    fun renameToFileAudio(newName: String, audioFile: AudioFile, typeFile: Folder): Boolean
    fun checkFileNameDuplicate(name: String, typeFile: Folder): Boolean
    fun hasUri(uri: String): Boolean
}