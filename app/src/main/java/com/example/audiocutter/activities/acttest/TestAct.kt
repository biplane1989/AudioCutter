package com.example.audiocutter.activities.acttest

import android.media.MediaPlayer
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.audiocutter.R
import com.example.audiocutter.functions.audiocutterscreen.view.animation.BarVisualizer

class TestAct : AppCompatActivity() {
    private var mVisualizer: BarVisualizer? = null
    lateinit var mediaPlayer: MediaPlayer
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_test)
        mediaPlayer = MediaPlayer()
        mediaPlayer.setDataSource("/storage/emulated/0/Download/Ed Sheeran - Shape Of You [Official].mp3")
        initViews()
    }

    private fun initViews() {

        mVisualizer = findViewById(R.id.bar)
    }

    override fun onStart() {
        super.onStart()
        startPlayingAudio()

    }

    private fun startPlayingAudio() {
        mediaPlayer.start()

        mVisualizer!!.setAudioSessionId(mediaPlayer.audioSessionId)
    }

}