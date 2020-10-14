package com.example.audiocutter.activities.acttest

import android.os.Bundle
import android.util.Log
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseActivity
import com.example.audiocutter.util.Utils
import kotlinx.android.synthetic.main.act_test.*

class TestAct : BaseActivity() {

//    var frg = MergePreviewScreen()


    override fun createView(savedInstanceState: Bundle?) {
        setContentView(R.layout.act_test)
        initViews()
    }


    private fun initViews() {
        bt_c.setOnClickListener {
            val a =Utils.convertDp2Px(edt_text.text.toString().toInt(), this)
            Log.d("TAG", "convertDP: $a")
        }


//        supportFragmentManager.beginTransaction().add(R.id.ln_main, frg).commit()


    }



}

