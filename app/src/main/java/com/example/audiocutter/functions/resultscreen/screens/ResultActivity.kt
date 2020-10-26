package com.example.audiocutter.functions.resultscreen.screens

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseActivity
import com.example.audiocutter.core.manager.ManagerFactory
import com.example.audiocutter.functions.resultscreen.objects.MergingConfig
import com.example.audiocutter.objects.AudioFile
import com.example.core.core.AudioCutConfig
import com.example.core.core.AudioFormat
import com.example.core.core.AudioMixConfig
import com.example.core.core.MixSelector
import java.io.File

class ResultActivity : BaseActivity() {
    val TAG = "giangtd"

    val file = File(Environment.getExternalStorageDirectory().toString() + "/Music/lonely.mp3")
    val file2 = File(Environment.getExternalStorageDirectory().toString() + "/Music")

    companion object {
        const val AUDIO_FILE_PATH_KEY1 = "AUDIO_FILE_PATH_KEY1"
        const val AUDIO_FILE_PATH_KEY2 = "AUDIO_FILE_PATH_KEY2"
        const val AUDIO_FILE_TYPE_MERGING = "AUDIO_FILE_TYPE_MERGING"
        const val AUDIO_FILE_AUDIO_CONFIG = "AUDIO_FILE_TYPE_MERGING"

        const val MIX = 3
        const val MER = 2
        const val CUT = 1

        fun startActivity(context: Context, audioPath1: String, audioPath2: String?, typeMerging: Int) {
            val intent = Intent(context, ResultActivity::class.java)
            intent.putExtra(AUDIO_FILE_PATH_KEY1, audioPath1)
            intent.putExtra(AUDIO_FILE_PATH_KEY2, audioPath2)
            intent.putExtra(AUDIO_FILE_TYPE_MERGING, typeMerging)

            context.startActivity(intent)
        }
    }

    override fun onPostCreate() {
        super.onPostCreate()
        setContentView(R.layout.activity_result)
    }

    override fun createView(savedInstanceState: Bundle?) {

        val audioPath1 = intent.getStringExtra(ResultActivity.AUDIO_FILE_PATH_KEY1)!!
        val audioPath2 = intent.getStringExtra(ResultActivity.AUDIO_FILE_PATH_KEY2)!!
        val typeMerging = intent.getIntExtra(ResultActivity.AUDIO_FILE_TYPE_MERGING, 0)
        val outFile = File(Environment.getExternalStorageDirectory().toString() + "/Music/orange")
        val output = AudioFile(outFile, outFile.name, file.length())

        val mergingConfig = MergingConfig(AudioFormat.MP3)
        val mixingConfig = AudioMixConfig(outFile.name, MixSelector.LONGEST, 100, 100, AudioFormat.MP3, file2.absolutePath)

        val audioFile1 = ManagerFactory.getAudioFileManager().buildAudioFile(audioPath1)

        if (!audioPath2.isNullOrBlank()) {
            val audioFile2 = ManagerFactory.getAudioFileManager().buildAudioFile(audioPath2)
            when (typeMerging) {
                MER -> {
                    ManagerFactory.getAudioEditorManager()
                        .mergeAudio(arrayListOf(audioFile1, audioFile2), mergingConfig, output)
                }
                MIX -> {
                    ManagerFactory.getAudioEditorManager()
                        .mixAudio(audioFile1, audioFile2, mixingConfig, output)
                }
            }
        } else {
            when (typeMerging) {
                CUT -> {

                }
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()

        Log.d(TAG, "onBackPressed: ###### ")
    }
}