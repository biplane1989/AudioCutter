package com.example.audiocutter.activities.acttest.testnm

import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseActivity
import com.example.audiocutter.functions.flashcall.sreens.AppChooserScreen
import com.example.audiocutter.functions.flashcall.sreens.FlashCallScreen


class TestAct : BaseActivity(), FlashCallScreen.FlashCallBack, AppChooserScreen.SetTimeCallBack {
    lateinit var frg: AppChooserScreen
    lateinit var frgCall: FlashCallScreen

    @RequiresApi(Build.VERSION_CODES.M)
    override fun createView(savedInstanceState: Bundle?) {

        setContentView(R.layout.act_test)
        frg = AppChooserScreen()
        frgCall = FlashCallScreen()
        frgCall.setOnCallBack(this)
        frg.setOnCallBack(this)
        supportFragmentManager.beginTransaction().replace(R.id.ln_main, frgCall).commit()

    }

    override fun showFrgs() {
        supportFragmentManager.beginTransaction().replace(R.id.ln_main, frg).commit()
    }

    override fun backs() {
        supportFragmentManager.beginTransaction().replace(R.id.ln_main, frgCall).commit()
    }


}

