package com.example.audiocutter.activities.acttest

import android.os.Bundle
import android.util.Log
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseActivity
import com.example.audiocutter.core.ManagerFactory
import kotlinx.android.synthetic.main.act_test.*

class TestAct : BaseActivity() {

//        lateinit var merChoose: AudioCutterScreen


    override fun createView(savedInstanceState: Bundle?) {
        setContentView(R.layout.act_test)
        initViews()

    }


    private fun initViews() {
        val path =
            "/storage/emulated/0/AudioCutter/merger/daylabaimix - Copy.mp3"

        bt_check.setOnClickListener {
            val x = ManagerFactory.getAudioFileManager().buildAudioFile(path)
            Log.d(
                "TAG",
                "checkFileBuild: data :${x.file.absolutePath} \n name : ${x.fileName}    \n" +
                        " duration: ${x.time} \n size ${x.size}  " +
                        " \n URI ${x.uri} \n bitmap :${x.bitmap} \n" +
                        " title : ${x.title}   \n" + " album  ${x.alBum}  \n" +
                        " artist: ${x.artist} \n" + " genre ${x.genre}  \n date ${x.dateAdded}   \n type  ${x.mimeType}"
            )
        }
//        merChoose = AudioCutterScreen()

//        supportFragmentManager.beginTransaction().add(R.id.ln_main, merChoose).commit()


    }

}

