package com.example.audiocutter.activities.acttest

import android.os.Bundle
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseActivity
import com.example.audiocutter.functions.audiocutterscreen.objs.AudioCutterView
import com.example.audiocutter.functions.mergescreen.event.OnActionCallback
import com.example.audiocutter.functions.mergescreen.m001merge.view.MergeScreen
import com.example.audiocutter.functions.mergescreen.m002mergechoose.view.MergeChooseScreen

class TestAct() : BaseActivity(), OnActionCallback {
    //    lateinit var audioCutterScreen: AudioCutterScreen
    lateinit var merChoose: MergeChooseScreen
    lateinit var merScreen: MergeScreen


    override fun createView(savedInstanceState: Bundle?) {
        setContentView(R.layout.act_test)
        initViews()

    }


    private fun initViews() {

        merChoose = MergeChooseScreen()
        merScreen = MergeScreen()
        merScreen.setOnCalBack(this)
        merChoose.setOnCalBack(this)
        supportFragmentManager.beginTransaction().add(R.id.ln_main, merChoose)
            .add(R.id.ln_main, merScreen).commit()
        showMergeScr()

    }

    private fun showMergeScr() {
        supportFragmentManager.beginTransaction().show(merScreen).hide(merChoose).commit()
    }

    override fun sendAndReceiveData(listData: List<AudioCutterView>) {
        merChoose.receiveData(listData)
        supportFragmentManager.beginTransaction().show(merChoose).hide(merScreen).commit()
    }

    override fun backFrg() {
        supportFragmentManager.beginTransaction().show(merScreen).hide(merChoose).commit()

    }



//    override fun onClick(p0: View) {
//        when (p0.id) {
//            R.id.bt_start -> waveAudio.resumeAnimation()
//            R.id.bt_pause -> waveAudio.pauseAnimation()
//            R.id.bt_resume -> waveAudio.resumeAnimation()
//        }
//    }

}

