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

class AudioPlayerImpl : AudioPlayer, MediaPlayer.OnPreparedListener {
    val TAG = AudioPlayerImpl::class.java.name


    private lateinit var appContext: Context
    private lateinit var mPlayer: MediaPlayer
    private val mainScope = MainScope()
    private var isStopped = false
    private var isSeekTo = 0

    private var _mPlayInfo = MutableLiveData<PlayerInfo>()
    private val playInfoData = PlayerInfo(null, 0, PlayerState.IDLE, 0, 0f)
    private val mPlayInfo: LiveData<PlayerInfo>
        get() = _mPlayInfo


    lateinit var audioManager: AudioManager


    override fun init(appContext: Context) {
        this.appContext = appContext
        mPlayer = MediaPlayer()
        audioManager = this.appContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        mPlayer.setOnCompletionListener(listener)
    }

    private val listener = MediaPlayer.OnCompletionListener {
        if (playInfoData.playerState != PlayerState.IDLE) {
            Log.d("1111", "setOnCompletionListener  ${playInfoData.playerState}")
            playInfoData.playerState = PlayerState.IDLE
            notifyPlayerDataChanged()
        }
    }


    private fun notifyPlayerDataChanged() {

        Log.d(
            "1111",
            "startTimerIfReady: path${playInfoData.currentAudio!!.fileName}   state ${playInfoData.playerState}  duration ${playInfoData.duration}   position ${playInfoData.posision}"
        )
        val playInfoCopy = PlayerInfo(
            playInfoData.currentAudio,
            playInfoData.posision,
            playInfoData.playerState,
            playInfoData.duration,
            playInfoData.volume
        )
        if (Looper.myLooper() == Looper.getMainLooper()) {
            _mPlayInfo.value = playInfoCopy
        } else {
            mainScope.launch { _mPlayInfo.value = playInfoCopy }
        }
    }

    override suspend fun play(audioFile: AudioFile): Boolean {
        try {
            Log.d("4444", "playNormal:")
            withContext(Dispatchers.IO) {
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
                isSeekTo = 0
                prepare(audioFile)
                isStopped = false;
            }
            startTimerIfReady()
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d(TAG, "exception: ${e.printStackTrace()}")
//            mPlayer1.stop()
            return false
        }

    }

    override suspend fun play(audioFile: AudioFile, currentPosition: Int): Boolean {
        try {
            isSeekTo = currentPosition
            Log.d("4444", "playNormal:")
            withContext(Dispatchers.IO) {
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
                prepare(audioFile)
                isStopped = false;
            }
            startTimerIfReady()
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d(TAG, "exception: ${e.printStackTrace()}")
            mPlayer.stop()
            return false
        }
    }




    private fun prepare(audioFile: AudioFile) {
        playInfoData.playerState = PlayerState.PREPARING
        mPlayer.apply {
            setOnPreparedListener(this@AudioPlayerImpl)
            setDataSource(appContext, audioFile.uri!!)
            prepareAsync()
        }
    }


    override fun onPrepared(player: MediaPlayer?) {
        if (isSeekTo == 0) {
            playInfoData.posision = 0
            playInfoData.duration = player?.duration!!
            player.start()
            playInfoData.playerState = PlayerState.PLAYING
            notifyPlayerDataChanged()
        } else {
            playInfoData.posision = isSeekTo
            playInfoData.duration = player?.duration!!
            player.start()
            player.seekTo(isSeekTo)
            playInfoData.playerState = PlayerState.PLAYING
            notifyPlayerDataChanged()
        }
    }


    override fun pause() {
        if (playInfoData.playerState == PlayerState.PLAYING) {
            mPlayer.pause()
        }
    }


    override fun resume() {
        if (playInfoData.playerState == PlayerState.PAUSE) {
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
            if (position >= mPlayer.duration) {
                mPlayer.stop()
            }
            mPlayer.seekTo(position)
            Log.d(TAG, "PlayToPosition seekto: ${position} duration " + getTotalPos())
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    override fun getMaxVolume(): Int {
        return audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
    }

    override fun setVolume(volume: Float) {
        mPlayer.setVolume(volume, volume)
        playInfoData.volume = volume

    }

    fun getTotalPos(): Int {
        return mPlayer.duration
    }

    override fun getAudioIsPlaying(): Boolean {
        return mPlayer.isPlaying
    }


    private suspend fun startTimerIfReady() {
        mainScope.launch {
            while (mainScope.isActive) {
                var changed = false
                delay(200)
                playInfoData.duration = mPlayer.duration
                var currentPosition = mPlayer.currentPosition
                if (currentPosition >= mPlayer.duration) {
                    currentPosition = 0
                    playInfoData.posision = currentPosition
                    mPlayer.stop()

                    if (playInfoData.playerState != PlayerState.IDLE) {
                        playInfoData.playerState = PlayerState.IDLE
                        Log.d("nmcode", "startTimerIfReady11: ${playInfoData.playerState}")
                        changed = true
                    }
                }

                if (playInfoData.playerState == PlayerState.PREPARING) {
                    changed = false
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
                            playInfoData.posision = currentPosition
                        } else {
                            playInfoData.playerState = PlayerState.PAUSE
                        }
                        changed = true
                    }
                }

                if (playInfoData.posision != currentPosition && playInfoData.playerState == PlayerState.PLAYING) {
                    changed = true
                    playInfoData.posision = currentPosition
                }
                if (changed) {
                    notifyPlayerDataChanged()
                }
            }
        }
    }

    override fun getPlayerInfoData(): PlayerInfo {
        return playInfoData
    }

    override fun getPlayerInfo(): LiveData<PlayerInfo> {
        return mPlayInfo
    }

}


