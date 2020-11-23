package com.example.audiocutter.functions.editor.screen

import androidx.lifecycle.LiveData
import com.example.audiocutter.base.BaseViewModel
import com.example.audiocutter.core.audiomanager.Folder
import com.example.audiocutter.core.manager.ManagerFactory
import com.example.audiocutter.core.manager.PlayerInfo
import com.example.audiocutter.core.manager.PlayerState
import com.example.audiocutter.objects.AudioFile

class CuttingViewModel : BaseViewModel() {
    private var audioFile: AudioFile?=null
    private val audioPlayer = ManagerFactory.getDefaultAudioPlayer()
    private var cuttingCurrPos = 0
    private var cuttingStartPos = 0
    private var cuttingEndPos = 0
    fun restore(pathAudio: String): Boolean {
        ManagerFactory.getAudioFileManager().findAudioFile(pathAudio)?.let {
            audioFile = it
            return true
        }
        return false
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
    }

    fun changeEndPos(pos: Int) {
        cuttingEndPos = pos
    }

    fun changeCurrPos(newPos: Int, allowSeekingAudio: Boolean = true) {
        if (newPos >= cuttingEndPos) {
            audioPlayer.stop()
            cuttingCurrPos = cuttingStartPos
        } else {
            if (newPos in cuttingStartPos..cuttingEndPos) {
                if (audioPlayer.getPlayerInfoData().playerState == PlayerState.PLAYING) {
                    if (allowSeekingAudio) {
                        audioPlayer.seek(newPos)
                    }
                } else {
                    audioPlayer.seek(newPos)
                }


            }
            cuttingCurrPos = newPos
        }


    }

    fun getNameSuggestion(): String {
        return ""
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