package com.example.audiocutter.base.viewstate

import android.content.Context
import android.content.Intent
import androidx.navigation.fragment.findNavController
import com.example.a0025antivirusapplockclean.base.viewstate.ViewStateMutable
import com.example.a0025antivirusapplockclean.base.viewstate.ViewStateScreen
import com.example.audiocutter.R
import com.example.audiocutter.activities.MainActivity
import com.example.audiocutter.base.BaseFragment
import com.example.audiocutter.functions.mystudio.screens.OutputActivity
import com.example.audiocutter.functions.resultscreen.screens.ResultActivity
import com.example.audiocutter.objects.AudioFile
import com.example.core.core.AudioCutConfig
import com.example.core.core.AudioMixConfig

interface AudioEditorViewStateManager {
    fun getViewStateMutable(): ViewStateMutable
    fun editorSaveMixingAudio(context: Context, audioFile1: AudioFile, audioFile2: AudioFile, filePath: String, audioMixConfig: AudioMixConfig) {
        if (getViewStateMutable().getLastState() == ViewStateScreen.MIXING_EDITOR_SCREEN) {
            getViewStateMutable().pushViewState(ViewStateScreen.RESULT_SCREEN)
            ResultActivity.startActivity(context, audioFile1.file.absolutePath, audioFile2.file.absolutePath, ResultActivity.MIX, null, audioMixConfig, null, null, filePath, null)
        }
    }

    fun editorSaveCutingAudio(context: Context, audioFile: AudioFile, audioCutConfig: AudioCutConfig, filePath: String, audioFormat: String) {
        if (getViewStateMutable().getLastState() == ViewStateScreen.CUTTING_EDITOR_SCREEN) {
            getViewStateMutable().pushViewState(ViewStateScreen.RESULT_SCREEN)
            ResultActivity.startActivity(context, audioFile.file.absolutePath, "", ResultActivity.CUT, audioCutConfig, null, null, null, filePath, audioFormat)
        }
    }

    fun editorSaveMergingAudio(context: Context, listPathAudio: ArrayList<String>, fileName: String, filePath: String, audioFormat: String) {
        if (getViewStateMutable().getLastState() == ViewStateScreen.MERGING_EDITOR_SCREEN) {
            getViewStateMutable().pushViewState(ViewStateScreen.RESULT_SCREEN)
            ResultActivity.startActivity(context, null, null, ResultActivity.MER, null, null, listPathAudio, fileName, filePath, audioFormat)
        }
    }

    fun resultScreenGoToHome(context: Context) {
        if (getViewStateMutable().getLastState() == ViewStateScreen.RESULT_SCREEN) {
//            getViewStateMutable().pushViewState(ViewStateScreen.HOME_SCREEN)
            val intent = Intent(context, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            context.startActivity(intent)
        }
    }
}