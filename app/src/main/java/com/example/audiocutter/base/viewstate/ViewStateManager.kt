package com.example.a0025antivirusapplockclean.base.viewstate

import android.util.Log
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.NavController
import com.example.audiocutter.R
import com.example.audiocutter.base.viewstate.*
import com.example.audiocutter.functions.mystudio.Constance
import com.example.audiocutter.functions.mystudio.screens.MyAudioManagerScreen

interface ViewStateManager : MainScreenViewStateManager, ContactScreenViewStateManager, ChooserScreenViewStateManager, AudioEditorViewStateManager, MyStudioScreenViewStateManager, ResultScreenViewStatusManager, IntroductionViewStatusManager {
    fun goToMyStudioScreen(navController: NavController, typeAudio: Int, instanceFragment: Fragment) {
        if (getViewStateMutable().getLastState() != ViewStateScreen.MY_STUDIO_SCREEN) {
            Log.d("TAG", "goToMyStudioScreen: typeAudio : " + typeAudio)
            getViewStateMutable().pushViewState(ViewStateScreen.MY_STUDIO_SCREEN)
            val bundle = bundleOf(Constance.TYPE_AUDIO_TO_NOTIFICATION to typeAudio)
            navController.navigate(R.id.my_studio_screen, bundle)
        } else {
            instanceFragment as MyAudioManagerScreen
            instanceFragment.setTabLayoutPosition(typeAudio)
        }
    }

    fun initState(viewStateScreen: ViewStateScreen)
    fun onBackPressed(): Boolean
    fun onScreenFinished()
}