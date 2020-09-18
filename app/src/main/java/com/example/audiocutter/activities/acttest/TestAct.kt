package com.example.audiocutter.activities.acttest

import android.media.MediaPlayer
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.audiocutter.R

class TestAct : AppCompatActivity() {
    lateinit var mediaPlayer: MediaPlayer
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_test)
        mediaPlayer = MediaPlayer()
        mediaPlayer.setDataSource("/storage/emulated/0/Download/Ed Sheeran - Shape Of You [Official].mp3")
        initViews()
    }

    private fun initViews() {

    }

    override fun onStart() {
        super.onStart()
        startPlayingAudio()

    }

    private fun startPlayingAudio() {
        mediaPlayer.start()

    }

}