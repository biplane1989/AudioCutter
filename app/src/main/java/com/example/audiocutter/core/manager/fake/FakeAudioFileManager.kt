package com.example.audiocutter.core.manager.fake

import android.content.Context
import android.os.Environment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.audiocutter.core.manager.AudioFileManager
import com.example.audiocutter.objects.AudioFile
import java.io.File

class FakeAudioFileManager : AudioFileManager {
    private val audioFileLiveData = MutableLiveData<List<AudioFile>>()

//    val file = File(Environment.getExternalStorageDirectory().toString() + "/Music/aloha.mp3")
            val file = File(Environment.getExternalStorageDirectory().toString() + "/Download/lonely.mp3")
    init {
        val listAudioFile = ArrayList<AudioFile>()
        listAudioFile.add(AudioFile(file, "file_name1", 10000, 128))
        listAudioFile.add(AudioFile(file, "file_name2", 10000, 128))
        listAudioFile.add(AudioFile(file, "file_name3", 10000, 128))
        audioFileLiveData.postValue(listAudioFile)
    }

    override suspend fun findAllAudioFiles(context: Context): LiveData<List<AudioFile>> {
        return audioFileLiveData
    }

    override fun buildAudioFile(filePath: String): AudioFile {
        return AudioFile(File(filePath), "file_name1", 10000, 128)
    }

    override suspend fun getListAudioCutter(): LiveData<List<AudioFile>> {
        return audioFileLiveData
    }

    override suspend fun getListAudioMerger(): LiveData<List<AudioFile>> {
        return audioFileLiveData
    }

    override suspend fun getListAudioMixer(): LiveData<List<AudioFile>> {
        return audioFileLiveData
    }
}