package com.example.audiocutter.functions.resultscreen.services

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.res.Configuration
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.*
import androidx.lifecycle.Observer
import com.example.audiocutter.MyApplication
import com.example.audiocutter.R
import com.example.audiocutter.activities.MainActivity
import com.example.audiocutter.core.manager.ManagerFactory
import com.example.audiocutter.functions.mystudio.Constance
import com.example.audiocutter.functions.resultscreen.objects.*
import com.example.audiocutter.util.PreferencesHelper
import com.example.audiocutter.util.Utils
import java.util.*

class ResultService : Service() , LifecycleOwner {

    private val TAG = "giangtd"
    private lateinit var manager: NotificationManagerCompat
    private var strContent: String? = ""

    private val progressMax = 100
    private var serviceForegroundID = -1
    private var lifecycleRegistry: LifecycleRegistry = LifecycleRegistry(this)

    private val processObserver = Observer<ConvertingItem?> { it ->
        if (it != null) {
            when (it.state) {
                ConvertingState.PROGRESSING -> {
                    sendNotificationConverting(serviceForegroundID, it.percent, it.state, it.getFileName(), it.getAudioType())
                }
                ConvertingState.SUCCESS -> {
                    Log.d(TAG, "processObserver: SUCCESS")
                    sendNotificationComplte(it.id, it.getFileName(), it.getAudioType())
                }
                ConvertingState.ERROR -> {
                    sendNotificationFail(it.id, it.getFileName(), it.getAudioType())
                }
                else -> {
                    //nothing
                }
            }
        }
    }


    override fun onCreate() {
        super.onCreate()
        lifecycleRegistry.currentState = Lifecycle.State.CREATED
        manager = NotificationManagerCompat.from(this@ResultService)
        observerData()
        setLanguage()
    }

    private fun setLanguage() {
        val language: String = PreferencesHelper.getString(PreferencesHelper.APP_LANGUAGE, Utils.getDefaultLanguage())
        Log.d("abba", "setLanguage: $language")

        val myLocale = Locale(language)

        Utils.updateLocale(this, myLocale)

//        Locale.setDefault(myLocale)
//        val conf = resources.configuration
//        conf.setLocale(myLocale)
//        resources.updateConfiguration(conf, resources.displayMetrics)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        lifecycleRegistry .currentState = Lifecycle.State.STARTED
        intent?.let {
            when (intent.action) {
                Constance.SERVICE_ACTION_BUILD_FORGROUND_SERVICE -> {
                    builderForegroundService()
                }
                Constance.SERVICE_ACTION_CANCEL_NOTIFICATION -> {
                    val id = intent.getIntExtra(Constance.SERVICE_ACTION_CANCEL_ID, -1)
                    cancelNotidication(id)
                }
                Constance.SERVICE_ACTION_REFESHER_NOTIFICATION -> {
                    refeshNotification()
                }
                Constance.SERVICE_ACTION_CHANGE_LANGUAGE -> {
                    val myLocale = Locale(PreferencesHelper.getLanguage())
                    Utils.updateLocale(this, myLocale)

//                Locale.setDefault(myLocale)
//                val conf: Configuration = resources.configuration
//                conf.setLocale(myLocale)
//                resources.updateConfiguration(conf, resources.displayMetrics)
                }
            }
        }
        return START_STICKY
    }

    private fun observerData() {
        ManagerFactory.getAudioEditorManager().getCurrentProcessingItem()
            .observe(this, processObserver)
    }

    private fun cancelNotidication(id: Int) {       // cancel 1 notification
        manager.cancel(id)
    }

    private fun refeshNotification() {
        manager.cancelAll()
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycleRegistry .currentState = Lifecycle.State.DESTROYED
        stopForeground(true)
        Log.d(TAG, "onDestroy: service ")
    }

