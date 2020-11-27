package com.example.audiocutter.functions.introduction.screens

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseFragment
import com.example.audiocutter.databinding.IntroductionInfoScreenBinding
import com.example.audiocutter.databinding.MyStudioScreenBinding
import kotlinx.android.synthetic.main.introduction_info_screen.*

class IntroductionInfoScreen : BaseFragment(), View.OnClickListener {

    private lateinit var binding: IntroductionInfoScreenBinding
    private lateinit var transaction: FragmentTransaction
    val splash1Screen = Splash1Screen()
    val splash2Screen = Splash2Screen()
    val splash3Screen = Splash3Screen()
    var index = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.introduction_info_screen, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnStart.setOnClickListener(this)
    }

    override fun onClick(view: View?) {
        when (view) {
            binding.btnStart -> {
                index++
                when (index) {
                    1 -> {
                        transaction = childFragmentManager.beginTransaction()
                        transaction.replace(R.id.fm_splash, splash2Screen)
                        transaction.addToBackStack(null)
                        transaction.commit()
                    }
                    2 -> {
                        transaction = childFragmentManager.beginTransaction()
                        transaction.replace(R.id.fm_splash, splash3Screen)
                        transaction.addToBackStack(null)
                        transaction.commit()
                    }
                    else -> {
                        viewStateManager.introductionScreenToHomeScreen(this)
                    }
                }
            }
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        transaction = childFragmentManager.beginTransaction()
        transaction.replace(R.id.fm_splash, splash1Screen)
        transaction.addToBackStack(null)
        transaction.commit()
    }
}