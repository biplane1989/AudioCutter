package com.example.audiocutter.activities.acttest

import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import androidx.lifecycle.Observer
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

class CoreTestActivity : BaseActivity(), View.OnClickListener {
    private val audioCutterImpl = AudioCutterImpl()
    private lateinit var file: File
    private val NAME_FILE = "Anh Đợi Em Này_Thanh Hưng"
    private val PATH_FOLDER: String =
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            .plus("/${NAME_FILE.plus(".mp3")}").toString()

    private lateinit var audioFile: AudioFile

    companion object {
        private const val TAG = "CoreTestActivity"
    }


    override fun createView(savedInstanceState: Bundle?) {
        setContentView(R.layout.activity_core_test)
        file = File(PATH_FOLDER)
        test_cut_bt.setOnClickListener(this)
        var i = 0
        audioCutterImpl.getAudioMergingInfo().observe(this, Observer {
            Log.e(TAG, "onCreate: " + it.percent + " i: " + i++)
        })

        for (t in 0..50) {
            val test = between(t, 0, 4) * (t / 4) + between(t, 4, 42) + between(
                t,
                44,
                50
            ) * (1 - (t / (50 - 44)))
            println(test)
        }

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
                            null, null, null, null, ".mp3"
                        ),
                        AudioCutConfig(
                            20,
                            60,
                            300,
                            "testCut1",
                            Effect.AFTER_6_S,
                            Effect.AFTER_6_S,
                            BitRate._320kb,
                            AudioFormat.MP3
                        )
                    )
                }
            }
        }
    }
}


fun between(t: Int, start: Int, end: Int): Int {
    return if (t in start..end)
        1
    else
        0
}

private operator fun File.plus(separator: String): Any {
    return this.absolutePath + separator
}
