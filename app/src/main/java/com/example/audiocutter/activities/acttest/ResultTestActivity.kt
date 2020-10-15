package com.example.audiocutter.activities.acttest

import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.audiocutter.R
import com.example.audiocutter.core.ManagerFactory
import com.example.audiocutter.functions.resultscreen.CuttingConfig
import com.example.audiocutter.functions.resultscreen.ResultScreen
import com.example.audiocutter.functions.resultscreen.ResultService
import com.example.audiocutter.objects.AudioFile
import kotlinx.android.synthetic.main.activity_result_test.*
import java.io.File


class ResultTestActivity : AppCompatActivity() {


    val file = File(Environment.getExternalStorageDirectory()
        .toString() + "/Download/doihoamattroi.mp3")
    val audioFile = AudioFile(file, "file_name2", 10000, 128)
    val cuttingConfig = CuttingConfig(1)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result_test)

        val fragment: Fragment = ResultScreen()
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()

        fragmentTransaction.replace(R.id.fl_test, fragment)
        fragmentTransaction.commit()
//        val service = Intent(this, ResultService::class.java)
//        startService(service)

//        ManagerFactory.getAudioEditorManager().startService()

        btn_result.setOnClickListener(View.OnClickListener {

            ManagerFactory.getAudioEditorManager().cutAudio(audioFile, cuttingConfig, file)
//            ManagerFactory.getAudioEditorManager().bindService()

        })


    }
}