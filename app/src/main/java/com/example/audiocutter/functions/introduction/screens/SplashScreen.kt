package com.example.audiocutter.functions.introduction.screens

import android.R
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View

import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.whenResumed
import com.example.audiocutter.base.BaseFragment
import kotlinx.coroutines.delay


class SplashScreen : BaseFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(com.example.audiocutter.R.layout.introduction_screen, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        runOnUI {
            delay(2500)
            lifecycleScope.launchWhenResumed {
                viewStateManager.splashScreenStartToIntroduction(this@SplashScreen)
            }
        }
    }
}