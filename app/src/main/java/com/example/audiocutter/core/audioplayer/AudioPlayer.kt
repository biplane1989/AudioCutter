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

internal object AudioPlayerImpl : AudioPlayer {
    val TAG = AudioPlayerImpl::class.java.name


    private var mPlayerState = PlayerState.IDLE
    private lateinit var appContext: Context
    private lateinit var mPlayer: MediaPlayer
    lateinit var currentAudio: AudioFile
    private val mainScope = MainScope()


    private var _mPlayInfo = MutableLiveData<PlayerInfo>()
    val playInfoData = PlayerInfo(null, 0, PlayerState.IDLE, 0, 0)
    val mPlayInfo: LiveData<PlayerInfo>
        get() = _mPlayInfo

    lateinit var audioManager: AudioManager

    init {
        _mPlayInfo.postValue(playInfoData)
    }

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
            playInfoData.currentAudio = currentAudio
            playInfoData.playerState = mPlayerState
            playInfoData.duration = mPlayer.duration
            playInfoData.position = 0
            if (mPlayer.currentPosition > mPlayer.duration) {
                mPlayer.stop()
                mPlayerState = PlayerState.IDLE
                Log.d(TAG, "stopped in fun play: $mPlayerState")
                playInfoData.playerState = mPlayerState
            }
            Log.d("001", "play")
            _mPlayInfo.postValue(playInfoData)
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d(TAG, "PlayToPosition: ${e.printStackTrace()}")
            mPlayer.stop()
            mPlayerState = PlayerState.IDLE
            playInfoData.playerState = mPlayerState
            _mPlayInfo.postValue(playInfoData)
            return false
        }


    }

    override suspend fun play(audioFile: AudioFile, currentPosition: Int) {
        try {
            Log.d(TAG, "PlayToPosition suspend : in function")
            play(audioFile)
            mPlayer.seekTo(currentPosition)
            playInfoData.position = currentPosition
            _mPlayInfo.postValue(playInfoData)
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d(TAG, "PlayToPosition exception:${e.printStackTrace()}")
            mPlayer.stop()
            mPlayerState = PlayerState.IDLE
            playInfoData.playerState = mPlayerState
            _mPlayInfo.postValue(playInfoData)

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
        playInfoData.playerState = mPlayerState
        _mPlayInfo.postValue(playInfoData)

    }


    override fun resume() {
        Log.d("taih", "resume")
        if (mPlayerState == PlayerState.PAUSE) {
            CoroutineScope(Dispatchers.IO).launch {
                mPlayer.start()
                mPlayerState = PlayerState.PLAYING
            }
        }
        playInfoData.playerState = mPlayerState
        _mPlayInfo.postValue(playInfoData)
    }

    override fun stop() {
        if (mPlayerState == PlayerState.PAUSE || mPlayerState == PlayerState.PLAYING) {
            mPlayer.stop()
            mPlayerState = PlayerState.IDLE

        }
        Log.d("001", "stop")
        playInfoData.playerState = mPlayerState
        playInfoData.position = 0
        _mPlayInfo.postValue(playInfoData)
    }

    override fun seek(position: Int) {
        try {

            Log.d(TAG, "PlayToPosition seekto: ${position} duration " + getTotalPos())
            mPlayer.seekTo(position)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        playInfoData.position = position
        _mPlayInfo.postValue(playInfoData)
    }

    fun getMaxVolume(): Int {
        return audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
    }


    override fun setVolume(value: Int) {
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, value, 0)
        playInfoData.volume = value
        _mPlayInfo.postValue(playInfoData)
    }


    fun getTotalPos(): Int {
        return mPlayer.duration
    }

    private suspend fun startTimerIfReady() {
        mainScope.launch {
            while (mainScope.isActive) {
                var changed = false

                var currentPosition = mPlayer.currentPosition
                delay(500)
                if (currentPosition >= mPlayer.duration) {
                    currentPosition = 0
                    mPlayer.stop()
                    if (playInfoData.playerState != PlayerState.IDLE) {
                        changed = true
                        playInfoData.playerState = PlayerState.IDLE
                    }

                }
                if (playInfoData.position != currentPosition) {
                    changed = true
                    playInfoData.position = currentPosition
                }
                if (mPlayer.isPlaying && playInfoData.playerState != PlayerState.PLAYING) {
                    changed = true
                    playInfoData.playerState = PlayerState.PLAYING
                }
                if (changed) {
                    _mPlayInfo.postValue(playInfoData)
                }

            }
        }
    }


    override fun getPlayerInfo(): LiveData<PlayerInfo> {
        return mPlayInfo
    }

}