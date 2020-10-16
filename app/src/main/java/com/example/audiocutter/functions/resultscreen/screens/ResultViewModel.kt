package com.example.audiocutter.functions.resultscreen.screens

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.example.audiocutter.base.BaseViewModel
import com.example.audiocutter.core.ManagerFactory
import com.example.audiocutter.core.manager.PlayerInfo
import com.example.audiocutter.core.manager.PlayerState
import com.example.audiocutter.functions.contacts.objects.SelectItemView
import com.example.audiocutter.functions.resultscreen.objects.ConvertingItem
import com.example.audiocutter.functions.resultscreen.objects.ConvertingState
import com.example.audiocutter.functions.resultscreen.objects.CuttingConvertingItem

class ResultViewModel : BaseViewModel() {

    lateinit var convertingItem: ConvertingItem
    val progressLivedata: MutableLiveData<ConvertingItem> = MutableLiveData()

    val processObserver = Observer<ConvertingItem> { data ->
        convertingItem = data
    }

    fun getData(): LiveData<ConvertingItem> {
        ManagerFactory.getAudioEditorManager().getCurrentProcessingItem()
            .observe(this, processObserver)

        return ManagerFactory.getAudioEditorManager().getCurrentProcessingItem()
    }

    fun getIDProgressItem(): Int {
        return ManagerFactory.getAudioEditorManager().getIDProcessingItem()
    }

    // chuyen trang thai play nhac
    fun playAudio() {
        convertingItem = ManagerFactory.getAudioEditorManager().getConvertingItem()
        runOnBackground {
            ManagerFactory.getAudioPlayer().play(convertingItem.audioFile)
        }
    }

    fun pauseAudio() {
        runOnBackground {
            ManagerFactory.getAudioPlayer().pause()
        }
    }

    fun stopAudio() {
        runOnBackground {
            ManagerFactory.getAudioPlayer().stop()
        }
//        val selectItemView = mListAudioFileView.get(position).copy()
//        val itemLoadStatus = selectItemView.selectItemStatus.copy()
//        itemLoadStatus.playerState = PlayerState.IDLE
//        selectItemView.selectItemStatus = itemLoadStatus

//        mListAudioFileView.set(position, selectItemView)
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