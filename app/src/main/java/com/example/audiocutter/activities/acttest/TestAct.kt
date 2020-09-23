package com.example.audiocutter.activities.acttest

import android.os.Bundle
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseActivity
import com.example.audiocutter.functions.audiocutterscreen.view.screen.AudioCutterScreen

class TestAct : BaseActivity() {
    lateinit var audioCutterScreen: AudioCutterScreen

    override fun createView(savedInstanceState: Bundle?) {
        setContentView(R.layout.act_test)
        initViews()
    }

    private fun initViews() {
        audioCutterScreen = AudioCutterScreen()
        supportFragmentManager.beginTransaction().replace(R.id.ln_main, audioCutterScreen).commit()
    }




}