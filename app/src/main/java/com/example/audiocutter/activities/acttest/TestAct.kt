package com.example.audiocutter.activities.acttest

import android.os.Bundle
import android.util.Log
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseActivity
import com.example.audiocutter.core.ManagerFactory
import com.example.audiocutter.core.audioManager.Folder
import com.example.audiocutter.functions.audiocutterscreen.view.screen.AudioCutterScreen
import kotlinx.android.synthetic.main.act_test.*
import java.io.File

class TestAct : BaseActivity() {

        lateinit var merChoose: AudioCutterScreen


    override fun createView(savedInstanceState: Bundle?) {
        setContentView(R.layout.act_test)
        initViews()

    }


    private fun initViews() {
//
//            val file = File("/storage/emulated/0/Download/WeDon.m4a")
//            bt_get.setOnClickListener {
//                runOnUI {
//                    val rs = ManagerFactory.getAudioFileManager().saveFile(
//                    ManagerFactory.getAudioFileManager().buildAudioFile(filePath = file.absolutePath),
//                    Folder.TYPE_CUTTER
//                )
//                    Log.d("checkCodeGetduration", "initViews: $rs")
//                }
//        }
        merChoose = AudioCutterScreen()

        supportFragmentManager.beginTransaction().add(R.id.ln_main, merChoose).commit()


    }

}

