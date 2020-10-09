package com.example.audiocutter.activities

import android.os.Bundle
import android.os.PersistableBundle
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentTransaction
import androidx.navigation.fragment.NavHostFragment
import com.example.a0025antivirusapplockclean.base.viewstate.ViewStateScreen
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseActivity
import com.example.audiocutter.databinding.ActivityMainBinding
import com.example.audiocutter.functions.MainScreen
import com.example.audiocutter.functions.mystudio.OutputAudioManagerScreen

class MainActivity : BaseActivity() {
    lateinit var binding: ActivityMainBinding
    override fun createView(savedInstanceState: Bundle?) {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
    }

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        viewStateManager.initState(ViewStateScreen.HOME_SCREEN)
    }

}