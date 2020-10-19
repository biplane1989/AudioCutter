package com.example.audiocutter.activities.acttest

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
        .toString() + "/Download/doihoamattroi.mp3")
    val audioFile = AudioFile(file,"T o m a t o",1000, 128, uri = Uri.parse(file.absolutePath))
//    val audioFile = ManagerFactory.getAudioFileManager().buildAudioFile(file.absolutePath)
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