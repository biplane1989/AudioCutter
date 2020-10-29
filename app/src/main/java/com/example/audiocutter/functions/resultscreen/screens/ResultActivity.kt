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

    companion object {
        const val AUDIO_FILE_PATH_KEY1 = "AUDIO_FILE_PATH_KEY1"
        const val AUDIO_FILE_PATH_KEY2 = "AUDIO_FILE_PATH_KEY2"
        const val AUDIO_FILE_TYPE_MERGING = "AUDIO_FILE_TYPE_MERGING"
        const val AUDIO_CUTCONFIG = "AUDIO_CUTCONFIG"
        const val AUDIO_MIXCONFIG = "AUDIO_MIXCONFIG"
        const val LIST_AUDIO_FILE_MERGING = "AUDIO_FILE_AUDIO_CUT_CONFIG"
        const val AUDIO_FILE_NAME = "AUDIO_FILE_NAME"
        const val AUDIO_FILE_PATH = "AUDIO_FILE_PATH"
        const val AUDIO_FORMAT = "AUDIO_FORMAT"


        const val MIX = 3
        const val MER = 2
        const val CUT = 1

        fun startActivity(context: Context, audioPath1: String?, audioPath2: String?, typeMerging: Int, audioCutConfig: AudioCutConfig?, audioMixConfig: AudioMixConfig?, listAudio: ArrayList<String>?, fileName: String?, filePath: String, audioFormat: String?) {
            val intent = Intent(context, ResultActivity::class.java)
            intent.putExtra(AUDIO_FILE_PATH_KEY1, audioPath1)
            intent.putExtra(AUDIO_FILE_PATH_KEY2, audioPath2)
            intent.putExtra(AUDIO_FILE_TYPE_MERGING, typeMerging)
            intent.putStringArrayListExtra(LIST_AUDIO_FILE_MERGING, listAudio)
            intent.putExtra(AUDIO_FILE_NAME, fileName)
            intent.putExtra(AUDIO_FILE_PATH, filePath)
            intent.putExtra(AUDIO_CUTCONFIG, audioCutConfig)
            intent.putExtra(AUDIO_MIXCONFIG, audioMixConfig)
            intent.putExtra(AUDIO_FORMAT, audioFormat)

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
        val listPathAudio = intent.getStringArrayListExtra(LIST_AUDIO_FILE_MERGING)
        val fileName = intent.getStringExtra(AUDIO_FILE_NAME)
        val filePath = intent.getStringExtra(AUDIO_FILE_PATH)
        val audioCutConfig = intent.getParcelableExtra<AudioCutConfig>(AUDIO_CUTCONFIG)
        val audioMixConfig = intent.getParcelableExtra<AudioMixConfig>(AUDIO_MIXCONFIG)
        val audioFormat = intent.getStringExtra(AUDIO_FORMAT)

        val file = File(Environment.getExternalStorageDirectory().toString() + filePath)
        val output = AudioFile(file, file.name, file.length())
        val outFile = File(Environment.getExternalStorageDirectory().toString() + filePath)

        val newListPathAudio = ArrayList<AudioFile>()

        listPathAudio?.let {
            for (item in listPathAudio) {
                newListPathAudio.add(ManagerFactory.getAudioFileManager().buildAudioFile(item))
            }
        }

//        val audioCutConfig = AudioCutConfig(1f, 100f, 300, "Cuting1", Effect.OFF, Effect.OFF, BitRate._128kb, AudioFormat.MP3, file.absolutePath)
//        val audioMixConfig = AudioMixConfig("mixing1", MixSelector.LONGEST, 100, 100, AudioFormat.MP3, file.absolutePath)

        when (typeMerging) {
            MIX -> {
                if (!audioPath2.isNullOrBlank() && !audioPath1.isNullOrBlank()) {
                    val audioFile1 = ManagerFactory.getAudioFileManager().buildAudioFile(audioPath1)
                    val audioFile2 = ManagerFactory.getAudioFileManager().buildAudioFile(audioPath2)
                    if (audioMixConfig != null && fileName != null) {
                        ManagerFactory.getAudioEditorManager()
                            .mixAudio(audioFile1, audioFile2, audioMixConfig, output, fileName)
                    }
                }
            }
            CUT -> {
                if (!audioPath1.isNullOrBlank()) {
                    val audioFile1 = ManagerFactory.getAudioFileManager().buildAudioFile(audioPath1)
                    if (audioCutConfig != null && fileName != null) {
                        ManagerFactory.getAudioEditorManager()
                            .cutAudio(audioFile1, audioCutConfig, outFile, fileName)
                    }

                }
            }
            MER -> {
                if (fileName != null) {
                    audioFormat?.let {
                        var format = AudioFormat.MP3
                        if (audioFormat == AudioFormat.MP3.toString()) {
                            format = AudioFormat.MP3
                        } else {
                            format = AudioFormat.ACC
                        }
                        ManagerFactory.getAudioEditorManager()
                            .mergeAudio(newListPathAudio, format, output, fileName)
                    }
                }
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()

        Log.d(TAG, "onBackPressed: ###### ")
    }
}