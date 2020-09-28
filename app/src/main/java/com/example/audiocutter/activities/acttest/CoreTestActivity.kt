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
import com.example.audiocutter.core.audioCutter.AudioCutterImpl
import com.example.audiocutter.core.manager.*
import com.example.audiocutter.objects.AudioFile
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
    private lateinit var audioFile: AudioFile
    private var listAudio = ArrayList<AudioFile>()

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
        var item = AudioFile(
            file,
            NAME_FILE,
            200000,
            128,
            getTimeAudio(file, this),
            null,
            null,
            "abc",
            null, null, null, null, ".mp3"
        )
        var item1 = AudioFile(
            file1,
            "Tướng Quân",
            file1.length(),
            128,
            getTimeAudio(file1, this),
            null,
            null,
            "abc",
            null, null, null, null, ".flac"
        )
        var item2 = AudioFile(
            file2,
            "sample1",
            file2.length(),
            128,
            getTimeAudio(file2, this),
            null,
            null,
            "abc",
            null, null, null, null, ".aac"
        )
        var item3 = AudioFile(
            file3,
            "file_example_WAV_1MG",
            file3.length(),
            128,
            getTimeAudio(file3, this),
            null,
            null,
            "abc",
            null, null, null, null, ".wav"
        )
        listAudio.add(item1)
        listAudio.add(item2)
        listAudio.add(item3)
        listAudio.add(item)

    }

    override fun onClick(p0: View?) {
        when (p0) {
            test_cut_bt -> {
                runOnUI {
                    val audioFile1 = AudioFile(
                        file,
                        NAME_FILE,
                        200000,
                        128,
                        360000,
                        null,
                        null,
                        "abc",
                        null, null, null, null, ".mp3"
                    )
                    audioFile = audioCutterImpl.cut(
                        audioFile1,
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
                    Log.e(TAG, "cut: $audioFile ")
                }
            }
            test_concat_bt -> {
                runOnUI {
                    var audioFile = audioCutterImpl.merge(listAudio, "concatVideo")
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
                        listAudio[2],
                        AudioMixConfig("testMix", MixSelector.LONGEST, 100, 50)
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
