package com.example.audiocutter.functions.resultscreen.screens

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.util.Log
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseActivity
import com.example.audiocutter.core.manager.ManagerFactory
import com.example.audiocutter.objects.AudioFile
import com.example.core.core.*
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
        const val LIST_AUDIO_FILE_MERGING = "AUDIO_FILE_AUDIO_CUT_CONFIG"

        const val MIX = 3
        const val MER = 2
        const val CUT = 1

        fun startActivity(context: Context, audioPath1: String?, audioPath2: String?, typeMerging: Int, audioCutConfig: AudioCutConfig?, listAudio: ArrayList<String>?) {
            val intent = Intent(context, ResultActivity::class.java)
            intent.putExtra(AUDIO_FILE_PATH_KEY1, audioPath1)
            intent.putExtra(AUDIO_FILE_PATH_KEY2, audioPath2)
            intent.putExtra(AUDIO_FILE_TYPE_MERGING, typeMerging)
            intent.putStringArrayListExtra(LIST_AUDIO_FILE_MERGING, listAudio)
//            intent.putExtra(AUDIO_FILE_AUDIO_CUT_CONFIG, audioCutConfig)
            context.startActivity(intent)
        }
    }

    override fun onPostCreate() {
        super.onPostCreate()
        setContentView(R.layout.activity_result)
        ManagerFactory.getAudioPlayer().stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        ManagerFactory.getAudioPlayer().stop()
    }

    override fun createView(savedInstanceState: Bundle?) {

        val audioPath1 = intent.getStringExtra(AUDIO_FILE_PATH_KEY1)
        val audioPath2 = intent.getStringExtra(AUDIO_FILE_PATH_KEY2)
        val typeMerging = intent.getIntExtra(AUDIO_FILE_TYPE_MERGING, 0)
        val outFile = File(Environment.getExternalStorageDirectory().toString() + "/Music/orange")
        val output = AudioFile(outFile, outFile.name, file.length())
        val listPathAudio = intent.getStringArrayListExtra(LIST_AUDIO_FILE_MERGING)

        val newListPathAudio = ArrayList<AudioFile>()

        listPathAudio?.let {
            for (item in listPathAudio) {
                newListPathAudio.add(ManagerFactory.getAudioFileManager().buildAudioFile(item))
            }
        }

//        val audioCutConfig = intent.getParcelableExtra<AudioCutConfig>(AUDIO_FILE_AUDIO_CUT_CONFIG)

        val audioCutConfig = AudioCutConfig(1.0f, 100f, 300, "lemon", Effect.OFF, Effect.OFF, BitRate._128kb, AudioFormat.MP3, file2.absolutePath)

        val audioFormat = AudioFormat.MP3
        val mixingConfig = AudioMixConfig(outFile.name, MixSelector.LONGEST, 100, 100, AudioFormat.MP3, file2.absolutePath)

        when (typeMerging) {
            MIX -> {
                if (!audioPath2.isNullOrBlank() && !audioPath1.isNullOrBlank()) {
                    val audioFile1 = ManagerFactory.getAudioFileManager().buildAudioFile(audioPath1)
                    val audioFile2 = ManagerFactory.getAudioFileManager().buildAudioFile(audioPath2)
                    ManagerFactory.getAudioEditorManager()
                        .mixAudio(audioFile1, audioFile2, mixingConfig, output)
                }
            }
            CUT -> {
                if (!audioPath1.isNullOrBlank()) {
                    val audioFile1 = ManagerFactory.getAudioFileManager().buildAudioFile(audioPath1)
                    audioCutConfig?.let {
                        ManagerFactory.getAudioEditorManager()
                            .cutAudio(audioFile1, audioCutConfig, outFile)
                    }
                }
            }
            MER -> {
                ManagerFactory.getAudioEditorManager()
                    .mergeAudio(newListPathAudio, audioFormat, file2.absolutePath)
            }

        }

        /*if (!audioPath2.isNullOrBlank() && !audioPath1.isNullOrBlank()) {
            val audioFile1 = ManagerFactory.getAudioFileManager().buildAudioFile(audioPath1)
            val audioFile2 = ManagerFactory.getAudioFileManager().buildAudioFile(audioPath2)
            when (typeMerging) {
                MIX -> {
                    ManagerFactory.getAudioEditorManager()
                        .mixAudio(audioFile1, audioFile2, mixingConfig, output)
                }
            }
        }
        if (!audioPath1.isNullOrBlank()) {
            val audioFile1 = ManagerFactory.getAudioFileManager().buildAudioFile(audioPath1)
            when (typeMerging) {
                CUT -> {
                    audioCutConfig?.let {
                        ManagerFactory.getAudioEditorManager()
                            .cutAudio(audioFile1, audioCutConfig, outFile)
                    }
                }
            }
        }
        when (typeMerging) {
            MER -> {
                ManagerFactory.getAudioEditorManager()
                    .mergeAudio(newListPathAudio, mergingConfig, output)
            }
        }*/
    }

    override fun onBackPressed() {
        super.onBackPressed()

        Log.d(TAG, "onBackPressed: ###### ")
    }
}