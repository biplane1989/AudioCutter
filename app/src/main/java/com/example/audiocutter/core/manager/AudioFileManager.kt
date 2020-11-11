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
    suspend fun saveFile(audioFile: AudioFile, typeFile: Folder): StateFile
    fun deleteFile(listAudioFile: List<AudioFile>, typeFile: Folder): Boolean
    fun getListAudioFileByType(typeFile: Folder): LiveData<AudioFileScans>
    fun getInfoAudioFile(itemFile: File?, type: Int): String?
    fun getDateCreatFile(file: File?): String?
    fun getFolderPath(typeFile: Folder): String
    fun insertFileToMediastore(file: File): Boolean
    fun getUriByPath(itemFile: File): Uri?
    fun shareFileAudio(audioFile: AudioFile): Boolean
    fun getListApprQueryReceiveData(): MutableList<ItemAppShare>
    fun getListReceiveData ():MutableList<ResolveInfo>
    fun reNameToFileAudio(name: String, audioFile: AudioFile, typeFile: Folder): Boolean
    fun checkFileNameDuplicate(name: String, typeFile: Folder): Boolean
    fun openWithApp( uri: Uri)

}