package com.example.audiocutter.core.flashcall

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.telephony.TelephonyManager
import androidx.annotation.RequiresApi
import java.util.*


abstract class PhoneCallReceiver : BroadcastReceiver() {
    companion object {
        private var lastState = TelephonyManager.CALL_STATE_IDLE
        private lateinit var callStartTime: Date
        private var isIncoming = false
        private var savedNumber: String? = null
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.getAction().equals("android.intent.action.NEW_OUTGOING_CALL")) {
            savedNumber = intent.getExtras()?.getString("android.intent.extra.PHONE_NUMBER")
        } else {
            val stateStr =
                intent.extras!!.getString(TelephonyManager.EXTRA_STATE)
            val number =
                intent.extras!!.getString(TelephonyManager.EXTRA_INCOMING_NUMBER)
            var state = 0
            if (stateStr == TelephonyManager.EXTRA_STATE_IDLE) {
                state = TelephonyManager.CALL_STATE_IDLE
            } else if (stateStr == TelephonyManager.EXTRA_STATE_OFFHOOK) {
                state = TelephonyManager.CALL_STATE_OFFHOOK
            } else if (stateStr == TelephonyManager.EXTRA_STATE_RINGING) {
                state = TelephonyManager.CALL_STATE_RINGING
            }

            onCallStateChanged(context, state, number);
        }
    }

    //Derived classes should override these to respond to specific events of interest
    protected abstract fun onIncomingCallStarted(
        ctx: Context,
        number: String?,
        start: Date
    )

    protected abstract fun onOutgoingCallStarted(
        ctx: Context,
        number: String?,
        start: Date
    )

    protected abstract fun onIncomingCallEnded(
        ctx: Context,
        number: String?,
        start: Date,
        end: Date
    )


    protected abstract fun onOutgoingCallEnded(
        ctx: Context,
        number: String?,
        start: Date,
        end: Date
    )

    protected abstract fun onMissedCall(
        ctx: Context,
        number: String?,
        start: Date
    )

    open fun onCallStateChanged(
        context: Context,
        state: Int,
        number: String?
    ) {

        if (lastState == state) {
            //No change, debounce extras
            return
        }
        when (state) {
            TelephonyManager.CALL_STATE_RINGING -> {
                isIncoming = true
                callStartTime = Date()
                savedNumber = number
                onIncomingCallStarted(context, number, callStartTime)
            }
            TelephonyManager.CALL_STATE_OFFHOOK ->   //Transition of ringing->offhook are pickups of incoming calls.  Nothing done on them
            {
                if (lastState != TelephonyManager.CALL_STATE_RINGING) {
                    isIncoming = false
                    callStartTime = Date()
                    onOutgoingCallStarted(context, savedNumber, callStartTime)
                }
            }

            TelephonyManager.CALL_STATE_IDLE ->                 //Went to idle-  this is the end of a call.  What type depends on previous state(s)
            {
                if (lastState == TelephonyManager.CALL_STATE_RINGING) {
                    //Ring but no pickup-  a miss
                    onMissedCall(context, savedNumber, callStartTime)
                } else if (isIncoming) {
                    onIncomingCallEnded(context, savedNumber, callStartTime, Date())
                } else {
                    onOutgoingCallEnded(context, savedNumber, callStartTime, Date())
                }
            }
        }
        lastState = state
    }
}

class CallReceiver : PhoneCallReceiver() {
    override fun onOutgoingCallStarted(ctx: Context, number: String?, start: Date) {
        if (RuleChecker.checkTurnFlashForComingCall()) {
            FlashCallSettingImpl.stopLightningSpeed()
        }
    }

    override fun onIncomingCallEnded(ctx: Context, number: String?, start: Date, end: Date) {
        if (RuleChecker.checkTurnFlashForComingCall()) {
            FlashCallSettingImpl.stopLightningSpeed()
        }
    }

    override fun onOutgoingCallEnded(ctx: Context, number: String?, start: Date, end: Date) {
        if (RuleChecker.checkTurnFlashForComingCall()) {
            FlashCallSettingImpl.stopLightningSpeed()
        }
    }

    override fun onMissedCall(ctx: Context, number: String?, start: Date) {
        if (RuleChecker.checkTurnFlashForComingCall()) {
            FlashCallSettingImpl.stopLightningSpeed()
        }
    }

    override fun onIncomingCallStarted(ctx: Context, number: String?, start: Date) {
        if (RuleChecker.checkTurnFlashForComingCall()) {
            FlashCallSettingImpl.startLightningSpeed()
        }
    }
}

@SuppressLint("OverrideAbstract")
@RequiresApi(Build.VERSION_CODES.KITKAT)
class FlashCallNotificationListenerService : NotificationListenerService() {
    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        sbn?.let {
            if (RuleChecker.checkNotificationApp(it.packageName)) {
                FlashCallSettingImpl.startLightningSpeed()
            }
        }
    }

}