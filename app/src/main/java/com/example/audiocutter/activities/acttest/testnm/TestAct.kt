package com.example.audiocutter.activities.acttest.testnm

import android.app.AlertDialog
import android.os.Bundle
import android.widget.Toast
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseActivity
import com.example.audiocutter.functions.settings.screens.SettingScreens
import kotlinx.android.synthetic.main.act_test.*
import java.io.File


class TestAct : BaseActivity() {
    val path = "/storage/emulated/0/AudioCutter/mixer/aloha - Copy (4).mp3"
    val file = File(path)
    private lateinit var frg: SettingScreens


    override fun createView(savedInstanceState: Bundle?) {
        setContentView(R.layout.act_test)
//        frg = SettingScreens()
//        supportFragmentManager.beginTransaction().replace(R.id.ln_main, frg).commit()
        bt_test.setOnClickListener {
            val dialog = AlertDialog.Builder(this).create()
            dialog.setTitle("hello")
            dialog.setMessage("cai gi the ban oi")
            dialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK") { _, _ -> dialog.dismiss() }

            dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel") { _, _ ->
                Toast.makeText(this, "sao the ban oi", Toast.LENGTH_SHORT).show()
            }

            dialog.show()
        }

    }


}



