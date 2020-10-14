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

    fun mainScreenOnMp3CutItemClicked(baseFragment: BaseFragment) {
        if (getViewStateMutable().getLastState() == ViewStateScreen.HOME_SCREEN) {
            getViewStateMutable().pushViewState(ViewStateScreen.MP3_CUT_CHOOSER_SCREEN)
            baseFragment.findNavController().navigate(R.id.go_to_cut_chooser_screen)
        }
    }

    fun mainScreenOnMp3MergeItemClicked(baseFragment: BaseFragment) {
        if (getViewStateMutable().getLastState() == ViewStateScreen.HOME_SCREEN) {
            getViewStateMutable().pushViewState(ViewStateScreen.MP3_MERGE_CHOOSER_SCREEN)
            baseFragment.findNavController().navigate(R.id.go_to_merge_chooser_screen)
        }
    }

    fun mainScreenOnMp3MixItemClicked(baseFragment: BaseFragment) {
        if (getViewStateMutable().getLastState() == ViewStateScreen.HOME_SCREEN) {
            getViewStateMutable().pushViewState(ViewStateScreen.MP3_MIX_CHOOSER_SCREEN)
            baseFragment.findNavController().navigate(R.id.go_to_mix_chooser_screen)
        }
    }


    fun mainScreenOnMyAudioItemClicked(baseFragment: BaseFragment) {
        if (getViewStateMutable().getLastState() == ViewStateScreen.HOME_SCREEN) {
            getViewStateMutable().pushViewState(ViewStateScreen.MY_AUDIO_SCREEN)
            baseFragment.findNavController().navigate(R.id.go_to_my_audio_screen)
        }
    }


}