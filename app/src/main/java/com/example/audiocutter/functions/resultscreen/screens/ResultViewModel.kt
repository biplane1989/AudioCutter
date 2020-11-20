package com.example.audiocutter.functions.resultscreen.screens

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.example.audiocutter.base.BaseAndroidViewModel
import com.example.audiocutter.core.manager.ManagerFactory
import com.example.audiocutter.core.manager.PlayerInfo
import com.example.audiocutter.core.manager.PlayerState
import com.example.audiocutter.functions.resultscreen.objects.ConvertingItem
import com.example.audiocutter.functions.resultscreen.objects.ConvertingState
import com.example.audiocutter.objects.AudioFile

class ResultViewModel(application: Application) : BaseAndroidViewModel(application) {

    private val mContext = getApplication<Application>().applicationContext
    private val audioPlayer = ManagerFactory.newAudioPlayer()
    private val audioEditorManager = ManagerFactory.getAudioEditorManager()

    // tao ra 3 live data cho 3 truong hop status cua ConvertingItem
    private val processDoneLiveData = MutableLiveData<AudioFile>()
    private val processingLiveData = MutableLiveData<ConvertingItem>()
    private val pendingProcessLiveData = MutableLiveData<String>()

    private val editProcessObserver = Observer<ConvertingItem> { convertingItem ->
        convertingItem?.let {
            Log.d("giangtd", " ResultViewModel  :  STATE ${it.state}")

            when (it.state) {
                ConvertingState.PROGRESSING -> {
                    val latestConvertingItem = audioEditorManager.getLatestConvertingItem()
                    latestConvertingItem?.let { item ->

                        if (item.id == it.id) {         // neu id item loading = item cuoi cung
                            processingLiveData.postValue(it)
                        } else {
                            pendingProcessLiveData.postValue(item.getFileName())
                        }

                    }
                }
                ConvertingState.SUCCESS -> {
                    val latestConvertingItem = audioEditorManager.getLatestConvertingItem()
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
        audioPlayer.init(mContext)
        audioEditorManager.getCurrentProcessingItem().observeForever(editProcessObserver)
    }

    fun init(arg: ResultScreenArgs) {       // nhan du lieu tu screen khac truyen den

        when (arg.type) {
            ResultScreen.CUT -> {
                if (arg.listAudioPath.size == 1) {
                    val audioFile = ManagerFactory.getAudioFileManager()
                        .buildAudioFile(arg.listAudioPath[0])
                    ManagerFactory.getAudioEditorManager().cutAudio(audioFile, arg.cuttingConfig!!)
                }
            }
            ResultScreen.MER -> {
                if (arg.listAudioPath.size >= 2) {
                    val listAudio = ArrayList<AudioFile>()
                    for (item in arg.listAudioPath) {
                        listAudio.add(ManagerFactory.getAudioFileManager().buildAudioFile(item))
                    }
                    ManagerFactory.getAudioEditorManager()
                        .mergeAudio(listAudio, arg.mergingConfig!!)
                }
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
                        audioPlayer.play(AudioFile(it.file, it.fileName, it.size, it.bitRate, it.duration, Uri.parse(it.file.absolutePath)))
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
        audioPlayer.seek(cusorPos)
    }

    fun getPlayerInfo(): LiveData<PlayerInfo> {
        return audioPlayer.getPlayerInfo()
    }

    fun setRingTone(): Boolean {
        ManagerFactory.getAudioEditorManager().getLatestConvertingItem()?.outputAudioFile?.let {
            return ManagerFactory.getRingtoneManager().setRingTone(it)
        }
        return false
    }

    fun setAlarm(): Boolean {
        ManagerFactory.getAudioEditorManager().getLatestConvertingItem()?.outputAudioFile?.let {
            return ManagerFactory.getRingtoneManager().setAlarmManager(it)
        }
        return false
    }

    fun setNotification(): Boolean {
        ManagerFactory.getAudioEditorManager().getLatestConvertingItem()?.outputAudioFile?.let {
            return ManagerFactory.getRingtoneManager().setNotificationSound(it)
        }
        return false
    }

    override fun onCleared() {
        super.onCleared()
        audioPlayer.stop()
        audioEditorManager.getCurrentProcessingItem().removeObserver(editProcessObserver)
    }

}