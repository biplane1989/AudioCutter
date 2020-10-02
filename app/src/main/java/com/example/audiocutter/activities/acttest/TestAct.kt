package com.example.audiocutter.activities.acttest

import android.os.Bundle
import android.view.View
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseActivity
import com.example.audiocutter.functions.audiocutterscreen.widget.BarVisualizer
import com.example.audiocutter.functions.audiocutterscreen.widget.WaveAudio
import kotlinx.android.synthetic.main.act_test.*

class TestAct() : BaseActivity(), View.OnClickListener {
    //    lateinit var audioCutterScreen: AudioCutterScreen
    lateinit var waveAudio: WaveAudio

    private lateinit var mVisualizer: BarVisualizer


    override fun createView(savedInstanceState: Bundle?) {
        setContentView(R.layout.act_test)
        initViews()
    }


    private fun initViews() {
        waveAudio = findViewById(R.id.wave_audio)
//        mVisualizer = findViewById(R.id.bar)

        bt_start.setOnClickListener(this)
        bt_pause.setOnClickListener(this)
        bt_resume.setOnClickListener(this)

//        audioCutterScreen = AudioCutterScreen()
//        recentAddedScreen = MixerAudioScreen()
//        supportFragmentManager.beginTransaction().replace(R.id.ln_main, recentAddedScreen).commit()
//        supportFragmentManager.beginTransaction().replace(R.id.ln_main, audioCutterScreen).commit()
    }


    private fun stopPlayingAudio() {
        mVisualizer.release()
    }

    override fun onClick(p0: View) {
        when (p0.id) {
            R.id.bt_start -> waveAudio.startAnimator()
            R.id.bt_pause -> waveAudio.pauseAnimator()
            R.id.bt_resume -> waveAudio.resumeAnimator()
        }
    }

}

