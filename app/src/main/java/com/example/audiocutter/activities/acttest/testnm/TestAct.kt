package com.example.audiocutter.activities.acttest.testnm

import android.os.Bundle
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseActivity
import com.example.audiocutter.core.manager.ManagerFactory
import com.example.audiocutter.functions.audiochooser.dialogs.DialogAppShare
import com.example.audiocutter.objects.AudioFile
import kotlinx.android.synthetic.main.act_test.*
import java.io.File


class TestAct : BaseActivity() {
    val path = "/storage/emulated/0/AudioCutter/mixer/aloha - Copy (4).mp3"
    val file = File(path)
    private lateinit var dialog: DialogAppShare
    private var audioFile: AudioFile = AudioFile(
        file,
        "hello",
        12588L,
        128,
        1255L,
        ManagerFactory.getAudioFileManager().getUriByPath(
            file
        )!!,
        null,
        "",
        "",
        "", "",
        "",
        ".mp3"
    )


    override fun createView(savedInstanceState: Bundle?) {
        setContentView(R.layout.act_test)
        bt_start.setOnClickListener {
            ManagerFactory.getAudioFileManager().openWithApp(audioFile.uri!!)
        }
    }


}



