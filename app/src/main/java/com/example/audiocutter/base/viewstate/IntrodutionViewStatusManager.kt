package com.example.audiocutter.base.viewstate

import androidx.navigation.fragment.findNavController
import com.example.a0025antivirusapplockclean.base.viewstate.ViewStateMutable
import com.example.a0025antivirusapplockclean.base.viewstate.ViewStateScreen
import com.example.audiocutter.base.BaseFragment
import com.example.audiocutter.functions.contacts.screens.ListContactScreenDirections
import com.example.audiocutter.functions.introduction.screens.IntroductionInfoScreenDirections
import com.example.audiocutter.functions.introduction.screens.SplashScreenDirections

interface IntrodutionViewStatusManager {
    fun getViewStateMutable(): ViewStateMutable
    fun introductionScreenToHomeScreen(baseFragment: BaseFragment) {
        if (getViewStateMutable().getLastState() == ViewStateScreen.INTRODUCTION) {
            getViewStateMutable().pushViewState(ViewStateScreen.HOME_SCREEN)
            val action = IntroductionInfoScreenDirections.introductionGoToMainScreen()
            baseFragment.findNavController().navigate(action)
        }
    }

    fun splashScreenStartToIntroduction(baseFragment: BaseFragment) {
        if (getViewStateMutable().getLastState() == ViewStateScreen.SPLASH) {
            getViewStateMutable().pushViewState(ViewStateScreen.INTRODUCTION)
            val action = SplashScreenDirections.splashScreenGoToIntroductionInfo()
            baseFragment.findNavController().navigate(action)
        }
    }
}