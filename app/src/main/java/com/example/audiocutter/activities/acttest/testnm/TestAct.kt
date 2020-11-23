package com.example.audiocutter.activities.acttest.testnm

import android.Manifest
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseActivity
import kotlinx.android.synthetic.main.act_test.*


class TestAct : BaseActivity() {


    @RequiresApi(Build.VERSION_CODES.M)
    override fun createView(savedInstanceState: Bundle?) {
        setContentView(R.layout.act_test)
        requestPermissions(arrayOf(Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_PHONE_STATE, Manifest.permission.PROCESS_OUTGOING_CALLS), 101)
        val broadcast = MyBroadcast()
        val intentFilter = IntentFilter()
        intentFilter.addAction("android.intent.action.PHONE_STATE")
        intentFilter.addAction("android.intent.action.NEW_OUTGOING_CALL")
        intentFilter.addAction("android.provider.Telephony.SMS_RECEIVED")
        intentFilter.addAction(Intent.ACTION_SCREEN_ON)
        registerReceiver(broadcast, intentFilter)


        bt_test.setOnClickListener {

            UtilsTest.startFlash(this, "01010101010101010101")

        }

    }


}

