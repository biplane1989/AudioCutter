package com.example.audiocutter.activities.acttest

import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseActivity
import com.example.audiocutter.functions.audiocutterscreen.view.screen.AudioCutterScreen

class TestAct() : BaseActivity() {
    lateinit var audioCutterScreen: AudioCutterScreen

    //    lateinit var recentAddedScreen: MixerAudioScreen
    override fun createView(savedInstanceState: Bundle?) {
        setContentView(R.layout.act_test)
        initViews()
    }

    private fun initViews() {
//        bt_start.setOnClickListener(this)
//        bt_stop.setOnClickListener(this)
//        bt_resume.setOnClickListener(this)
//        bt_pause.setOnClickListener(this)
        audioCutterScreen = AudioCutterScreen()
//        recentAddedScreen = MixerAudioScreen()
//        supportFragmentManager.beginTransaction().replace(R.id.ln_main, recentAddedScreen).commit()
        supportFragmentManager.beginTransaction().replace(R.id.ln_main, audioCutterScreen).commit()
    }

}

