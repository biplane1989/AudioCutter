package com.example.audiocutter.functions.editor.screen

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.audiocutter.base.BaseViewModel
import com.example.audiocutter.core.audiomanager.Folder
import com.example.audiocutter.core.manager.ManagerFactory
import com.example.audiocutter.core.manager.PlayerInfo
import com.example.audiocutter.core.manager.PlayerState
import com.example.audiocutter.objects.AudioFile

class CuttingViewModel : BaseViewModel() {
    private var audioFile: AudioFile? = null
    private val audioPlayer = ManagerFactory.newAudioPlayer()
    private var cuttingCurrPos = 0
    private var cuttingStartPos = 0
    private var cuttingEndPos = 0
    private var ldAudioFile = MutableLiveData<AudioFile?>()

    fun loading(context: Context, pathAudio: String): LiveData<AudioFile?> {
        audioPlayer.init(context)
        ManagerFactory.getAudioFileManager().findAudioFile(pathAudio)?.let {
            audioFile = it
        }
        ldAudioFile.postValue(audioFile)
        return ldAudioFile
    }
    fun currPosReachToEnd(){
        cuttingCurrPos = cuttingStartPos
        pauseAudio()
    }
    fun currPosReachToStart(){
        cuttingCurrPos = cuttingEndPos
        pauseAudio()
    }
    suspend fun clickedPlayButton() {
        audioFile?.let {
            val playerInfo = audioPlayer.getPlayerInfoData()
            if (playerInfo.playerState == PlayerState.PLAYING) {
                audioPlayer.pause()
            } else {
                if (playerInfo.playerState == PlayerState.IDLE) {
                    audioPlayer.play(it, cuttingCurrPos)
                } else {

                    if(audioPlayer.getPlayerInfoData().posision != cuttingCurrPos){
                        if(cuttingCurrPos == cuttingEndPos){
                            cuttingCurrPos = cuttingStartPos;
                        }
                        audioPlayer.seek(cuttingCurrPos)
                    }
                    audioPlayer.resume()
                }
            }
        }

    }

    fun getAudioPlayerInfo(): LiveData<PlayerInfo> {
        return audioPlayer.getPlayerInfo()
    }

    fun changeStartPos(pos: Int) {
        cuttingStartPos = pos
        if(cuttingStartPos >= cuttingCurrPos){
            changeCurrPos(cuttingStartPos)
        }
    }

    fun changeEndPos(pos: Int) {
        cuttingEndPos = pos
        if(cuttingEndPos <= cuttingCurrPos){
            changeCurrPos(cuttingEndPos)
        }
    }

    fun changeCurrPos(newPos: Int, allowSeekingAudio: Boolean = true) {
        if (newPos == cuttingCurrPos) {
            return
        }
        if (newPos >= cuttingEndPos) {
            audioPlayer.stop()
            cuttingCurrPos = cuttingStartPos
        } else {
            if (newPos in cuttingStartPos..cuttingEndPos) {
                if (audioPlayer.getPlayerInfoData().playerState != PlayerState.IDLE) {
                    if (allowSeekingAudio) {
                        audioPlayer.seek(newPos)
                    }
                }
            }
            cuttingCurrPos = newPos
        }


    }


    fun getStorageFolder(): String {
        return ManagerFactory.getAudioFileManager().getFolderPath(Folder.TYPE_CUTTER)
    }

    fun resumeAudio() {
        audioPlayer.resume()
    }

    fun getAudioFile(): AudioFile? {
        return audioFile
    }

    fun pauseAudio() {
        if (audioPlayer.getPlayerInfoData().playerState == PlayerState.PLAYING) {
            audioPlayer.pause()
        }
    }

    fun seekAudio(pos: Int) {
        audioPlayer.seek(pos)
    }

    fun setVolume(value: Float) {
        audioPlayer.setVolume(value)
    }

    fun getCuttingCurrPos(): Int {
        return cuttingCurrPos
    }

    fun getCuttingStartPos(): Int {
        return cuttingStartPos
    }

    fun getCuttingEndPos(): Int {
        return cuttingEndPos
    }


}