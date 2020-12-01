package com.example.audiocutter.base.viewstate

import androidx.navigation.fragment.findNavController
import com.example.a0025antivirusapplockclean.base.viewstate.ViewStateMutable
import com.example.a0025antivirusapplockclean.base.viewstate.ViewStateScreen
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseFragment
import com.example.audiocutter.functions.introduction.screens.IntroductionInfoScreenDirections
import com.example.audiocutter.functions.introduction.screens.Splash2Screen
import com.example.audiocutter.functions.introduction.screens.Splash3Screen
import com.example.audiocutter.functions.introduction.screens.SplashScreenDirections

interface IntroductionViewStatusManager {
    fun getViewStateMutable(): ViewStateMutable
    fun introductionScreenToHomeScreen(baseFragment: BaseFragment) {
        if (getViewStateMutable().getLastState() == ViewStateScreen.INTRODUCTION_3) {
            getViewStateMutable().pushViewState(ViewStateScreen.HOME_SCREEN)
            val action = IntroductionInfoScreenDirections.introductionGoToMainScreen()
            baseFragment.findNavController().navigate(action)
        }
    }
    fun introductionGoToIntroduction2(baseFragment: BaseFragment) {
        if (getViewStateMutable().getLastState() == ViewStateScreen.INTRODUCTION_1) {
            getViewStateMutable().pushViewState(ViewStateScreen.INTRODUCTION_2)
            val transaction = baseFragment.childFragmentManager.beginTransaction()
            transaction.replace(R.id.fm_splash, Splash2Screen())
            transaction.addToBackStack(null)
            transaction.commit()
        }
    }
    fun introductionGoToIntroduction3(baseFragment: BaseFragment) {
        if (getViewStateMutable().getLastState() == ViewStateScreen.INTRODUCTION_2) {
            getViewStateMutable().pushViewState(ViewStateScreen.INTRODUCTION_3)
            val transaction = baseFragment.childFragmentManager.beginTransaction()
            transaction.replace(R.id.fm_splash, Splash3Screen())
            transaction.addToBackStack(null)
            transaction.commit()
        }
    }

    fun splashScreenStartToIntroduction(baseFragment: BaseFragment) {
        if (getViewStateMutable().getLastState() == ViewStateScreen.SPLASH) {
            getViewStateMutable().pushViewState(ViewStateScreen.INTRODUCTION_1)
            val action = SplashScreenDirections.splashScreenGoToIntroductionInfo()
            baseFragment.findNavController().navigate(action)
        }
    }
}