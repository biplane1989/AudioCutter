package com.example.audiocutter.activities

import android.os.Bundle
import android.os.PersistableBundle
import androidx.databinding.DataBindingUtil
import com.example.a0025antivirusapplockclean.base.viewstate.ViewStateScreen
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseActivity
import com.example.audiocutter.databinding.ActivityMainBinding

class MainActivity : BaseActivity() {
    lateinit var binding: ActivityMainBinding

    override fun onPostCreate() {
        super.onPostCreate()
        viewStateManager.initState(ViewStateScreen.HOME_SCREEN)
    }
    override fun createView(savedInstanceState: Bundle?) {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
    }

}