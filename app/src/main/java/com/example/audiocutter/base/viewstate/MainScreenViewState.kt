package com.example.audiocutter.base.viewstate

import androidx.navigation.fragment.findNavController
import com.example.a0025antivirusapplockclean.base.viewstate.ViewStateMutable
import com.example.a0025antivirusapplockclean.base.viewstate.ViewStateScreen
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseFragment

interface MainScreenViewState {
    fun getViewStateMutable(): ViewStateMutable
    fun mainScreenOnContactItemClicked(baseFragment: BaseFragment) {
        if (getViewStateMutable().getLastState() == ViewStateScreen.HOME_SCREEN) {
            getViewStateMutable().pushViewState(ViewStateScreen.LIST_CONTACT_SCREEN)
            baseFragment.findNavController().navigate(R.id.go_to_list_contact_screen)
        }
    }

    fun mainScreenOnMyAudioItemClicked(baseFragment: BaseFragment) {
        if (getViewStateMutable().getLastState() == ViewStateScreen.HOME_SCREEN) {
            getViewStateMutable().pushViewState(ViewStateScreen.MY_AUDIO_SCREEN)
            baseFragment.findNavController().navigate(R.id.go_to_my_audio_screen)
        }
    }


}