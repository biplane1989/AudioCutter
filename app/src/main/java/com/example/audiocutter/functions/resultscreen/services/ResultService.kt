package com.example.audiocutter.functions.resultscreen.services

import android.app.PendingIntent
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.Observer
import com.example.audiocutter.MyApplication
import com.example.audiocutter.R
import com.example.audiocutter.core.manager.ManagerFactory
import com.example.audiocutter.functions.mystudio.Constance
import com.example.audiocutter.functions.mystudio.screens.OutputActivity
import com.example.audiocutter.functions.resultscreen.objects.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ResultService : LifecycleService() {

    val TAG = "giangtd"
    private val mBinder: IBinder = MyBinder()
    private lateinit var mBuilder: NotificationCompat.Builder
    private lateinit var manager: NotificationManagerCompat
    var strContent: String? = ""

    val progressMax = 100
    var serviceForegroundID = -1
    var TYPE_AUDIO = -1

    val processObserver = Observer<ConvertingItem> { it ->
        if (it != null) {
            Log.d(TAG, "ResultService percent : " + it.percent + "   status : " + it.state)
            builderNotification(it.getFileName())
            when (it.state) {
                ConvertingState.PROGRESSING -> {
                    sendNotification(serviceForegroundID, it.percent, it.state)
                }
                ConvertingState.SUCCESS -> {
                    sendNotificationComplte(it.id)
                }
//                ConvertingState.ERROR -> {
////                    sendNotificationFail(it.id)
////                    stopForeground(serviceForegroundID)
////                    sendNotificationFail(serviceForegroundID)
//                }
            }
        }
    }

    inner class MyBinder : Binder() {
        val service: ResultService
            get() = this@ResultService
    }

    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)

        CoroutineScope(Dispatchers.Main).launch {
            TYPE_AUDIO = intent.getIntExtra(Constance.TYPE_AUDIO, 0)
            manager = NotificationManagerCompat.from(this@ResultService)
            observerData()
            builderForegroundService()
        }
        return mBinder
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        return START_STICKY
    }

    fun observerData() {
        ManagerFactory.getAudioEditorManager().getCurrentProcessingItem()
            .observe(this, processObserver)
    }

    fun cancelNotidication(id: Int) {       // cancel 1 notification
        manager.cancel(id)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopForeground(true)
    }

    fun resultIntent(): Intent {
        val intent = Intent(this, OutputActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            setAction(Constance.NOTIFICATION_ACTION_EDITOR)
            putExtra(Constance.TYPE_RESULT, TYPE_AUDIO)
        }
        return intent
    }

    fun builderNotification(audioTitle: String) {           // build 1 notification
        val resultPendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, resultIntent(), 0)

        mBuilder = NotificationCompat.Builder(this, MyApplication.CHANNEL_ID)
            .setSmallIcon(R.drawable.list_contact_icon_back).setContentTitle(audioTitle)
            .setContentText(strContent).setOngoing(true).setContentIntent(resultPendingIntent)
            .setOnlyAlertOnce(true).setProgress(progressMax, 0, true).setAutoCancel(true)
    }

    fun sendNotificationComplte(notificationID: Int) {          // send 1 notification khi hoan thanh
        mBuilder.setContentText("Loading complete").setProgress(0, 0, false).setOngoing(false)
        manager.notify(notificationID, mBuilder.build())
    }

    fun sendNotificationFail(notificationID: Int) {
        mBuilder.setContentText("Fail").setProgress(0, 0, false).setOngoing(false)
        manager.notify(notificationID, mBuilder.build())
    }

    fun sendNotification(notificationID: Int, data: Int, convertingState: ConvertingState) {        // send 1 notification

        when (convertingState) {
            ConvertingState.PROGRESSING -> {
                mBuilder.setContentText(data.toString() + "%").setProgress(progressMax, data, false)
                    .build()
                manager.notify(notificationID, mBuilder.build())
            }
            ConvertingState.SUCCESS -> {
                mBuilder.setContentText("Loading complete").setProgress(0, 0, false)
                    .setOngoing(false)
                manager.notify(notificationID, mBuilder.build())
            }
//            ConvertingState.ERROR -> {
////                stopForeground(serviceForegroundID)
////                mBuilder.setContentText("Fail").setProgress(0, 0, false).setOngoing(false)
////                manager.notify(notificationID, mBuilder.build())
//            }
        }
    }

    fun builderForegroundService() {        // build foreground service
        val resultPendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, resultIntent(), PendingIntent.FLAG_CANCEL_CURRENT)

        val notification = NotificationCompat.Builder(this, MyApplication.CHANNEL_ID)
            .setContentTitle("Audio Cutter Loading").setSmallIcon(R.drawable.list_contact_icon_back)
            .setContentText(strContent).setContentIntent(resultPendingIntent).setOngoing(true)
            .setOnlyAlertOnce(true).setProgress(progressMax, 0, true).setAutoCancel(true).build()

        startForeground(serviceForegroundID, notification)
    }
}