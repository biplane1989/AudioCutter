package com.example.audiocutter.activities.acttest

import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseActivity
import com.example.audiocutter.core.audioCutter.AudioCutterImpl
import com.example.audiocutter.core.manager.AudioCutConfig
import com.example.audiocutter.core.manager.AudioFormat
import com.example.audiocutter.core.manager.BitRate
import com.example.audiocutter.core.manager.Effect
import com.example.audiocutter.objects.AudioFile
import kotlinx.android.synthetic.main.activity_core_test.*
import java.io.File

open class CoreTestActivity : BaseActivity(), View.OnClickListener {
    private val audioCutterImpl = AudioCutterImpl()
    private lateinit var file: File
    private val NAME_FILE = "Anh Đợi Em Này_Thanh Hưng.mp3"
    private val PATH_FOLDER: String =
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            .plus("/$NAME_FILE").toString()

    private lateinit var audioFile: AudioFile

    companion object {
        private const val TAG = "CoreTestActivity"
    }


    override fun createView(savedInstanceState: Bundle?) {
        setContentView(R.layout.activity_core_test)
        file = File(PATH_FOLDER)
        test_cut_bt.setOnClickListener(this)
        audioCutterImpl.getAudioMergingInfo().observe(this, androidx.lifecycle.Observer {
            Log.e(TAG, "onCreate: " + it.percent)
        })
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            test_cut_bt.id -> {
                runOnUI {
                    audioFile = audioCutterImpl.cut(
                        AudioFile(
                            file,
                            NAME_FILE,
                            200000,
                            128,
                            360000,
                            null,
                            null,
                            "abc",
                            null, null, null, null
                        ),
                        AudioCutConfig(
                            20000,
                            60000,
                            100,
                            "testCut",
                            Effect.OFF,
                            Effect.OFF,
                            BitRate._128kb,
                            AudioFormat.MP3
                        )
                    )
                }
            }
        }
    }
}

private operator fun File.plus(separator: String): Any {
    return this.absolutePath + separator
}
