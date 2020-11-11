package com.example.audiocutter.base.viewstate

import androidx.navigation.fragment.findNavController
import com.example.a0025antivirusapplockclean.base.viewstate.ViewStateMutable
import com.example.a0025antivirusapplockclean.base.viewstate.ViewStateScreen
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseFragment
import com.example.audiocutter.functions.audiochooser.objects.AudioCutterView
import com.example.audiocutter.functions.audiochooser.screens.CutChooserScreenDirections
import com.example.audiocutter.functions.audiochooser.screens.MergePreviewScreenDirections
import com.example.audiocutter.functions.mystudio.screens.MyAudioManagerScreenDirections
import com.example.audiocutter.functions.resultscreen.screens.ResultScreen

interface MyStudioScreenViewStateManager {
    fun getViewStateMutable(): ViewStateMutable
    fun myStudioSetContactItemClicked(baseFragment: BaseFragment, filePath: String) {
        if (getViewStateMutable().getLastState() == ViewStateScreen.MY_STUDIO_SCREEN) {
            getViewStateMutable().pushViewState(ViewStateScreen.SET_CONTACT_SCREEN)

            val action = MyAudioManagerScreenDirections.goToMyStudioSetContactScreen(filePath)
            baseFragment.findNavController().navigate(action)
        }
    }

    fun myStudioCuttingItemClicked(baseFragment: BaseFragment, filePath: String) {
        if (getViewStateMutable().getLastState() == ViewStateScreen.MY_STUDIO_SCREEN) {
            getViewStateMutable().pushViewState(ViewStateScreen.CUTTING_EDITOR_SCREEN)
            val action = MyAudioManagerScreenDirections.myStudioGoToCuttingEditorScreen(filePath)
            baseFragment.findNavController().navigate(action)
        }
    }
}