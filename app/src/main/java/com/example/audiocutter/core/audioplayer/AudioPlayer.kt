package com.example.audiocutter.core.audioplayer

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Looper
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.audiocutter.core.manager.AudioPlayer
import com.example.audiocutter.core.manager.PlayerInfo
import com.example.audiocutter.core.manager.PlayerState
import com.example.audiocutter.objects.AudioFile
import kotlinx.coroutines.*

object AudioPlayerImpl : AudioPlayer {
    val TAG = AudioPlayerImpl::class.java.name


    private lateinit var appContext: Context
    private lateinit var mPlayer: MediaPlayer
    private val mainScope = MainScope()
    private var isStopped = false


    private var _mPlayInfo = MutableLiveData<PlayerInfo>()
    val playInfoData = PlayerInfo(null, 0, PlayerState.IDLE, 0, 0)
    val mPlayInfo: LiveData<PlayerInfo>
        get() = _mPlayInfo


    lateinit var audioManager: AudioManager


    fun init(appContext: Context) {
        this.appContext = appContext
        mPlayer = MediaPlayer()
        audioManager = this.appContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        mPlayer.setOnCompletionListener {
            Log.d("sesm", "setOnCompletionListener")

            if (playInfoData.playerState != PlayerState.IDLE) {
                playInfoData.playerState = PlayerState.IDLE
                notifyPlayerDataChanged()
            }
        }
    }

    private fun notifyPlayerDataChanged() {

        Log.d(
            "1111",
            "startTimerIfReady: path${playInfoData.currentAudio!!.fileName}   state ${playInfoData.playerState}  duration ${playInfoData.duration}   position ${playInfoData.position}"
        )

        val copy = PlayerInfo(
            playInfoData.currentAudio,
            playInfoData.position,
            playInfoData.playerState,
            playInfoData.duration,
            playInfoData.volume
        )
        if (Looper.myLooper() == Looper.getMainLooper()) {
            _mPlayInfo.value = copy
        } else {
            mainScope.launch { _mPlayInfo.value = copy }
        }
    }

    override suspend fun play(audioFile: AudioFile): Boolean {
        try {
            withContext(Dispatchers.IO) {
                synchronized(mPlayer) {
                    stop()
                    if (playInfoData.playerState != PlayerState.IDLE) {
                        playInfoData.playerState = PlayerState.IDLE
                        notifyPlayerDataChanged()
                    } else if (playInfoData.currentAudio != null && playInfoData.currentAudio != audioFile) {

                        playInfoData.playerState = PlayerState.IDLE
                        notifyPlayerDataChanged()
                    }
                    playInfoData.currentAudio = audioFile
                    mPlayer.reset()
//                    val ins = FileInputStream(audioFile.file)
//                    mPlayer.setDataSource(ins.fd)
                    mPlayer.setDataSource(appContext, audioFile.uri!!)
                    mPlayer.prepare()
                    mPlayer.start()
                    isStopped = false;

                }
                startTimerIfReady()

            }
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d(TAG, "exception: ${e.printStackTrace()}")
            mPlayer.stop()
            return false
        }

    }

    override suspend fun play(audioFile: AudioFile, currentPosition: Int) {
        /* try {
             Log.d(TAG, "PlayToPosition suspend : in function")
             play(audioFile)
             mPlayer.seekTo(currentPosition)
             playInfoData.position = currentPosition
             _mPlayInfo.postValue(playInfoData)
         } catch (e: Exception) {
             e.printStackTrace()
             Log.d(TAG, "PlayToPosition exception:${e.printStackTrace()}")
             mPlayer.stop()
             playInfoData.playerState = PlayerState.IDLE
             _mPlayInfo.postValue(playInfoData)
         }*/
        TODO()

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
        //return false
//        }
        TODO()
    }

    override fun pause() {
        if ( playInfoData.playerState == PlayerState.PLAYING) {
            mPlayer.pause()
        }

    }


    override fun resume() {
        if ( playInfoData.playerState == PlayerState.PAUSE) {
            mPlayer.start()
        }
    }


    override fun stop() {
        Log.d("check", "stop")
        if (playInfoData.playerState == PlayerState.PAUSE || playInfoData.playerState == PlayerState.PLAYING) {
            mPlayer.stop()
            isStopped = true;
        }

    }

    override fun seek(position: Int) {
        try {
            Log.d(TAG, "PlayToPosition seekto: ${position} duration " + getTotalPos())
            mPlayer.seekTo(position)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    fun getMaxVolume(): Int {
        return audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
    }


    override fun setVolume(value: Int) {
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, value, 0)
        playInfoData.volume = value
        notifyPlayerDataChanged()
    }


    fun getTotalPos(): Int {
        return mPlayer.duration
    }

    private suspend fun startTimerIfReady() {
        mainScope.launch {
            while (mainScope.isActive) {
                var changed = false

                delay(500)
                synchronized(mPlayer) {
                    playInfoData.duration = mPlayer.duration
                    var currentPosition = mPlayer.currentPosition
                    if (currentPosition >= mPlayer.duration) {
                        currentPosition = 0
                        playInfoData.position = currentPosition
                        mPlayer.stop()

                        if (playInfoData.playerState != PlayerState.IDLE) {
                            playInfoData.playerState = PlayerState.IDLE
                            changed = false
                        }
                    }

                    if (mPlayer.isPlaying) {
                        if (playInfoData.playerState != PlayerState.PLAYING) {
                            playInfoData.playerState = PlayerState.PLAYING
                            changed = true
                        }
                    } else {
                        if (playInfoData.playerState == PlayerState.PLAYING) {
                            if (isStopped) {
                                playInfoData.playerState = PlayerState.IDLE
                                currentPosition = 0
                                playInfoData.position = currentPosition
                            } else {
                                playInfoData.playerState = PlayerState.PAUSE
                            }
                            changed = true
                        }
                    }

                    if (playInfoData.position != currentPosition && playInfoData.playerState == PlayerState.PLAYING) {
                        changed = true
                        playInfoData.position = currentPosition
                    }
                }
                if (changed) {
                    notifyPlayerDataChanged()
                }
            }
        }
    }


    override fun getPlayerInfo(): LiveData<PlayerInfo> {
        return mPlayInfo
    }

}