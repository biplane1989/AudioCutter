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
                val audio = ManagerFactory.getAudioFileManager().buildAudioFile("/storage/emulated/0/AudioCutter/cutter/Cung-Dan-Tinh-Yeu-Dan-Truong-My-Tam.mp3")
               val tmp = ManagerFactory.getAudioFileManager().saveFile(audio, Folder.TYPE_MIXER)
                Log.d("TAG", "checkState: state ${tmp.name}")
            }
        }
    }



}

