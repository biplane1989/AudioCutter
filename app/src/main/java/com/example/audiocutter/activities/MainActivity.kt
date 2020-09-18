package com.example.audiocutter.activities

import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseActivity
import com.example.audiocutter.functions.audiocutterscreen.view.screen.AudioCutterScreen

class MainActivity : BaseActivity() {
    private val CODE_WRITE_SETTINGS_PERMISSION: Int = 1000
    lateinit var audioCutterFrg: AudioCutterScreen

    @RequiresApi(Build.VERSION_CODES.M)
    override fun createView(savedInstanceState: Bundle?) {
        setContentView(R.layout.main_screen)

        audioCutterFrg = AudioCutterScreen()
        supportFragmentManager.beginTransaction().add(R.id.ln_main, audioCutterFrg).commit()
        supportFragmentManager.beginTransaction().show(audioCutterFrg).commit()
    }





}