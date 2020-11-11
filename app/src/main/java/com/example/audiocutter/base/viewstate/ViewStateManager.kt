package com.example.a0025antivirusapplockclean.base.viewstate

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.example.audiocutter.R
import com.example.audiocutter.base.viewstate.*
import com.example.audiocutter.functions.mystudio.Constance
import com.example.audiocutter.functions.mystudio.screens.MyStudioScreen

interface ViewStateManager : MainScreenViewStateManager, ContactScreenViewStateManager, ChooserScreenViewStateManager, AudioEditorViewStateManager, MyStudioScreenViewStateManager, ResultScreenViewStatusManager {
    fun goToMyStudioScreen(navController: NavController, typeAudio: Int) {
        if (getViewStateMutable().getLastState() != ViewStateScreen.MY_STUDIO_SCREEN) {
            getViewStateMutable().pushViewState(ViewStateScreen.MY_STUDIO_SCREEN)
//            val bundle = MyStudioScreen.buildArgs(typeAudio)
            val bundle = bundleOf(Constance.TYPE_AUDIO_TO_NOTIFICATION to typeAudio)
            navController.navigate(R.id.my_studio_screen, bundle)
        }
    }

    fun initState(viewStateScreen: ViewStateScreen)
    fun onBackPressed(): Boolean
    fun onScreenFinished()
}