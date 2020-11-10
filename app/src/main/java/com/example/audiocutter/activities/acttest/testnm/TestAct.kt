package com.example.audiocutter.activities.acttest.testnm

import android.content.Intent
import android.os.Bundle
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseActivity
import com.example.audiocutter.core.manager.ManagerFactory
import com.example.audiocutter.functions.audiochooser.dialogs.DialogAppShare
import com.example.audiocutter.objects.AudioFile
import kotlinx.android.synthetic.main.act_test.*
import java.io.File


class TestAct : BaseActivity(), DialogAppShare.DialogAppListener {
    private lateinit var dialog: DialogAppShare
    private var audioFile: AudioFile = AudioFile(
        File("/storage/emulated/0/AudioCutter/merger/vhkllllkj.mp3"),
        "hello",
        12588L,
        128,
        1255L,
        ManagerFactory.getAudioFileManager().getUriByPath(
            File("/storage/emulated/0/AudioCutter/merger/vhkllllkj.mp3")
        )!!,
        null,
        "",
        "",
        ""
    )


    override fun createView(savedInstanceState: Bundle?) {
        setContentView(R.layout.act_test)
        bt_start.setOnClickListener {
            dialog = DialogAppShare(this)
            dialog.setOnCallBack(this)
            dialog.show(supportFragmentManager, "TAG_DIALOG")
        }
    }


    override fun shareFileAudioToAppDevices() {
        dialog.dismiss()
        ManagerFactory.getAudioFileManager().shareFileAudio(audioFile)
    }

    override fun shareFilesToAppsDialog(position: Int) {
        val intent = Intent()
        intent.putExtra(Intent.EXTRA_STREAM, audioFile.uri)
        intent.type = "audio/*"
        intent.`package` = ManagerFactory.getAudioFileManager()
            .getListReceiveData()[position].activityInfo.packageName
        intent.action = Intent.ACTION_SEND
        startActivity(intent)
    }


}



