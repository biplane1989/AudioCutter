package com.example.audiocutter.functions.introduction.screens

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.audiocutter.base.BaseFragment
import com.example.audiocutter.databinding.IntroductionInfoScreenBinding
import com.example.audiocutter.databinding.MyStudioScreenBinding

class IntroductionInfoScreen : BaseFragment(), View.OnClickListener {

    private lateinit var binding: IntroductionInfoScreenBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(com.example.audiocutter.R.layout.introduction_info_screen, container, false)
    }

    override fun onClick(view: View?) {
        when (view) {
            binding.btnStart -> {
                Log.d("giangtd", "onClick: ")
            }
        }
    }

}