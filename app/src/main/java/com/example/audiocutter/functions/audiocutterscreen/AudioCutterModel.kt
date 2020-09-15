package com.example.audiocutter.functions.audiocutterscreen

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.example.audiocutter.core.audioManager.AudioFileManagerImpl
import com.example.audiocutter.core.audioplayer.AudioPlayerImpl
import com.example.audiocutter.core.manager.PlayerInfo
import com.example.audiocutter.objects.AudioFile

class AudioCutterModel : ViewModel() {
    private val TAG = AudioCutterModel::class.java.name

    suspend fun getAllAudioFile(): LiveData<List<AudioCutterView>> {
        return Transformations.map(AudioFileManagerImpl.findAllAudioFiles()) { listAudioFiles ->
            val listAudioCutterItem = ArrayList<AudioCutterView>()
            listAudioFiles.forEach {
                listAudioCutterItem.add(AudioCutterView(it))
            }
            listAudioCutterItem
        }
    }

    suspend fun getAllFileByType(): LiveData<List<AudioFile>> {
        return AudioFileManagerImpl.getAllListByType()
    }

    suspend fun getPlayInfo(): LiveData<PlayerInfo> {
        return AudioPlayerImpl.getPlayerInfo()
    }
}