package com.example.audiocutter.functions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseFragment

class SetContactRingtonScreen : BaseFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.set_contact_rington_screen, container, false)
    }
}