package com.example.audiocutter.base.viewstate

import android.content.Context
import android.content.Intent
import androidx.navigation.fragment.findNavController
import com.example.a0025antivirusapplockclean.base.viewstate.ViewStateMutable
import com.example.a0025antivirusapplockclean.base.viewstate.ViewStateScreen
import com.example.audiocutter.activities.MainActivity
import com.example.audiocutter.base.BaseFragment
import com.example.audiocutter.functions.audiochooser.objects.AudioCutterView
import com.example.audiocutter.functions.audiochooser.screens.MergePreviewScreenDirections
import com.example.audiocutter.functions.editor.screen.CuttingEditorScreenDirections
import com.example.audiocutter.functions.editor.screen.MixingScreenDirections
import com.example.audiocutter.functions.resultscreen.screens.ResultScreen
import com.example.audiocutter.functions.resultscreen.screens.ResultScreenDirections
import com.example.audiocutter.objects.AudioFile
import com.example.core.core.AudioCutConfig
import com.example.core.core.AudioMergingConfig
import com.example.core.core.AudioMixConfig

interface AudioEditorViewStateManager {
    fun getViewStateMutable(): ViewStateMutable
    fun editorSaveMixingAudio(baseFragment: BaseFragment, audioFile1: AudioFile, audioFile2: AudioFile, audioMixConfig: AudioMixConfig) {
        if (getViewStateMutable().getLastState() == ViewStateScreen.MIXING_EDITOR_SCREEN) {
            getViewStateMutable().pushViewState(ViewStateScreen.RESULT_SCREEN)
            val audioPathArray = arrayOf(audioFile1.file.absolutePath.toString(), audioFile2.file.absolutePath.toString())
            val action = MixingScreenDirections.goToMixingEditorResultScreen(ResultScreen.MIX, audioPathArray, audioMixConfig, null, null)
            baseFragment.findNavController().navigate(action)
        }
    }

    fun editorSaveCutingAudio(baseFragment: BaseFragment, audioFile: AudioFile, audioCutConfig: AudioCutConfig) {
        if (getViewStateMutable().getLastState() == ViewStateScreen.CUTTING_EDITOR_SCREEN) {
            getViewStateMutable().pushViewState(ViewStateScreen.RESULT_SCREEN)
            val audioPathArray = arrayOf(audioFile.file.absolutePath.toString())
            val action = CuttingEditorScreenDirections.goToCuttingEditorResultScreen(ResultScreen.CUT, audioPathArray, null, audioCutConfig, null)
            baseFragment.findNavController().navigate(action)
        }
    }

    fun editorSaveMergingAudio(baseFragment: BaseFragment, listPathAudio: List<AudioCutterView>, audioMergingConfig: AudioMergingConfig) {
        if (getViewStateMutable().getLastState() == ViewStateScreen.MERGING_EDITOR_SCREEN) {
            getViewStateMutable().pushViewState(ViewStateScreen.RESULT_SCREEN)
            val audioPathArray = ArrayList<String>()
            listPathAudio.forEach {
                audioPathArray.add(it.audioFile.file.absolutePath.toString())
            }
            val action = MergePreviewScreenDirections.goToMergingEditorResultScreen(ResultScreen.MER, audioPathArray.toTypedArray(), null, null, audioMergingConfig)
            baseFragment.findNavController().navigate(action)
        }
    }

    fun resultScreenGoToHome(baseFragment: BaseFragment){
        if (getViewStateMutable().getLastState() == ViewStateScreen.RESULT_SCREEN){
            getViewStateMutable().popViewStateTo(ViewStateScreen.HOME_SCREEN)
            val action = ResultScreenDirections.goToHomeScreen()
            baseFragment.findNavController().navigate(action)
        }
    }
}