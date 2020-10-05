package com.example.audiocutter.activities.acttest

import android.os.Bundle
import android.view.View
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseActivity
import com.example.audiocutter.functions.audiocutterscreen.widget.WaveAudio
import kotlinx.android.synthetic.main.act_testlayout.*

class TmpAct : BaseActivity(), View.OnClickListener {
    lateinit var waveAudio: WaveAudio


    override fun createView(savedInstanceState: Bundle?) {
        setContentView(R.layout.act_testlayout)
        initViews()
    }


    private fun initViews() {
        waveAudio = findViewById(R.id.wave_audio)

        bt_start.setOnClickListener(this)
        bt_pause.setOnClickListener(this)
        bt_resume.setOnClickListener(this)

    }


    override fun onClick(p0: View) {
        when (p0.id) {
            R.id.bt_start -> waveAudio.resumeAnimation()
            R.id.bt_pause -> waveAudio.pauseAnimation()
            R.id.bt_resume -> waveAudio.resumeAnimation()
        }
    }

}

