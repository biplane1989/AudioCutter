package com.example.audiocutter.activities

import android.os.Bundle
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseActivity
import com.example.audiocutter.functions.audiocutterscreen.AudioCutterScreen

class MainActivity : BaseActivity() {
    lateinit var audioCutterFrg: AudioCutterScreen
    override fun createView(savedInstanceState: Bundle?) {
        setContentView(R.layout.main_screen)
        audioCutterFrg = AudioCutterScreen()
        supportFragmentManager.beginTransaction().add(R.id.ln_main, audioCutterFrg).commit()
        supportFragmentManager.beginTransaction().show(audioCutterFrg).commit()
    }



}