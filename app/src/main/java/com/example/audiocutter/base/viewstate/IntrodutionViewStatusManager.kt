package com.example.audiocutter.base.viewstate

import androidx.navigation.fragment.findNavController
import com.example.a0025antivirusapplockclean.base.viewstate.ViewStateMutable
import com.example.a0025antivirusapplockclean.base.viewstate.ViewStateScreen
import com.example.audiocutter.base.BaseFragment
import com.example.audiocutter.functions.contacts.screens.ListContactScreenDirections
import com.example.audiocutter.functions.introduction.screens.IntroductionInfoScreenDirections

interface IntrodutionViewStatusManager {
    fun getViewStateMutable(): ViewStateMutable
    fun introductionScreenStart(baseFragment: BaseFragment) {
        if (getViewStateMutable().getLastState() == ViewStateScreen.INTRODUCTION) {
            getViewStateMutable().pushViewState(ViewStateScreen.HOME_SCREEN)
            val action = IntroductionInfoScreenDirections.introductionGoToMainScreen()
            baseFragment.findNavController().navigate(action)
        }
    }
}