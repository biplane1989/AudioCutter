package com.example.audiocutter.core.audioplayer

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Looper
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.audiocutter.core.manager.*
import com.example.audiocutter.objects.AudioFile
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.*

class AudioPlayerImpl : AudioPlayer, MediaPlayer.OnPreparedListener {
    //    val TAG = AudioPlayerImpl::class.java.name
    val TAG = "5560"


    private lateinit var appContext: Context
    private lateinit var mPlayer: MediaPlayer
    private val mainScope = MainScope()
    private var isStopped = false
    private var isSeekTo = 0
    private lateinit var mAudioFile: AudioFile


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
        mPlayer.setOnErrorListener(listenerError)
    }

    private val listener = MediaPlayer.OnCompletionListener {
        if (playInfoData.playerState != PlayerState.IDLE) {
            playInfoData.playerState = PlayerState.IDLE
            notifyPlayerDataChanged()
        }
    }

    private val listenerError = MediaPlayer.OnErrorListener { mp, what, extra ->
        Log.d(TAG, "OnErrorListener: what $what isplaying ${mp.isPlaying} MediaPlayer.MEDIA_ERROR_IO ${MediaPlayer.MEDIA_ERROR_IO}")
        stop()
        true
    }


    private fun notifyPlayerDataChanged() {

        Log.d("1111", "startTimerIfReady: path${playInfoData.currentAudio!!.fileName}   state ${playInfoData.playerState}  duration ${playInfoData.duration}   position ${playInfoData.posision}")
        val playInfoCopy = PlayerInfo(playInfoData.currentAudio, playInfoData.posision, playInfoData.playerState, playInfoData.duration, playInfoData.volume)
        if (Looper.myLooper() == Looper.getMainLooper()) {
            _mPlayInfo.value = playInfoCopy
        } else {
            mainScope.launch { _mPlayInfo.value = playInfoCopy }
        }
    }

    override  fun play(audioFile: AudioFile): Boolean {
        mAudioFile = audioFile
        try {
            mainScope.launch {
                mPlayer.stop()
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
                /*playInfoData.duration = audioFile.duration.toInt()*/
                isStopped = false
                startTimerIfReady()
            }
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d(TAG, "exception: ${e.printStackTrace()}")
            return false
        }

    }

    override  fun play(audioFile: AudioFile, currentPosition: Int): Boolean {
        Log.d(TAG, "play: currentPosition : ${currentPosition}")
        mAudioFile = audioFile
        try {
            isSeekTo = currentPosition
            mainScope.launch {
                mPlayer.stop()
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
                /* playInfoData.duration = audioFile.duration.toInt()*/
                isStopped = false
                startTimerIfReady()
            }

            return true
        } catch (e: Exception) {
            e.printStackTrace()
            mPlayer.stop()
            return false
        }
    }

    private fun prepare(audioFile: AudioFile) {
        try {
            playInfoData.playerState = PlayerState.PREPARING
            mPlayer.apply {
                setOnPreparedListener(this@AudioPlayerImpl)
                setDataSource(appContext, audioFile.uri!!)
                prepareAsync()
            }
        } catch (e: Exception) {
            Log.d(TAG, "prepare: ${e.printStackTrace()}")
            e.printStackTrace()
        }

    }


    override fun onPrepared(player: MediaPlayer?) {
        if (isSeekTo == 0) {
            player?.let {
                playInfoData.posision = 0
                player.start()
                if (player.isPlaying) {
                    playInfoData.duration = player.duration
                }
                playInfoData.playerState = PlayerState.PLAYING
                notifyPlayerDataChanged()

            }
        } else {
            playInfoData.posision = isSeekTo
            player?.let {
                player.start()
                if (player.isPlaying) {
                    playInfoData.duration = player.duration
                }
                player.seekTo(isSeekTo)
                playInfoData.playerState = PlayerState.PLAYING
                notifyPlayerDataChanged()
            }
        }


    }

    private fun isReady(): Boolean {
        return playInfoData.playerState != PlayerState.IDLE && playInfoData.playerState != PlayerState.PREPARING && !isStopped
    }

    override fun pause() {
        if (isReady()) {
            mPlayer.pause()
        }
    }


    override fun resume() {
        if (isReady()) {
            mPlayer.start()
        }
    }


    override fun stop() {
        if (playInfoData.playerState == PlayerState.PAUSE || playInfoData.playerState == PlayerState.PLAYING) {
            mPlayer.stop()
            isStopped = true
        }
    }

    override fun seek(position: Int) {
        try {
            if (position >= mPlayer.duration) {
                mPlayer.stop()
            }
            mPlayer.seekTo(position)
            playInfoData.posision = position
            Log.d(TAG, "PlayToPosition seekto: $position duration " + getTotalPos())
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
                delay(500)

                if (playInfoData.playerState != PlayerState.PREPARING && playInfoData.playerState != PlayerState.IDLE) {
                    playInfoData.duration = mPlayer.duration
                    var currentPosition = mPlayer.currentPosition
                    if (currentPosition >= mPlayer.duration) {
                        currentPosition = 0
                        playInfoData.posision = currentPosition
                        mPlayer.stop()

                        if (playInfoData.playerState != PlayerState.IDLE) {
                            playInfoData.playerState = PlayerState.IDLE
                            changed = true
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
                                playInfoData.posision = currentPosition
                            } else {
                                playInfoData.playerState = PlayerState.PAUSE
                            }
                            changed = true
                        } else {
                           /* if (isStopped &&  playInfoData.playerState != PlayerState.IDLE) {
                                playInfoData.playerState = PlayerState.IDLE
                                currentPosition = 0
                                playInfoData.posision = currentPosition
                                changed = true
                            }*/
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
    }


    override fun getPlayerInfoData(): PlayerInfo {
        return playInfoData
    }

    override fun getPlayerInfo(): LiveData<PlayerInfo> {
        return mPlayInfo
    }

}


