package com.example.audiocutter

import android.app.Application
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.graphics.Color
import android.os.Build
import com.example.audiocutter.core.ManagerFactory
import com.example.audiocutter.permissions.PermissionManager
import com.example.audiocutter.util.PreferencesHelper

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        PreferencesHelper.start(applicationContext)
        PermissionManager.start(applicationContext)
        ManagerFactory.init(applicationContext)
        createNotificationChannels()
    }

    companion object {
        val CHANNEL_ID = "Audio Notification"
        val CHANNEL_NAME = "Audio Cutter"
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH)
            channel.description = "Audio Cutter"
            channel.enableLights(true)
            channel.enableVibration(true)
            channel.lightColor = Color.GREEN
            channel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

}