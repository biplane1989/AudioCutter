package com.example.audiocutter.base.viewstate

import androidx.navigation.fragment.findNavController
import com.example.a0025antivirusapplockclean.base.viewstate.ViewStateMutable
import com.example.a0025antivirusapplockclean.base.viewstate.ViewStateScreen
import com.example.audiocutter.base.BaseFragment
import com.example.audiocutter.functions.audiochooser.objects.AudioCutterViewItem
import com.example.audiocutter.functions.audiochooser.screens.CutChooserScreenDirections
import com.example.audiocutter.functions.audiochooser.screens.MergeChooserScreenDirections
import com.example.audiocutter.functions.audiochooser.screens.MixChooserScreenDirections
import com.example.audiocutter.objects.AudioFile

interface ChooserScreenViewStateManager {
    fun getViewStateMutable(): ViewStateMutable
    fun mixingOnSelected(baseFragment: BaseFragment, audioFile1: AudioFile, audioFile2: AudioFile) {
        if (getViewStateMutable().getLastState() == ViewStateScreen.MP3_MIX_CHOOSER_SCREEN) {
            getViewStateMutable().pushViewState(ViewStateScreen.MIXING_EDITOR_SCREEN)
            val action = MixChooserScreenDirections.goToMixingScreen(audioFile1.file.absolutePath, audioFile2.file.absolutePath)
            baseFragment.findNavController().navigate(action)
        }
    }

    fun onCuttingItemClicked(baseFragment: BaseFragment, itemAudio: AudioCutterViewItem) {
        if (getViewStateMutable().getLastState() == ViewStateScreen.MP3_CUT_CHOOSER_SCREEN) {
            getViewStateMutable().pushViewState(ViewStateScreen.CUTTING_EDITOR_SCREEN)
            val action = CutChooserScreenDirections.goToCuttingEditorScreen(itemAudio.audioFile.file.absolutePath)
            baseFragment.findNavController().navigate(action)
        }
    }

    fun onMergingItemClicked(baseFragment: BaseFragment, listPathAudio: Array<String>) {
        if (getViewStateMutable().getLastState() == ViewStateScreen.MP3_MERGE_CHOOSER_SCREEN) {
            getViewStateMutable().pushViewState(ViewStateScreen.MERGING_EDITOR_SCREEN)
            val action = MergeChooserScreenDirections.goToPreviewScreen(listPathAudio)
            baseFragment.findNavController().navigate(action)
        }
    }

    fun onCutScreenSetRingtoneContact(baseFragment: BaseFragment, filePath: String) {
        if (getViewStateMutable().getLastState() == ViewStateScreen.MP3_CUT_CHOOSER_SCREEN) {
            getViewStateMutable().pushViewState(ViewStateScreen.SET_CONTACT_SCREEN)
            val action = CutChooserScreenDirections.cutChooserScreenGoToSetContactScreen(filePath)
            baseFragment.findNavController().navigate(action)
        }
    }
}