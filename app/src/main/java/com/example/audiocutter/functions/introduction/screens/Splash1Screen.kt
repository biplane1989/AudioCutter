package com.example.audiocutter.functions.introduction.screens

import android.animation.ValueAnimator
import android.graphics.drawable.Animatable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.whenResumed
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseFragment
import com.example.audiocutter.databinding.IntroductionSplash1ScreenBinding

class Splash1Screen : BaseFragment() {
    private lateinit var binding: IntroductionSplash1ScreenBinding
    private lateinit var personAnimation: Animatable
    private var isAnimationDisplayed = false
    private var valueAnimator: AlphaAnimation? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.introduction_splash_1_screen,
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
        runOnUI {
            viewLifecycleOwner.whenResumed {
                if (!isAnimationDisplayed) {
                    valueAnimator = AlphaAnimation(0.2f, 1.0f)
                    valueAnimator?.setDuration(1000)
                    valueAnimator?.setFillAfter(true)
                    binding.ivSence.startAnimation(valueAnimator)
                    personAnimation.start()
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        if (personAnimation.isRunning) {
            personAnimation.stop()
        }
        valueAnimator?.cancel()
    }

}