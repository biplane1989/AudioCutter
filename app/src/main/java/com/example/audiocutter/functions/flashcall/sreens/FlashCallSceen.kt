package com.example.audiocutter.functions.flashcall.sreens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseFragment
import com.example.audiocutter.databinding.FlashCallScreenBinding

class FlashCallSceen : BaseFragment() {
    private lateinit var binding: FlashCallScreenBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.flash_call_screen, container, false)
        initViews()
        return binding.root
    }

    private fun initViews() {
    }

}
