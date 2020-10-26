package com.example.audiocutter.functions.resultscreen.screens

import android.net.Uri
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

    val TAG = "giangtd"
    lateinit var convertingItem: ConvertingItem

    fun getData(): LiveData<ConvertingItem> {
        return ManagerFactory.getAudioEditorManager().getCurrentProcessingItem()
    }

    fun getIDProgressItem(): Int {
        return ManagerFactory.getAudioEditorManager().getIDProcessingItem()
    }

    // chuyen trang thai play nhac
    fun playAudio() {
        runOnBackground {
            convertingItem = ManagerFactory.getAudioEditorManager().getConvertingItem()
            ManagerFactory.getAudioPlayer()
                .play(AudioFile(convertingItem.audioFile.file, convertingItem.audioFile.fileName, convertingItem.audioFile.size, convertingItem.audioFile.bitRate, convertingItem.audioFile.time, Uri.parse(convertingItem.audioFile.file.absolutePath)))
        }
    }

    fun pauseAudio() {
        runOnBackground {
            ManagerFactory.getAudioPlayer().pause()
        }
    }

    fun stopAudio() {
//        runOnBackground {
        ManagerFactory.getAudioPlayer().stop()
//        }
    }

    fun resumeAudio() {

        runOnBackground {
            ManagerFactory.getAudioPlayer().resume()
        }
    }

    fun seekToAudio(cusorPos: Int) {
        runOnBackground {
            ManagerFactory.getAudioPlayer().seek(cusorPos)
        }
    }

    fun getPlayerInfo(): LiveData<PlayerInfo> {
        return ManagerFactory.getAudioPlayer().getPlayerInfo()
    }

}