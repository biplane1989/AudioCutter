package com.example.audiocutter.functions.introduction.screens

import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseFragment
import com.example.audiocutter.core.manager.ManagerFactory
import com.example.audiocutter.databinding.IntroductionInfoScreenBinding
import com.example.audiocutter.databinding.MyStudioScreenBinding
import com.example.audiocutter.functions.mystudio.dialog.CancelDialog
import com.example.audiocutter.util.PreferencesHelper
import kotlinx.android.synthetic.main.introduction_info_screen.*

class IntroductionInfoScreen : BaseFragment(), View.OnClickListener {

    private lateinit var binding: IntroductionInfoScreenBinding
    private lateinit var transaction: FragmentTransaction
    val splash1Screen = Splash1Screen()
    var index = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.introduction_info_screen, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnStart.setOnClickListener(this)
    }

    val TAG = "00111"

    override fun onClick(view: View?) {
        when (view) {
            binding.btnStart -> {
                index++
                Log.d(TAG, "onClick: index : " + index)
                when (index) {
                    1 -> {
                        viewStateManager.introductionGoToIntroduction2(this)
                    }
                    2 -> {
                        binding.tvStart.setText(R.string.introduction_info_screen_start)
                        viewStateManager.introductionGoToIntroduction3(this)
                    }
                    3 -> {
                        viewStateManager.introductionScreenToHomeScreen(this)
                        PreferencesHelper.setFirstTimeToUsedApp(true)
                        Log.d(TAG, "onClick:  Start")
                    }
                }
            }
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        transaction = childFragmentManager.beginTransaction()
        transaction.replace(R.id.fm_splash, splash1Screen)
        transaction.commit()
    }

    override fun onResume() {       // xu ly back button
        super.onResume()
        requireView().isFocusableInTouchMode = true
        requireView().requestFocus()
        requireView().setOnKeyListener { v, keyCode, event ->
            if (event.action == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {

                index--
                when (index) {
                    0 -> {
                        binding.tvStart.setText(R.string.introduction_info_screen_next)
                    }
                    1 -> {
                        binding.tvStart.setText(R.string.introduction_info_screen_next)
                    }
                }
                Log.d(TAG, "onClick: index : " + index)
                requireActivity().onBackPressed()

                // handle back button
                true
            } else false
        }
    }
}