package com.example.audiocutter.base.viewstate

import androidx.navigation.fragment.findNavController
import com.example.a0025antivirusapplockclean.base.viewstate.ViewStateMutable
import com.example.a0025antivirusapplockclean.base.viewstate.ViewStateScreen
import com.example.audiocutter.base.BaseFragment
import com.example.audiocutter.functions.audiochooser.objects.AudioCutterView
import com.example.audiocutter.functions.audiochooser.screens.CutChooserScreenDirections

interface AudioChooserViewState {
    fun getViewStateMutable(): ViewStateMutable
    fun onCuttingItemClicked(baseFragment: BaseFragment, itemAudio: AudioCutterView) {
        if (getViewStateMutable().getLastState() == ViewStateScreen.MP3_CUT_CHOOSER_SCREEN) {
            getViewStateMutable().pushViewState(ViewStateScreen.CUTTING_EDITOR_SCREEN)
            val action =
                CutChooserScreenDirections.goToCuttingEditorScreen(itemAudio.audioFile.file.absolutePath)
            baseFragment.findNavController().navigate(action)
        }
    }
}