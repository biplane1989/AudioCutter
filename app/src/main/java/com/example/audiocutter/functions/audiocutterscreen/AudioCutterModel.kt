package com.example.audiocutter.functions.audiocutterscreen

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.audiocutter.core.audioManager.AudioFileManagerImpl
import com.example.audiocutter.objects.AudioFile

class AudioCutterModel : ViewModel() {
    private val TAG = AudioCutterModel::class.java.name

     suspend fun getAllaudioFile(): LiveData<List<AudioFile>> {
        return AudioFileManagerImpl.findAllAudioFiles()
    }

     suspend fun getAllFileByType(): LiveData<List<AudioFile>> {
        return AudioFileManagerImpl.getAllListByType()
    }
}