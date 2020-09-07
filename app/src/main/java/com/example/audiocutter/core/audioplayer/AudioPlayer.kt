package com.example.audiocutter.core.audioplayer

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.audiocutter.core.manager.AudioPlayer
import com.example.audiocutter.core.manager.PlayerInfo
import com.example.audiocutter.core.manager.PlayerState
import com.example.audiocutter.objects.AudioFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.FileInputStream

object AudioPlayerImpl : AudioPlayer {
    val TAG = AudioPlayerImpl::class.java.name


    var mPlayerState = PlayerState.IDLE
    private lateinit var appContext: Context
    private lateinit var mPlayer: MediaPlayer
    lateinit var currentAudio: AudioFile
    var currentPosition = 0



    private var _mPlayInfo = MutableLiveData<PlayerInfo>()

    val mPlayInfo: LiveData<PlayerInfo>
        get() = _mPlayInfo

    fun init(appContext: Context) {
        this.appContext = appContext
        mPlayer = MediaPlayer()
    }

    private fun resetState() {
        mPlayerState = PlayerState.IDLE
    }

    override suspend fun play(audioFile: AudioFile): Boolean {
        try {
            withContext(Dispatchers.IO) {
                currentAudio = audioFile
                if (mPlayerState == PlayerState.IDLE) {
                    mPlayer.reset()
                    val ins = FileInputStream(audioFile.file)
                    Log.d(TAG, "play: ${audioFile.file.exists()}")

                    mPlayer.setDataSource(ins.fd)
                    mPlayer.prepare()
                    mPlayer.start()
                    mPlayerState = PlayerState.PLAYING
                }
            }
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d(TAG, "bugofFile: ${e.printStackTrace()}")
            return false
        }

    }

    override suspend fun play(audioFile: AudioFile, currentPosition: Int) {
        play(audioFile)
        mPlayer.seekTo(currentPosition)
    }

    override suspend fun play(audioFile: AudioFile, startPosition: Int, endPosition: Int): Boolean {
        try {
            withContext(Dispatchers.IO) {
                resetState()
                if (mPlayerState == PlayerState.IDLE) {
                    currentAudio = audioFile
                    mPlayer.reset()
                    val ins = FileInputStream(audioFile.file)
                    mPlayer.setDataSource(ins.fd)
                    mPlayer.prepare()
                    mPlayer.start()
                    mPlayerState = PlayerState.PLAYING
                }

            }
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    override fun pause() {
        if (mPlayerState == PlayerState.PLAYING) {
            mPlayer.pause()
            mPlayerState = PlayerState.PAUSE
        }
    }


    override fun resume() {
        if (mPlayerState == PlayerState.PAUSE) {
            CoroutineScope(Dispatchers.IO).launch {
                mPlayer.start()
                mPlayerState = PlayerState.PLAYING
            }

        }

    }

    override fun stop() {
        if (mPlayerState == PlayerState.PAUSE || mPlayerState == PlayerState.PLAYING) {
            mPlayer.stop()
            mPlayerState = PlayerState.IDLE
        }
    }

    override fun seek(position: Int) {
        mPlayer.seekTo(position)
    }

    fun getMaxVolume(): Int {
        val audioManager = appContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        return audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
    }


    fun getCurrentPos(): Int {
        return mPlayer.currentPosition
    }

    override fun setVolume(value: Int) {
        val audioManager = appContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, value, 0)
    }

    override fun getPlayerInfo(): LiveData<PlayerInfo> {
        _mPlayInfo.postValue(PlayerInfo(currentAudio, currentPosition, mPlayerState))
        return mPlayInfo
    }

    fun getTotalPos(): Int {
        return mPlayer.duration
    }

}