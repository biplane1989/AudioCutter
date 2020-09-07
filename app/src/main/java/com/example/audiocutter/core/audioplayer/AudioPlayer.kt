package com.example.audiocutter.core.audioplayer

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.audiocutter.MyApplication
import com.example.audiocutter.core.manager.AudioPlayer
import com.example.audiocutter.core.manager.PlayerInfo
import com.example.audiocutter.core.manager.PlayerState
import com.example.audiocutter.objects.AudioFile
import kotlinx.coroutines.*
import java.io.FileInputStream

object AudioPlayerImpl : AudioPlayer {
    val TAG = AudioPlayerImpl::class.java.name

    const val STATE_IDLE = 1
    const val STATE_PLAYING = 2
    const val STATE_PAUSED = 3
    var mPlayerState = PlayerState.IDLE
    private var mState: Int = STATE_IDLE
    private lateinit var appContext: Context
    private lateinit var mPlayer: MediaPlayer
    lateinit var currentAudio: AudioFile
    var currentPosition = 0

    private val mainScope = MainScope()


    private var _mPlayInfo = MutableLiveData<PlayerInfo>()

    val mPlayInfo: LiveData<PlayerInfo>
        get() = _mPlayInfo

    fun init(appContext: Context) {
        this.appContext = appContext
        mPlayer = MediaPlayer()
    }

    private fun resetState() {
        mState = STATE_IDLE
    }

    override suspend fun play(audioFile: AudioFile): Boolean {
        Log.d(TAG, "checkState: start $mState")
        try {
            withContext(Dispatchers.IO) {
                currentAudio = audioFile
                if (mPlayerState == PlayerState.IDLE) {
                    mPlayer.reset()
                    val ins = FileInputStream(audioFile.file)
                    Log.d(TAG, "play: ${audioFile.file.exists()}")
//                    Log.d(TAG, "checkPath: ${audioFile.file}")

                    mPlayer.setDataSource(ins.fd)
                    mPlayer.prepare()
                    mPlayer.start()
//                mPlayerState = PlayerState.PLAYING
                    mState = STATE_PLAYING
                }
            }
            Log.d(TAG, "checkState: $mState")
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
                    //            mPlayerState = PlayerState.PLAYING
                    mState = STATE_PLAYING
                }

            }
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    override fun pause() {
        if (mState == STATE_PLAYING) {
            mPlayer.pause()
//        mPlayerState = PlayerState.PAUSE
            mState = STATE_PAUSED
        }
    }


    override fun resume() {
        if (mState == STATE_PAUSED) {
            CoroutineScope(Dispatchers.IO).launch {
                mPlayer.start()
//        mPlayerState = PlayerState.PLAYING
                mState = STATE_PLAYING
            }

        }

    }

    override fun stop() {
        if (mState == STATE_PAUSED || mState == STATE_PLAYING) {
            mPlayer.stop()
//        mPlayerState = PlayerState.IDLE
            mState = STATE_IDLE
        }
    }

    override fun seek(position: Int) {


        mPlayer.seekTo(position)
    }


    fun getCurrentPos(): Int {
        return mPlayer.currentPosition
    }

    override fun setVolume(value: Int) {
        val audioManager = MyApplication().getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, value, 0)
    }

    override fun getPlayerInfo(): LiveData<PlayerInfo> {
        _mPlayInfo.postValue(PlayerInfo(currentAudio, currentPosition, mPlayerState))
        return mPlayInfo
    }

    fun getTotalPos(): Int {
        return mPlayer.duration
    }

    suspend fun startTimerIfReady(): Int {
        while (mainScope.isActive) {
            currentPosition++
            delay(200)
        }
        return currentPosition
    }

}