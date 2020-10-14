package com.example.audiocutter.activities.acttest

import android.os.Bundle
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseActivity
import com.example.audiocutter.functions.audiochooser.merge.screen.MergePreviewScreen

class TestAct : BaseActivity() {

    var frg = MergePreviewScreen()


    override fun createView(savedInstanceState: Bundle?) {
        setContentView(R.layout.act_test)
        initViews()

    }


    private fun initViews() {
        supportFragmentManager.beginTransaction().add(R.id.ln_main, frg).commit()


    }

}

