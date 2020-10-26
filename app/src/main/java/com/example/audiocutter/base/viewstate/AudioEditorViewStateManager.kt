package com.example.audiocutter.base.viewstate

import android.content.Context
import com.example.a0025antivirusapplockclean.base.viewstate.ViewStateMutable
import com.example.a0025antivirusapplockclean.base.viewstate.ViewStateScreen
import com.example.audiocutter.functions.resultscreen.screens.ResultActivity
import com.example.audiocutter.objects.AudioFile
import com.example.core.core.AudioCutConfig

interface AudioEditorViewStateManager {
    fun getViewStateMutable(): ViewStateMutable
    fun editorSaveMixingAudio(context: Context, audioFile1: AudioFile, audioFile2: AudioFile) {
        if (getViewStateMutable().getLastState() == ViewStateScreen.MIXING_EDITOR_SCREEN) {
            getViewStateMutable().pushViewState(ViewStateScreen.RESULT_SCREEN)
            ResultActivity.startActivity(context, audioFile1.file.absolutePath, audioFile2.file.absolutePath, ResultActivity.MIX)
        }
    }

    fun editorSaveCutingAudio(context: Context, audioFile: AudioFile) {
        if (getViewStateMutable().getLastState() == ViewStateScreen.CUTTING_EDITOR_SCREEN) {
            getViewStateMutable().pushViewState(ViewStateScreen.RESULT_SCREEN)
            ResultActivity.startActivity(context, audioFile.file.absolutePath, "", ResultActivity.CUT)
        }
    }
}