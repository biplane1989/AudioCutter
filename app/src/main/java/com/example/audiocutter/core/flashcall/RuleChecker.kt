package com.example.audiocutter.core.flashcall

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.util.Log
import com.example.audiocutter.core.manager.FlashCallConfig

object RuleChecker : BroadcastReceiver() {
    private const val TAG = "RuleChecker"
    private var isScreenOff = false
    fun init() {
        val filter = IntentFilter(Intent.ACTION_SCREEN_OFF)
        filter.addAction(Intent.ACTION_SCREEN_ON)
        filter.addAction(Intent.ACTION_SCREEN_OFF)
        FlashCallSettingImpl.getAppContext().registerReceiver(this, filter)
    }

    fun checkTurnFlashForComingCall(): Boolean {
        val flashCallConfig = FlashCallSettingImpl.getFlashCallConfigData()
        if (!precondition(flashCallConfig)) {
            return false
        }
        if(!flashCallConfig.incomingCallEnable){
            return false
        }
        return true
    }

    fun checkNotificationApp(pkgName: String): Boolean {
        val flashCallConfig = FlashCallSettingImpl.getFlashCallConfigData()
        if (!precondition(flashCallConfig)) {
            return false
        }
        if(!FlashCallSettingImpl.isNotificationEnabled(pkgName)){
            return false
        }
        return true
    }

    private fun precondition(flashCallConfig: FlashCallConfig): Boolean {
        if (!flashCallConfig.enable) {
            return false
        }
        if (flashCallConfig.flashTimer.enable && flashCallConfig.flashTimer.isNowInRange()) {
            return false
        }

        if (!isScreenOff && flashCallConfig.notFiredWhenInUsed) {
            return false
        }

        val audioManager = FlashCallSettingImpl.getAppContext()
            .getSystemService(Context.AUDIO_SERVICE) as AudioManager

        when (audioManager.ringerMode) {
            AudioManager.RINGER_MODE_NORMAL -> {
                if (!flashCallConfig.flashMode.bellEnable) {
                    return false
                }
            }
            AudioManager.RINGER_MODE_VIBRATE -> {
                if (!flashCallConfig.flashMode.vibrateEnable) {
                    return false
                }
            }
            AudioManager.RINGER_MODE_SILENT -> {
                if (!flashCallConfig.flashMode.silentEnable) {
                    return false
                }
            }
        }

        return true
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            Log.d(TAG, "ACTION_SCREEN_OFF ")
            isScreenOff = true
        }
        if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
            Log.d(TAG, "ACTION_SCREEN_ON ")
            isScreenOff = false
        }
    }
}