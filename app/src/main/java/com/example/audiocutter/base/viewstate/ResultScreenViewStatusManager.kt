package com.example.audiocutter.base.viewstate

import androidx.navigation.fragment.findNavController
import com.example.a0025antivirusapplockclean.base.viewstate.ViewStateMutable
import com.example.a0025antivirusapplockclean.base.viewstate.ViewStateScreen
import com.example.audiocutter.base.BaseFragment
import com.example.audiocutter.functions.mystudio.screens.MyAudioManagerScreenDirections
import com.example.audiocutter.functions.resultscreen.screens.ResultScreenDirections

interface ResultScreenViewStatusManager {

    fun getViewStateMutable(): ViewStateMutable

    fun resultScreenSetContactItemClicked(baseFragment: BaseFragment, filePath: String) {
        if (getViewStateMutable().getLastState() == ViewStateScreen.RESULT_SCREEN) {
            getViewStateMutable().pushViewState(ViewStateScreen.SET_CONTACT_SCREEN)

            val action = ResultScreenDirections.resultGoToSetContactScreen(filePath)
            baseFragment.findNavController().navigate(action)
        }
    }
}