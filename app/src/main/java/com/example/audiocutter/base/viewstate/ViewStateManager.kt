package com.example.a0025antivirusapplockclean.base.viewstate

import com.example.audiocutter.base.viewstate.ContactScreenViewState
import com.example.audiocutter.base.viewstate.MainScreenViewState

interface ViewStateManager : MainScreenViewState, ContactScreenViewState {
    fun initState(viewStateScreen: ViewStateScreen)
    fun onBackPressed()
}