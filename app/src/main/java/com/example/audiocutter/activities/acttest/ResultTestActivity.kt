package com.example.audiocutter.activities.acttest

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseActivity
import com.example.audiocutter.core.ManagerFactory
import com.example.audiocutter.functions.resultscreen.objects.CuttingConfig
import com.example.audiocutter.functions.resultscreen.objects.MergingConfig
import com.example.audiocutter.functions.resultscreen.objects.MixingConfig
import com.example.audiocutter.functions.resultscreen.screens.ResultActivity
import com.example.audiocutter.functions.resultscreen.screens.ResultScreen
import com.example.audiocutter.objects.AudioFile
import kotlinx.android.synthetic.main.activity_result_test.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File


class ResultTestActivity : AppCompatActivity() {

    val TAG = "giangtd"
    val file = File(Environment.getExternalStorageDirectory()
        .toString() + "/Music/lonely.mp3")
    val audioFile = AudioFile(file, "T o m a t o", 100000, 128, uri = Uri.parse(file.absolutePath))

    //    val audioFile = ManagerFactory.getAudioFileManager().buildAudioFile(file.absolutePath)
    val cuttingConfig = CuttingConfig(1)
    val mixConfig = MixingConfig(1)
    val merConfig = MergingConfig(1)

    val listAudio = ArrayList<AudioFile>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result_test)

        listAudio.add(audioFile)
        listAudio.add(audioFile)

        btn_result.setOnClickListener(View.OnClickListener {

            ManagerFactory.getAudioEditorManager().cutAudio(audioFile, cuttingConfig, file)
            val intent = Intent(this, ResultActivity::class.java)
            startActivity(intent)
        })

        btn_mix.setOnClickListener(View.OnClickListener {
            ManagerFactory.getAudioEditorManager()
                .mixAudio(audioFile, audioFile, mixConfig, audioFile)
            val intent = Intent(this, ResultActivity::class.java)
            startActivity(intent)
        })

        btn_mer.setOnClickListener(View.OnClickListener {
            ManagerFactory.getAudioEditorManager().mergeAudio(listAudio, merConfig, audioFile)
            val intent = Intent(this, ResultActivity::class.java)
            startActivity(intent)
        })


    }

}