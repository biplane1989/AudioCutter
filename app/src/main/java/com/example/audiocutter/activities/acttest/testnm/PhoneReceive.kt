package com.example.audiocutter.activities.acttest.testnm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.util.Log
import androidx.annotation.RequiresApi
import java.util.*


 class PhonecallReceiver : BroadcastReceiver() {


    companion object {
        //The receiver will be recreated whenever android feels like it.  We need a static variable to remember data between instantiations
        private var lastState = TelephonyManager.CALL_STATE_IDLE
        private var callStartTime: Date? = null
        private var isIncoming = false
        private var savedNumber: String? = null
    }

    override fun onReceive(context: Context, intent: Intent) {

        //We listen to two intents.  The new outgoing call only tells us of an outgoing call.  We use it to get the number.
        when (intent.action) {
            "android.intent.action.NEW_OUTGOING_CALL" -> {
                savedNumber = intent.extras!!.getString("android.intent.extra.PHONE_NUMBER")
            }
            "android.intent.action.PHONE_STATE" -> {

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
//                                UtilsTest.turnOff()
                            }
                            2 -> {
//                                UtilsTest.turnOff()
                            }
                        }

                    }
                }, PhoneStateListener.LISTEN_CALL_STATE)

            }
            else -> {
                val stateStr = intent.extras!!.getString(TelephonyManager.EXTRA_STATE)
                val number = intent.extras!!.getString(TelephonyManager.EXTRA_INCOMING_NUMBER)
                var state = 0
                when (stateStr) {
                    TelephonyManager.EXTRA_STATE_IDLE -> {
                        state = TelephonyManager.CALL_STATE_IDLE
                    }
                    TelephonyManager.EXTRA_STATE_OFFHOOK -> {
                        state = TelephonyManager.CALL_STATE_OFFHOOK
                    }
                    TelephonyManager.EXTRA_STATE_RINGING -> {
                        state = TelephonyManager.CALL_STATE_RINGING
                    }
                }
                onCallStateChanged(context, state, number)
            }
        }
    }


    fun onCallStateChanged(context: Context?, state: Int, number: String?) {
        if (lastState == state) {
            //No change, debounce extras
            return
        }
        when (state) {
            TelephonyManager.CALL_STATE_RINGING -> {
                isIncoming = true
                callStartTime = Date()
                savedNumber = number
//                onIncomingCallStarted(context, number!!, callStartTime!!)
            }
            TelephonyManager.CALL_STATE_OFFHOOK ->                 //Transition of ringing->offhook are pickups of incoming calls.  Nothing done on them
                if (lastState != TelephonyManager.CALL_STATE_RINGING) {
                    isIncoming = false
                    callStartTime = Date()
//                    onOutgoingCallStarted(context, savedNumber!!, callStartTime!!)
                }
            TelephonyManager.CALL_STATE_IDLE ->                 //Went to idle-  this is the end of a call.  What type depends on previous state(s)
                if (lastState == TelephonyManager.CALL_STATE_RINGING) {
                    //Ring but no pickup-  a miss
//                    onMissedCall(context, savedNumber!!, callStartTime!!)
                } else if (isIncoming) {
//                    onIncomingCallEnded(context, savedNumber!!, callStartTime!!, Date())
                } else {
//                    onOutgoingCallEnded(context, savedNumber!!, callStartTime!!, Date())
                }
        }
        lastState = state
    }


}