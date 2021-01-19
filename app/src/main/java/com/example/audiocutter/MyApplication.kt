package com.example.audiocutter

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.graphics.Color
import android.os.Build
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.multidex.MultiDexApplication
import com.example.audiocutter.core.manager.ManagerFactory
import com.example.audiocutter.permissions.PermissionManager
import com.example.audiocutter.util.PreferencesHelper
import com.example.audiocutter.util.Utils
import java.util.*

class MyApplication : MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        PreferencesHelper.start(applicationContext)
        PermissionManager.start(applicationContext)
        ManagerFactory.init(applicationContext)
        createNotificationChannels()
    }

    companion object {
        val CHANNEL_ID = "Audio Notification"
        val CHANNEL_NAME = "Audio Cutter"
        val DESCRIPTION = "description"
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.d("abba", "createNotificationChannels: ")
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH)
            channel.description = DESCRIPTION
            channel.enableLights(true)
            channel.enableVibration(true)
            channel.lightColor = Color.GREEN
            channel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

}