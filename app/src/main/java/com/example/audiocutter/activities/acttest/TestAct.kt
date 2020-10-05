package com.example.audiocutter.activities.acttest

import android.os.Bundle
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseActivity
import com.example.audiocutter.functions.mergeraudioscreen.view.MergerScreen

class TestAct() : BaseActivity() {
    //    lateinit var audioCutterScreen: AudioCutterScreen
//    lateinit var mixScreen: MixerAudioScreen
    lateinit var merScreen: MergerScreen


    override fun createView(savedInstanceState: Bundle?) {
        setContentView(R.layout.act_test)
        initViews()
    }


    private fun initViews() {

//        audioCutterScreen = AudioCutterScreen()
//        mixScreen = MixerAudioScreen()
        merScreen = MergerScreen()
//        supportFragmentManager.beginTransaction().replace(R.id.ln_main, mixScreen).commit()
//        supportFragmentManager.beginTransaction().replace(R.id.ln_main, audioCutterScreen).commit()
        supportFragmentManager.beginTransaction().replace(R.id.ln_main, merScreen).commit()

    }



//    override fun onClick(p0: View) {
//        when (p0.id) {
//            R.id.bt_start -> waveAudio.resumeAnimation()
//            R.id.bt_pause -> waveAudio.pauseAnimation()
//            R.id.bt_resume -> waveAudio.resumeAnimation()
//        }
//    }

}

