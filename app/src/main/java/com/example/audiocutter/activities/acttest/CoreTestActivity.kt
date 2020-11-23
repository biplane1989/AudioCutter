package com.example.audiocutter.activities.acttest

import android.os.Bundle
import android.os.Environment
import android.os.Environment.DIRECTORY_DOWNLOADS
import android.util.Log
import android.view.View
import androidx.lifecycle.Observer
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseActivity
import com.example.audiocutter.util.Utils.getTimeAudio
import com.example.core.core.*
import kotlinx.android.synthetic.main.activity_core_test.*
import java.io.File


class CoreTestActivity : BaseActivity(), View.OnClickListener {
    private val audioCutterImpl = AudioCutterImpl()
    private lateinit var file: File
    private val NAME_FILE = "Anh Đợi Em Này_Thanh Hưng"
    private val PATH_FOLDER: String = Environment.getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS)
        .plus("/${NAME_FILE.plus(".mp3")}").toString()

    private var path = Environment.getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS).toString()
    private lateinit var audioFile: AudioCore
    private var listAudio = ArrayList<AudioCore>()


    companion object {
        val PATH_DEFAUL_FOLDER = "${Environment.getExternalStorageDirectory()}/AudioCutter/"
        val PATH_CUT_FOLDER = "${PATH_DEFAUL_FOLDER}cutter"
        val PATH_MERGE_FOLDER = "${PATH_DEFAUL_FOLDER}merger"
        val PATH_MIXER_FOLDER = "${PATH_DEFAUL_FOLDER}mixer"
        private const val TAG = "CoreTestActivity"
    }


    override fun createView(savedInstanceState: Bundle?) {
        setContentView(R.layout.activity_core_test)
        file = File(PATH_FOLDER)
        var file1 = File(path.plus("/test7 - Copy.aac"))
        var file2 = File(path.plus("/sample1.aac"))
        var file3 = File(path.plus("/file_example_WAV_1MG.wav"))
        var file4 = File(path.plus("/WeDon.m4a"))
        test_cut_bt.setOnClickListener(this)
        test_concat_bt.setOnClickListener(this)
        test_cancel.setOnClickListener(this)
        test_mix.setOnClickListener(this)
        var i = 0
        audioCutterImpl.getAudioMergingInfo().observe(this, Observer {
            Log.e(TAG, "onCreate: " + it.percent + " i: " + i++)
        })

        for (t in 0..50) {
            val test = between(t, 0, 4) * (t / 4) + between(t, 4, 42) + between(t, 44, 50) * (1 - (t / (50 - 44)))
            println(test)
        }
        var item = AudioCore(file, NAME_FILE, file.length(), 128, getTimeAudio(file, this), ".mp3")
        var item1 = AudioCore(file1, "Tướng Quân", file1.length(), 128, getTimeAudio(file1, this), ".flac")
        var item2 = AudioCore(file2, "sample1", file2.length(), 128, getTimeAudio(file2, this), ".aac")
        var item3 = AudioCore(file3, "file_example_WAV_1MG", file3.length(), 128, getTimeAudio(file3, this), ".wav")
        var item4 = AudioCore(file4, "WeDon", file4.length(), 128, getTimeAudio(file4, this), ".m4a")
        listAudio.add(item)
        listAudio.add(item1)
        listAudio.add(item2)
        listAudio.add(item3)
        listAudio.add(item4)

      /*  runOnUI {
            var a = System.currentTimeMillis()
            Log.e(TAG, "createView: " )
            val audioMergingInfo = audioCutterImpl.getAudioInfo(listAudio[1].file.absolutePath)
            Log.e(TAG, "createView: ${audioMergingInfo}" )
        }*/

    }

    override fun onClick(p0: View?) {
        when (p0) {
            test_cut_bt -> {
                runOnUI {
//                    audioFile = audioCutterImpl.cut(listAudio[4], AudioCutConfig(20.2f, 20.5f, 300, "testCut2", Effect.AFTER_3_S, Effect.AFTER_1_S, BitRate._128kb, AudioFormat.MP3, PATH_CUT_FOLDER))
                    Log.e(TAG, "cut: $audioFile ")
                }
            }
            test_concat_bt -> {
                runOnUI {
                    var audioFile = audioCutterImpl.merge(listAudio, "concatVideo", AudioFormat.ACC, PATH_MERGE_FOLDER)
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
                    //audioCutterImpl.mix(listAudio[0], listAudio[1], AudioMixConfig("testMix", MixSelector.LONGEST, 100, 100, AudioFormat.ACC, PATH_MIXER_FOLDER))
                }
            }
        }
    }
}


fun between(t: Int, start: Int, end: Int): Int {
    return if (t in start..end) 1
    else 0
}


private operator fun File.plus(separator: String): Any {
    return this.absolutePath + separator
}
