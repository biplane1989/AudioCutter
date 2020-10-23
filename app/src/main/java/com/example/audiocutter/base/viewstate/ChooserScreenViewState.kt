package com.example.audiocutter.base.viewstate

import androidx.navigation.fragment.findNavController
import com.example.a0025antivirusapplockclean.base.viewstate.ViewStateMutable
import com.example.a0025antivirusapplockclean.base.viewstate.ViewStateScreen
import com.example.audiocutter.base.BaseFragment
import com.example.audiocutter.functions.audiochooser.screens.MixChooserScreenDirections
import com.example.audiocutter.objects.AudioFile

interface ChooserScreenViewState {
    fun getViewStateMutable(): ViewStateMutable
    fun mixingOnSelected(baseFragment: BaseFragment, audioFile1: AudioFile, audioFile2: AudioFile) {
        if(getViewStateMutable().getLastState() == ViewStateScreen.MP3_MIX_CHOOSER_SCREEN){
            getViewStateMutable().pushViewState(ViewStateScreen.MP3_MIXING_SCREEN)
            val action = MixChooserScreenDirections.goToMixingScreen(
                audioFile1.file.absolutePath,
                audioFile2.file.absolutePath
            )
            baseFragment.findNavController().navigate(action)
        }

    }
}