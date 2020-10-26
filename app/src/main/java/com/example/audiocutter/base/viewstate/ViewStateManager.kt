package com.example.a0025antivirusapplockclean.base.viewstate

import com.example.audiocutter.base.viewstate.AudioEditorViewStateManager
import com.example.audiocutter.base.viewstate.ChooserScreenViewStateManager
import com.example.audiocutter.base.viewstate.ContactScreenViewStateManager
import com.example.audiocutter.base.viewstate.MainScreenViewStateManager

interface ViewStateManager : MainScreenViewStateManager, ContactScreenViewStateManager, ChooserScreenViewStateManager, AudioEditorViewStateManager {
    fun initState(viewStateScreen: ViewStateScreen)
    fun onBackPressed(): Boolean
    fun onScreenFinished()
}