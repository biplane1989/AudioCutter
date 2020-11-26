package com.example.audiocutter.functions.introduction.screens

import android.R
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View

import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.example.audiocutter.base.BaseFragment


class IntroductionScreen : BaseFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(com.example.audiocutter.R.layout.introduction_screen, container, false)
    }
}