package com.example.audiocutter.activities.acttest

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Environment.DIRECTORY_DOWNLOADS
import android.util.Log
import android.view.View
import androidx.lifecycle.Observer
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseActivity
import com.example.core.core.*
import kotlinx.android.synthetic.main.activity_core_test.*
import java.io.File


class CoreTestActivity : BaseActivity(), View.OnClickListener {
    private val audioCutterImpl = AudioCutterImpl()
    private lateinit var file: File
    private val NAME_FILE = "Anh Đợi Em Này_Thanh Hưng"
    private val PATH_FOLDER: String =
        Environment.getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS)
            .plus("/${NAME_FILE.plus(".mp3")}").toString()

    private var path = Environment.getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS).toString()
    private lateinit var audioFile: AudioCore
    private var listAudio = ArrayList<AudioCore>()

    companion object {
        private const val TAG = "CoreTestActivity"
    }


    override fun createView(savedInstanceState: Bundle?) {
        setContentView(R.layout.activity_core_test)
        file = File(PATH_FOLDER)
        var file1 = File(path.plus("/Tướng Quân.flac"))
        var file2 = File(path.plus("/sample1.aac"))
        var file3 = File(path.plus("/file_example_WAV_1MG.wav"))
        test_cut_bt.setOnClickListener(this)
        test_concat_bt.setOnClickListener(this)
        test_cancel.setOnClickListener(this)
        test_mix.setOnClickListener(this)
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
        var item = AudioCore(
            file,
            NAME_FILE,
            file.length(),
            128,
            getTimeAudio(file, this),
            ".mp3"
        )
        var item1 = AudioCore(
            file1,
            "Tướng Quân",
            file1.length(),
            128,
            getTimeAudio(file1, this),
            ".flac"
        )
        var item2 = AudioCore(
            file2,
            "sample1",
            file2.length(),
            128,
            getTimeAudio(file2, this),
            ".aac"
        )
        var item3 = AudioCore(
            file3,
            "file_example_WAV_1MG",
            file3.length(),
            128,
            getTimeAudio(file3, this), ".wav"
        )
        listAudio.add(item)
        listAudio.add(item1)
        listAudio.add(item2)
        listAudio.add(item3)

    }

    override fun onClick(p0: View?) {
        when (p0) {
            test_cut_bt -> {
                runOnUI {
                    audioFile = audioCutterImpl.cut(
                        listAudio[0],
                        AudioCutConfig(
                            20,
                            60,
                            300,
                            "testCut1",
                            Effect.AFTER_6_S,
                            Effect.AFTER_6_S,
                            BitRate._256kb,
                            AudioFormat.ACC
                        )
                    )
                    Log.e(TAG, "cut: $audioFile ")
                }
            }
            test_concat_bt -> {
                runOnUI {
                    var audioFile = audioCutterImpl.merge(listAudio, "concatVideo", AudioFormat.ACC)
                    Log.e(TAG, "concat: $audioFile ")
                }
            }
            test_cancel -> {
                runOnUI {
                    audioCutterImpl.cancelTask()
                }
            }
            test_mix -> {
                runOnUI {
                    audioCutterImpl.mix(
                        listAudio[0],
                        listAudio[1],
                        AudioMixConfig(
                            "testMix",
                            MixSelector.LONGEST,
                            100,
                            100, AudioFormat.ACC
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

fun getTimeAudio(file: File, context: Context): Long {
    val mp: MediaPlayer = MediaPlayer.create(context, Uri.parse(file.absolutePath))
    val duration = mp.duration
    mp.release()
    return duration.toLong()
}

private operator fun File.plus(separator: String): Any {
    return this.absolutePath + separator
}
