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
import com.google.android.material.snackbar.Snackbar

class ResultViewModel(application: Application) : BaseAndroidViewModel(application) {

    private val mContext = getApplication<Application>().applicationContext
    private val audioPlayer = ManagerFactory.newAudioPlayer()
    private val audioEditorManager = ManagerFactory.getAudioEditorManager()

    // tao ra 3 live data cho 3 truong hop status cua ConvertingItem
    private val processDoneLiveData = MutableLiveData<AudioFile>()
    private val processingLiveData = MutableLiveData<ConvertingItem>()
    private val pendingProcessLiveData = MutableLiveData<String>()

    private val errorLiveData = MutableLiveData<Boolean>()


    private val editProcessObserver = Observer<ConvertingItem?> { convertingItem ->
        convertingItem?.let {
            Log.d("giangtd", " ResultViewModel  :  STATE ${it.state}")

            when (it.state) {
                ConvertingState.PROGRESSING -> {
                    val latestConvertingItem = audioEditorManager.getLatestConvertingItem()
                    latestConvertingItem?.let { item ->

                        if (item.id == it.id) {         // neu id item loading = item cuoi cung
                            processingLiveData.postValue(it)
                        } else {
                            val convertingItem = ManagerFactory.getAudioEditorManager()
                                .getLatestConvertingItem()
                            pendingProcessLiveData.postValue(convertingItem?.getFileName())
                        }

                    }
                }
                ConvertingState.SUCCESS -> {
                    val latestConvertingItem = audioEditorManager.getLatestConvertingItem()
                    latestConvertingItem?.let { item ->
                        if (item.id == it.id) {
                            processDoneLiveData.postValue(item.outputAudioFile)
                        }
                    }

                }
                ConvertingState.WAITING -> {

                }
                ConvertingState.ERROR -> {
                    errorLiveData.postValue(true)
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
                        .findAudioFile(arg.listAudioPath[0])
                    audioFile?.let { audio ->
                        arg.cuttingConfig?.let {
                            ManagerFactory.getAudioEditorManager().cutAudio(audio, it)
                        }
                    }

                }
            }
            ResultScreen.MER -> {
                if (arg.listAudioPath.size >= 2) {
                    val listAudio = ArrayList<AudioFile>()
                    for (item in arg.listAudioPath) {
                        val audioFile = ManagerFactory.getAudioFileManager().findAudioFile(item)
                        audioFile?.let {
                            listAudio.add(it)
                        }

                    }
                    arg.mergingConfig?.let {
                        ManagerFactory.getAudioEditorManager().mergeAudio(listAudio, it)
                    }

                }
            }
            ResultScreen.MIX -> {
                if (arg.listAudioPath.size == 2) {
                    val audioFile1 = ManagerFactory.getAudioFileManager()
                        .findAudioFile(arg.listAudioPath[0])
                    val audioFile2 = ManagerFactory.getAudioFileManager()
                        .findAudioFile(arg.listAudioPath[1])
                    audioFile1?.let {
                        audioFile2?.let {
                            arg.mixingConfig?.let {
                                ManagerFactory.getAudioEditorManager()
                                    .mixAudio(audioFile1, audioFile2, it)
                            }
                        }
                    }

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

    fun getErrorLiveData(): LiveData<Boolean> {
        return errorLiveData
    }

    // chuyen trang thai play nhac
    suspend fun playAudio() {

        val audioFile = ManagerFactory.getAudioEditorManager()
            .getLatestConvertingItem()?.outputAudioFile
        audioFile?.let {

            audioPlayer.play(audioFile)
        }
    }

    suspend fun playAudioByPositition(audioFile: AudioFile, position: Int) {
        audioPlayer.play(audioFile, position)
    }

    fun pauseAudio() {
        audioPlayer.pause()
    }

    fun stopAudio() {
        audioPlayer.stop()
    }

    fun resumeAudio() {
        audioPlayer.resume()
    }

    fun seekToAudio(cusorPos: Int) {
        audioPlayer.seek(cusorPos, true)
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