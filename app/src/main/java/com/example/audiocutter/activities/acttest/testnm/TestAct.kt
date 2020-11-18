package com.example.audiocutter.activities.acttest.testnm

import android.os.Bundle
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseActivity
import com.example.audiocutter.functions.settings.screens.SettingScreens
import java.io.File


class TestAct : BaseActivity() {
    val path = "/storage/emulated/0/AudioCutter/mixer/aloha - Copy (4).mp3"
    val file = File(path)
    private lateinit var frg: SettingScreens


    override fun createView(savedInstanceState: Bundle?) {
        setContentView(R.layout.act_test)
        frg = SettingScreens()
        supportFragmentManager.beginTransaction().replace(R.id.ln_main, frg).commit()
    }


}



