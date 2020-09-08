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
import kotlinx.coroutines.*
import java.io.FileInputStream

object AudioPlayerImpl : AudioPlayer {
    val TAG = AudioPlayerImpl::class.java.name


    var mPlayerState = PlayerState.IDLE
    private lateinit var appContext: Context
    private lateinit var mPlayer: MediaPlayer
    lateinit var currentAudio: AudioFile
    private val mainScope = MainScope()


    private var _mPlayInfo = MutableLiveData<PlayerInfo>()

    val mPlayInfo: LiveData<PlayerInfo>
        get() = _mPlayInfo

    var _mPlayState = MutableLiveData<PlayerState>()
    var _mPosition = MutableLiveData<Int>()
    var _mAudio = MutableLiveData<AudioFile>()
    var _mDuration = MutableLiveData<Int>()
    var _mVolume = MutableLiveData<Int>()
    lateinit var audioManager: AudioManager

    fun init(appContext: Context) {
        this.appContext = appContext
        mPlayer = MediaPlayer()
        audioManager = this.appContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
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
                    startTimerIfReady()
                    mPlayerState = PlayerState.PLAYING
                    Log.d(TAG, "PlayToPosition: $mPlayerState")
                }
            }
            _mAudio.postValue(currentAudio)
            _mPlayState.postValue(mPlayerState)
            _mDuration.postValue(mPlayer.duration)
            if (mPlayer.currentPosition > mPlayer.duration) {
                mPlayer.stop()
                mPlayerState = PlayerState.IDLE
                Log.d(TAG, "stopped in fun play: $mPlayerState")
                _mPlayState.postValue(mPlayerState)
            }
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d(TAG, "PlayToPosition: ${e.printStackTrace()}")
            mPlayer.stop()
            mPlayerState = PlayerState.IDLE
            _mPlayState.postValue(mPlayerState)
            return false
        }

    }

    override suspend fun play(audioFile: AudioFile, currentPosition: Int) {
        try {
            Log.d(TAG, "PlayToPosition suspend : in function")
            play(audioFile)
            mPlayer.seekTo(currentPosition)
            _mPosition.postValue(currentPosition)
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d(TAG, "PlayToPosition exception:${e.printStackTrace()}")
            mPlayer.stop()
            mPlayerState = PlayerState.IDLE
            _mPlayState.postValue(mPlayerState)
        }

    }

    override suspend fun play(audioFile: AudioFile, startPosition: Int, endPosition: Int): Boolean {
//        try {
//            withContext(Dispatchers.IO) {
//                resetState()
//                if (mPlayerState == PlayerState.IDLE) {
//                    currentAudio = audioFile
//                    mPlayer.reset()
//                    val ins = FileInputStream(audioFile.file)
//                    mPlayer.setDataSource(ins.fd)
//                    mPlayer.prepare()
//                    mPlayer.start()
//                    mPlayerState = PlayerState.PLAYING
//                }
//            }
//            _mAudio.postValue(currentAudio)
//            _mPlayState.postValue(mPlayerState)
//            return true
//        } catch (e: Exception) {
//            e.printStackTrace()
//            mPlayer.stop()
//            mPlayerState = PlayerState.IDLE
//            _mPlayState.postValue(mPlayerState)
        return false
//        }
    }

    override fun pause() {
        Log.d("taih", "pause")
        if (mPlayerState == PlayerState.PLAYING) {
            mPlayer.pause()
            mPlayerState = PlayerState.PAUSE
        }
        _mPlayState.postValue(mPlayerState)

    }


    override fun resume() {
        Log.d("taih", "resume")
        if (mPlayerState == PlayerState.PAUSE) {
            CoroutineScope(Dispatchers.IO).launch {
                mPlayer.start()
                mPlayerState = PlayerState.PLAYING
            }
        }
        _mPlayState.postValue(mPlayerState)
    }

    override fun stop() {
        Log.d("taih", "stop")
        if (mPlayerState == PlayerState.PAUSE || mPlayerState == PlayerState.PLAYING) {
            mPlayer.stop()
            mPlayerState = PlayerState.IDLE
        }
        _mPlayState.postValue(mPlayerState)
    }

    override fun seek(position: Int) {
        try {

            Log.d(TAG, "PlayToPosition seekto: ${position} duration " + getTotalPos())
            mPlayer.seekTo(position)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        _mPosition.postValue(position)
    }

    fun getMaxVolume(): Int {
        return audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
    }


    override fun setVolume(value: Int) {
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, value, 0)
        _mVolume.postValue(value)
    }


    fun getTotalPos(): Int {
        return mPlayer.duration
    }

    private suspend fun startTimerIfReady() {
        mainScope.launch {
            while (mainScope.isActive) {
                var currentPosition = mPlayer.currentPosition
                delay(500)
                if (currentPosition >= mPlayer.duration) {
                    currentPosition = 0
                    mPlayer.stop()
                    _mPlayState.postValue(PlayerState.IDLE)
                }
                _mPosition.postValue(currentPosition)
            }
        }
    }


    override fun getPlayerInfo(): LiveData<PlayerInfo> {
        _mPlayInfo.postValue(
            PlayerInfo(
                _mAudio.value!!,
                _mPosition.value!!,
                _mPlayState.value!!,
                _mDuration.value!!,
                _mVolume.value!!
            )
        )
        return mPlayInfo
    }

}