    override fun onBind(p0: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    private fun sendNotificationComplte(notificationID: Int, audioTitle: String, typeAudio: Int) {          // send 1 notification khi hoan thanh
        when (typeAudio) {
            CUTTING_AUDIO_TYPE -> {
                buildNotificationComplete(Constance.NOTIFICATION_ACTION_EDITOR_COMPLETE_CUT, Constance.TYPE_RESULT_COMPLETE_CUT, typeAudio, notificationID, audioTitle)
            }
            MERGING_AUDIO_TYPE -> {
                buildNotificationComplete(Constance.NOTIFICATION_ACTION_EDITOR_COMPLETE_MERGER, Constance.TYPE_RESULT_COMPLETE_MERGER, typeAudio, notificationID, audioTitle)
            }
            MIXING_AUDIO_TYPE -> {
                buildNotificationComplete(Constance.NOTIFICATION_ACTION_EDITOR_COMPLETE_MIX, Constance.TYPE_RESULT_COMPLETE_MIX, typeAudio, notificationID, audioTitle)
            }
        }
    }

    private fun buildNotificationComplete(action: String, typeAction: String, typeAudio: Int, notificationID: Int, audioTitle: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            setAction(action)
            putExtra(typeAction, typeAudio)
        }
        val resultPendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val builder = NotificationCompat.Builder(this, MyApplication.CHANNEL_ID)
            .setSmallIcon(R.drawable.notification_icon).setContentTitle(audioTitle)
            .setContentText(resources.getString(R.string.result_sercice_loading_complete))
            .setOngoing(true).setLocalOnly(true).setContentIntent(resultPendingIntent)
            .setOnlyAlertOnce(true).setProgress(0, 0, false).setOngoing(false).setAutoCancel(true)
        manager.notify(notificationID, builder.build())
    }


    private fun sendNotificationFail(notificationID: Int, audioTitle: String, typeAudio: Int) {
        val intent = Intent(this, MainActivity::class.java).apply {
            setAction(Constance.NOTIFICATION_ACTION_EDITOR_FAIL)
            putExtra(Constance.TYPE_RESULT_FAIL, typeAudio)
        }
        val resultPendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val builder = NotificationCompat.Builder(this, MyApplication.CHANNEL_ID)
            .setSmallIcon(R.drawable.notification_icon).setContentTitle(audioTitle)
            .setContentText(resources.getString(R.string.result_service_loading_fail))
            .setOngoing(true).setContentIntent(resultPendingIntent).setOnlyAlertOnce(true)
            .setProgress(0, 0, false).setOngoing(false).setAutoCancel(true)
        manager.notify(notificationID, builder.build())
    }

    private fun sendNotificationConverting(notificationID: Int, data: Int, convertingState: ConvertingState, audioTitle: String, typeAudio: Int) {        // send 1 notification

        val intent = Intent(this, MainActivity::class.java).apply {
            setAction(Constance.NOTIFICATION_ACTION_EDITOR_CONVERTING)
            putExtra(Constance.TYPE_RESULT_CONVERTING, typeAudio)
        }
        val resultPendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val builder = NotificationCompat.Builder(this, MyApplication.CHANNEL_ID)
            .setSmallIcon(R.drawable.notification_icon).setContentTitle(audioTitle)
            .setContentText(data.toString() + "%").setOngoing(true)
            .setContentIntent(resultPendingIntent).setOnlyAlertOnce(true)
            .setProgress(progressMax, data, false).setOngoing(false).setAutoCancel(true)
        manager.notify(notificationID, builder.build())
    }


    fun builderForegroundService() {        // build foreground service
        val notification = NotificationCompat.Builder(this, MyApplication.CHANNEL_ID)
            .setContentTitle(resources.getString(R.string.result_service_content_title))
            .setSmallIcon(R.drawable.notification_icon).setContentText(strContent)
            .setOnlyAlertOnce(true).setOngoing(true).setOnlyAlertOnce(true)
            .setProgress(progressMax, 0, true).setAutoCancel(true).build()

        startForeground(serviceForegroundID, notification)
    }

    override fun getLifecycle(): Lifecycle {
        return lifecycleRegistry
    }
}