package com.example.audiocutter.activities

import android.os.Bundle
import android.os.Environment
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.audiocutter.R
import com.example.audiocutter.util.Utils
import com.example.waveform.views.WaveformView
import kotlinx.coroutines.launch
import java.io.File

class TestWaveformActivity : AppCompatActivity() {
    private lateinit var waveformView: WaveformView
    private lateinit var zoomInButton: Button
    private lateinit var zoomOutButton: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_waveform)
        waveformView = findViewById(R.id.waveform_view)
        val audioFilePath =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).absolutePath + File.separator + "test.m4a"
        lifecycleScope.launch {
            waveformView.setDataSource(
                audioFilePath,
                Utils.getTimeAudio(File(audioFilePath), this@TestWaveformActivity)
            )
        }
        zoomInButton = findViewById(R.id.zoom_in)
        zoomOutButton = findViewById(R.id.zoom_out)

        zoomInButton.setOnClickListener {
            waveformView.zoomIn()
        }
        zoomOutButton.setOnClickListener {
            waveformView.zoomOut()
        }

    }
}