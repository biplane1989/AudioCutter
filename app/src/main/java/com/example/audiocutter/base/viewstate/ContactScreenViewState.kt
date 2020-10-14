package com.example.audiocutter.base.viewstate

import androidx.navigation.fragment.findNavController
import com.example.a0025antivirusapplockclean.base.viewstate.ViewStateMutable
import com.example.a0025antivirusapplockclean.base.viewstate.ViewStateScreen
import com.example.audiocutter.base.BaseFragment
import com.example.audiocutter.functions.contacts.contacts.ListContactScreenDirections

interface ContactScreenViewState {
    fun getViewStateMutable(): ViewStateMutable
    fun contactScreenOnItemClicked(baseFragment: BaseFragment, phoneNumber: String, fileName: String) {
        if (getViewStateMutable().getLastState() == ViewStateScreen.LIST_CONTACT_SCREEN) {
            getViewStateMutable().pushViewState(ViewStateScreen.LIST_CONTACT_CHOOSER_SCREEN)
            val action = ListContactScreenDirections.goToListSelectAudioScreen(phoneNumber, fileName)
            baseFragment.findNavController().navigate(action)
        }
    }
}