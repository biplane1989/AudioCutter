package com.example.audiocutter.functions.introduction.screens

import android.graphics.drawable.Animatable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseFragment
import com.example.audiocutter.databinding.IntroductionSplash1ScreenBinding
import com.example.audiocutter.databinding.IntroductionSplash2ScreenBinding

class Splash2Screen : BaseFragment() {
    private lateinit var binding: IntroductionSplash2ScreenBinding
    private lateinit var personAnimation: Animatable
    private var isAnimationDisplayed = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.introduction_splash_2_screen,
            container,
            false
        )
        initView()
        return binding.root
    }

    private fun initView() {
        personAnimation = binding.ivSence.drawable as Animatable
    }

    override fun onStart() {
        super.onStart()

        if (!isAnimationDisplayed) {
            personAnimation.start()
        }
    }

    override fun onStop() {
        super.onStop()
        if (personAnimation.isRunning) {
            personAnimation.stop()
        }
    }
}