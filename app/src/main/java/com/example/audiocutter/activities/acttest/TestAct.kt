package com.example.audiocutter.activities.acttest

import android.os.Bundle
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseActivity
import com.example.audiocutter.core.manager.ManagerFactory
import kotlinx.android.synthetic.main.act_test.*
import java.io.File

class TestAct : BaseActivity() {



    override fun createView(savedInstanceState: Bundle?) {
        setContentView(R.layout.act_test)
        initViews()
    }


    private fun initViews() {
        bt_start.setOnClickListener {

            val a = ManagerFactory.getAudioFileManager()
                .insertFileToMediastore(File("/storage/emulated/0/AudioCutter/merger/Cung-Dan-Tinh-Yeu-Dan-Truong-My-Tam.mp3"))
        }
    }



}

