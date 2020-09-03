package com.example.audiocutter.core.manager.fake

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.audiocutter.core.manager.AudioFileManager
import com.example.audiocutter.objects.AudioFile
import java.io.File

class FakeAudioFileManager : AudioFileManager {
    private val audioFileLiveData = MutableLiveData<List<AudioFile>>()

    init {
        val listAudioFile = ArrayList<AudioFile>()
        listAudioFile.add(AudioFile(File(""), "file_name1", 10000, 128))
        listAudioFile.add(AudioFile(File(""), "file_name2", 10000, 128))
        listAudioFile.add(AudioFile(File(""), "file_name3", 10000, 128))
        audioFileLiveData.postValue(listAudioFile)
    }

    override suspend fun findAllAudioFiles(): LiveData<List<AudioFile>> {
        return audioFileLiveData
    }
}