package com.example.audiocutter.activities.acttest.testnm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi

class MyBroadcast : BroadcastReceiver() {
    val TAG = "Hello"

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("TAG", "onReceive: ${intent.action.toString()}")
        when {
            intent.action.equals("android.intent.action.PHONE_STATE") -> {
                val telephony = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
                telephony.listen(object : PhoneStateListener() {
                    @RequiresApi(Build.VERSION_CODES.M)
                    override fun onCallStateChanged(state: Int, incomingNumber: String) {
                        super.onCallStateChanged(state, incomingNumber)
                        Log.d("TAG", "onReceive: state $state")
                        when (state) {
                            1 -> {
                                UtilsTest.startFlash(context, "010101010101010101010101010101010101010101")
                            }
                            0 -> {
                                UtilsTest.turnOff()
                            }
                            2 -> {
                                UtilsTest.turnOff()
                            }
                        }

                    }
                }, PhoneStateListener.LISTEN_CALL_STATE)
            }

            intent.action.equals("android.intent.action.NEW_OUTGOING_CALL") -> {
                Log.d("TAG", "onReceive:  new going call ")
            }


            intent.action.equals(Intent.ACTION_SCREEN_ON) -> {
                Log.d("TAG", "onReceive: wwellllll")
                Toast.makeText(context, "welCome", Toast.LENGTH_SHORT).show()
            }
        }
    }

}