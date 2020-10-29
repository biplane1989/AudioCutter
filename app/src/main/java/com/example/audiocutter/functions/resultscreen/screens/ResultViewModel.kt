package com.example.audiocutter.functions.resultscreen.screens

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.example.audiocutter.base.BaseViewModel
import com.example.audiocutter.core.manager.ManagerFactory
import com.example.audiocutter.core.manager.PlayerInfo
import com.example.audiocutter.functions.resultscreen.objects.ConvertingItem
import com.example.audiocutter.functions.resultscreen.objects.MixingConvertingItem
import com.example.audiocutter.objects.AudioFile

class ResultViewModel : BaseViewModel() {
    private val audioPlayer = ManagerFactory.newAudioPlayer()

    val TAG = "giangtd"

    fun getData(): LiveData<ConvertingItem> {
        return ManagerFactory.getAudioEditorManager().getCurrentProcessingItem()
    }

    fun getIDProgressItem(): Int {
        return ManagerFactory.getAudioEditorManager().getIDProcessingItem()
    }

    // chuyen trang thai play nhac
    fun playAudio(convertingItem: ConvertingItem) {
        runOnBackground {
            stopAudio()
//            convertingItem = ManagerFactory.getAudioEditorManager().getConvertingItem()
            Log.d("009", "convertingItem : " + convertingItem.audioFile.fileName + " file path: " + convertingItem.audioFile.file.absoluteFile)
            audioPlayer.play(AudioFile(convertingItem.audioFile.file, convertingItem.audioFile.fileName, convertingItem.audioFile.size, convertingItem.audioFile.bitRate, convertingItem.audioFile.time, Uri.parse(convertingItem.audioFile.file.absolutePath)))
        }
    }

    fun pauseAudio() {
        runOnBackground {
//            ManagerFactory.getAudioPlayer().pause()
            audioPlayer.pause()
        }
    }

    fun stopAudio() {
//        runOnBackground {
//        ManagerFactory.getAudioPlayer().stop()
        if (audioPlayer.getAudioIsPlaying()) {
            audioPlayer.stop()
        }
//        }
    }

    fun resumeAudio() {
        runOnBackground {
//            ManagerFactory.getAudioPlayer().resume()
            audioPlayer.resume()
        }
    }

    fun seekToAudio(cusorPos: Int) {
        runOnBackground {
//            ManagerFactory.getAudioPlayer().seek(cusorPos)
            audioPlayer.seek(cusorPos)
        }
    }

    fun getPlayerInfo(): LiveData<PlayerInfo> {
//        return ManagerFactory.getAudioPlayer().getPlayerInfo()
        return audioPlayer.getPlayerInfo()
    }

    override fun onCleared() {
        super.onCleared()
//        ManagerFactory.getAudioPlayer().stop()
        if (audioPlayer.getAudioIsPlaying()) {
            audioPlayer.stop()
        }
        Log.d("009", "onCleared: ")
    }

}