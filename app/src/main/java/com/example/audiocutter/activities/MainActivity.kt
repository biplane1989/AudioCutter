package com.example.audiocutter.activities

import android.os.Bundle
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseActivity

class MainActivity : BaseActivity() {
    override fun createView(savedInstanceState: Bundle?) {
        setContentView(R.layout.main_screen)
    }
}