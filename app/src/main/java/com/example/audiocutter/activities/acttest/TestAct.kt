package com.example.audiocutter.activities.acttest

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.audiocutter.R
import com.example.audiocutter.core.audioManager.AudioFileManagerImpl
import com.example.audiocutter.objects.AudioFile
import java.io.File

class TestAct : AppCompatActivity(), View.OnClickListener {
    lateinit var delete: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_test)
        initViews()
    }

    private fun initViews() {
        delete = findViewById(R.id.bt_delete)
        delete.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        if (v.id == R.id.bt_delete) {
            val audioFile = AudioFile(
                File("/storage/emulated/0/VoiceRecorder/Recording_16.m4a"),
                "dd",
                100,
                128,
                0, Uri.parse("content://media/external/audio/media/414")
            )

           /* val rs = AudioFileManagerImpl.deleteFile(audioFile, )*/

            /*Log.d("TAG", "onClick: $rs")*/
        }
    }


}