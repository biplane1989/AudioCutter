package com.example.audiocutter.activities.acttest

import android.os.Bundle
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseActivity
import com.example.audiocutter.core.audioManager.Folder
import com.example.audiocutter.core.manager.ManagerFactory
import kotlinx.android.synthetic.main.act_test.*

class TestAct : BaseActivity() {



    override fun createView(savedInstanceState: Bundle?) {
        setContentView(R.layout.act_test)
        initViews()
    }


    private fun initViews() {
        bt_start.setOnClickListener {
            ManagerFactory.getAudioFileManager()
                .createValidFileName("AudioCutter_AudioCutter_lonel", Folder.TYPE_CUTTER)
        }
    }



}

