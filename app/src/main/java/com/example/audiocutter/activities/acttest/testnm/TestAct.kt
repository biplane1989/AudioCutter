package com.example.audiocutter.activities.acttest.testnm

import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseActivity
import com.example.audiocutter.functions.flashcall.sreens.AppChooserScreen
import com.example.audiocutter.functions.flashcall.sreens.FlashCallSceen


class TestAct : BaseActivity() {
    lateinit var frgCall: AppChooserScreen

    @RequiresApi(Build.VERSION_CODES.M)
    override fun createView(savedInstanceState: Bundle?) {

        setContentView(R.layout.act_test)
        frgCall = AppChooserScreen()

        supportFragmentManager.beginTransaction().replace(R.id.ln_main, frgCall).commit()

    }
}

