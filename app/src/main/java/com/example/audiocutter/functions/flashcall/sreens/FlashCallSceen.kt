package com.example.audiocutter.functions.flashcall.sreens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.databinding.DataBindingUtil
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseFragment
import com.example.audiocutter.databinding.FlashCallScreenBinding

class FlashCallSceen : BaseFragment(), CompoundButton.OnCheckedChangeListener {
    private lateinit var binding: FlashCallScreenBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.flash_call_screen, container, false)
        initViews()
        return binding.root
    }

    private fun initViews() {
        binding.swFlashCallMode.setOnCheckedChangeListener(this)
    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        if(isChecked){
            binding.lnFlashOffMode.visibility=View.INVISIBLE
            binding.lnFlashOnMode.visibility=View.VISIBLE
        }else{
            binding.lnFlashOffMode.visibility=View.VISIBLE
            binding.lnFlashOnMode.visibility=View.INVISIBLE
        }
    }

}
