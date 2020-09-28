package com.example.audiocutter.activities.acttest

import android.os.Bundle
import android.view.View
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseActivity
import kotlinx.android.synthetic.main.act_test.*

class TestAct : BaseActivity(), View.OnClickListener {
    //        lateinit var audioCutterScreen: AudioCutterScreen
//    lateinit var recentAddedScreen: MixerAudioScreen
    override fun createView(savedInstanceState: Bundle?) {
        setContentView(R.layout.act_test)
        initViews()
    }

    private fun initViews() {
        bt_start.setOnClickListener(this)
        bt_stop.setOnClickListener(this)
        bt_resume.setOnClickListener(this)
        bt_pause.setOnClickListener(this)
//        audioCutterScreen = AudioCutterScreen()
//        recentAddedScreen = MixerAudioScreen()
//        supportFragmentManager.beginTransaction().replace(R.id.ln_main, recentAddedScreen).commit()
//        supportFragmentManager.beginTransaction().replace(R.id.ln_main, audioCutterScreen).commit()
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.bt_resume -> prg_view.resumeAnimator()
            R.id.bt_start
            -> prg_view.startAnimator(3000)
            R.id.bt_stop
            -> prg_view.stopAnimator()
            R.id.bt_pause
            -> prg_view.pauseAnimator()
        }
    }


}