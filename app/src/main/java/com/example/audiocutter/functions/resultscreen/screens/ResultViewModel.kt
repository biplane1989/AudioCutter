package com.example.audiocutter.functions.resultscreen.screens

import android.net.Uri
import android.util.Log
import androidx.lifecycle.*
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseViewModel
import com.example.audiocutter.core.manager.ManagerFactory
import com.example.audiocutter.core.manager.PlayerInfo
import com.example.audiocutter.core.manager.PlayerState
import com.example.audiocutter.functions.resultscreen.objects.ConvertingItem
import com.example.audiocutter.functions.resultscreen.objects.ConvertingState
import com.example.audiocutter.objects.AudioFile

class ResultViewModel : BaseViewModel() {
    private val audioPlayer = ManagerFactory.newAudioPlayer()
    private val audioEditorManager = ManagerFactory.getAudioEditorManager()
    private val processDoneLiveData = MutableLiveData<AudioFile>()
    private val processingLiveData = MutableLiveData<ConvertingItem>()
    private val pendingProcessLiveData = MutableLiveData<String>()
    private val editProcessObserver = Observer<ConvertingItem> { convertingItem ->
        convertingItem?.let {
            Log.d("taih", "STATE ${it.state}")
            when (it.state) {
                ConvertingState.PROGRESSING -> {
                    val latestConvertingItem = audioEditorManager.getLatestConvertingItem().value
                    latestConvertingItem?.let { item ->
                        if (item.id == it.id) {
                            processingLiveData.postValue(it)
                        } else {
                            pendingProcessLiveData.postValue(item.getFileName())
                        }
                    }
                }
                ConvertingState.SUCCESS -> {
                    val latestConvertingItem = audioEditorManager.getLatestConvertingItem().value
                    latestConvertingItem?.let { item ->
                        if (item.id == it.id) {
                            processDoneLiveData.postValue(it.outputAudioFile)
                        }
                    }

                }
                ConvertingState.WAITING -> {

                }
                ConvertingState.ERROR -> {

                }
            }
        }
    }

    init {
        audioEditorManager.getCurrentProcessingItem().observeForever(editProcessObserver)
    }

    fun init(arg: ResultScreenArgs) {

        when (arg.type) {
            ResultScreen.CUT -> {
                if (arg.listAudioPath.size == 1) {
                    val audioFile = ManagerFactory.getAudioFileManager()
                        .buildAudioFile(arg.listAudioPath[0])
                    ManagerFactory.getAudioEditorManager().cutAudio(audioFile, arg.cuttingConfig!!)
                }
            }
            ResultScreen.MER -> {
                val listAudio = ArrayList<AudioFile>()
                for (item in arg.listAudioPath) {
                    listAudio.add(ManagerFactory.getAudioFileManager().buildAudioFile(item))
                }
                    /*   if (arg.listAudioPath.size == 2) {
                           val audioFile = ManagerFactory.getAudioFileManager()
                               .buildAudioFile(arg.listAudioPath[0])
                           val audioFile2 = ManagerFactory.getAudioFileManager()
                               .buildAudioFile(arg.listAudioPath[1])

                           listAudio.add(audioFile)
                           listAudio.add(audioFile2)*/
                    ManagerFactory.getAudioEditorManager()
                        .mergeAudio(listAudio, arg.mergingConfig!!)
//                }
            }
            ResultScreen.MIX -> {
                if (arg.listAudioPath.size == 2) {
                    val audioFile1 = ManagerFactory.getAudioFileManager()
                        .buildAudioFile(arg.listAudioPath[0])
                    val audioFile2 = ManagerFactory.getAudioFileManager()
                        .buildAudioFile(arg.listAudioPath[1])
                    ManagerFactory.getAudioEditorManager()
                        .mixAudio(audioFile1, audioFile2, arg.mixingConfig!!)
                }
            }
        }
    }

    fun getProcessDoneLiveData(): LiveData<AudioFile> {
        return processDoneLiveData
    }

    fun getProcessingLiveData(): LiveData<ConvertingItem> {
        return processingLiveData
    }

    fun getPendingProcessLiveData(): LiveData<String> {
        return pendingProcessLiveData
    }

    // chuyen trang thai play nhac
    suspend fun playAudio() {
        processDoneLiveData.value.let { audioFile ->
            audioFile?.let {
                when (audioPlayer.getPlayerInfoData().playerState) {
                    PlayerState.IDLE -> {
                        audioPlayer.play(AudioFile(it.file, it.fileName, it.size, it.bitRate, it.time, Uri.parse(it.file.absolutePath)))
                    }
                    PlayerState.PAUSE -> {
                        audioPlayer.resume()
                    }

                    PlayerState.PLAYING -> {
                        audioPlayer.pause()
                    }
                    PlayerState.PREPARING -> {

                    }
                }
            }

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
        audioPlayer.stop()
        audioEditorManager.getCurrentProcessingItem().removeObserver(editProcessObserver)
    }

}