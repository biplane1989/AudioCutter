package com.example.audiocutter.activities.acttest

import android.os.Bundle
import android.util.Log
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseActivity
import com.example.audiocutter.core.audioManager.Folder
import com.example.audiocutter.core.manager.ManagerFactory
import kotlinx.android.synthetic.main.act_test.*
import kotlinx.coroutines.delay

class TestAct : BaseActivity() {



    override fun createView(savedInstanceState: Bundle?) {
        setContentView(R.layout.act_test)
        initViews()
    }


    private fun initViews() {
        bt_start.setOnClickListener {
            runOnUI {
                for (i in 0..100){

                    val duration =ManagerFactory.getAudioCutter().getAudioInfo("/storage/emulated/0/Mp3Cutter/AudioCutter/AudioCutter_AudioCutter_AudioMerger_AudioMix_AudioCutter_AudioMerger_AudioCutter.mp3")!!.duration
                    Log.d("TAG", "initViews:  duration $duration pos $i")
                }

            }
        }
    }



}

