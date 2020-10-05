package com.example.audiocutter.activities.acttest

import android.os.Bundle
import android.view.View
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseActivity
import com.example.audiocutter.functions.audiocutterscreen.view.screen.AudioCutterScreen
import com.example.audiocutter.functions.audiocutterscreen.widget.BarVisualizer
import com.example.audiocutter.functions.audiocutterscreen.widget.WaveAudio

import kotlinx.android.synthetic.main.act_test.*

class TestAct() : BaseActivity() {
        lateinit var audioCutterScreen: AudioCutterScreen



    override fun createView(savedInstanceState: Bundle?) {
        setContentView(R.layout.act_test)
        initViews()
    }


    private fun initViews() {

        audioCutterScreen = AudioCutterScreen()
//        recentAddedScreen = MixerAudioScreen()
//        supportFragmentManager.beginTransaction().replace(R.id.ln_main, recentAddedScreen).commit()
        supportFragmentManager.beginTransaction().replace(R.id.ln_main, audioCutterScreen).commit()
    }



//    override fun onClick(p0: View) {
//        when (p0.id) {
//            R.id.bt_start -> waveAudio.resumeAnimation()
//            R.id.bt_pause -> waveAudio.pauseAnimation()
//            R.id.bt_resume -> waveAudio.resumeAnimation()
//        }
//    }

}

