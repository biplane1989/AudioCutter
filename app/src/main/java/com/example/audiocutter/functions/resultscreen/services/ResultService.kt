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
import com.example.audiocutter.activities.MainActivity
import com.example.audiocutter.core.manager.ManagerFactory
import com.example.audiocutter.functions.mystudio.Constance
import com.example.audiocutter.functions.resultscreen.objects.ConvertingItem
import com.example.audiocutter.functions.resultscreen.objects.ConvertingState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ResultService : LifecycleService() {

    private val TAG = "giangtd"
    private val mBinder: IBinder = MyBinder()
    private lateinit var mBuilder: NotificationCompat.Builder
    private lateinit var manager: NotificationManagerCompat
    private var strContent: String? = ""

    private val progressMax = 100
    private var serviceForegroundID = -1
    private var TYPE_AUDIO = -1

    private val processObserver = Observer<ConvertingItem?> { it ->
        if (it != null) {
            Log.d(TAG, "ResultService percent : " + it.percent + "   status : " + it.state + " typeAudio: " + it.typeAudio)
            TYPE_AUDIO = it.typeAudio!!
            builderNotification(it.getFileName())
            when (it.state) {
                ConvertingState.PROGRESSING -> {
                    sendNotification(serviceForegroundID, it.percent, it.state)
                }
                ConvertingState.SUCCESS -> {
                    sendNotificationComplte(it.id)
                    Log.d(TAG, "cancelNotidication : create ID : " + it.id)
                }
                ConvertingState.ERROR -> {
                    sendNotificationFail(it.id)
                }
                else -> {
                    //nothing
                }
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
            Log.d(TAG, "onBind: TYPE_AUDIO : " + TYPE_AUDIO)
            manager = NotificationManagerCompat.from(this@ResultService)
            observerData()
            builderForegroundService(0)
        }
        return mBinder
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        return START_STICKY
    }

    private fun observerData() {
        ManagerFactory.getAudioEditorManager().getCurrentProcessingItem()
            .observe(this, processObserver)
    }

    fun cancelNotidication(id: Int) {       // cancel 1 notification
        manager.cancel(id)
        Log.d(TAG, "cancelNotidication: cancel ID : " + id)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopForeground(true)
    }

    private fun resultIntent(): Intent {
        Log.d(TAG, "resultIntent: TYPE_AUDIO : " + TYPE_AUDIO)
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            setAction(Constance.NOTIFICATION_ACTION_EDITOR)
            putExtra(Constance.TYPE_RESULT, TYPE_AUDIO)
        }
        return intent
    }

    private fun builderNotification(audioTitle: String) {           // build 1 notification
        val resultPendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, resultIntent(), PendingIntent.FLAG_UPDATE_CURRENT)

        mBuilder = NotificationCompat.Builder(this, MyApplication.CHANNEL_ID)
            .setSmallIcon(R.drawable.notification_icon).setContentTitle(audioTitle)
            .setContentText(strContent).setOngoing(true).setContentIntent(resultPendingIntent)
            .setOnlyAlertOnce(true).setProgress(progressMax, 0, true).setAutoCancel(true)
    }

    private fun sendNotificationComplte(notificationID: Int) {          // send 1 notification khi hoan thanh
        mBuilder.setContentText(getString(R.string.result_sercice_loading_complete))
            .setProgress(0, 0, false).setOngoing(false)
        manager.notify(notificationID, mBuilder.build())
    }

    private fun sendNotificationFail(notificationID: Int) {
        mBuilder.setContentText(getString(R.string.result_service_loading_fail))
            .setProgress(0, 0, false).setOngoing(false)
        manager.notify(notificationID, mBuilder.build())
    }

    fun refeshNotification() {
        manager.cancelAll()
    }

    private fun sendNotification(notificationID: Int, data: Int, convertingState: ConvertingState) {        // send 1 notification

        when (convertingState) {
            ConvertingState.PROGRESSING -> {
                mBuilder.setContentText(data.toString() + "%").setProgress(progressMax, data, false)
                    .build()
            }
            ConvertingState.SUCCESS -> {
                mBuilder.setContentText(getString(R.string.result_service_loading_complete))
                    .setProgress(0, 0, false).setOngoing(false)
            }
            ConvertingState.ERROR -> {
//                stopForeground(serviceForegroundID)
                mBuilder.setContentText(getString(R.string.result_service_loading_fail))
                    .setProgress(0, 0, false).setOngoing(false)
            }
            else -> {
                //nothing
            }
        }
        manager.notify(notificationID, mBuilder.build())
    }

//    private fun builderForegroundService() {        // build foreground service
//        val resultPendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, resultIntent(), PendingIntent.FLAG_CANCEL_CURRENT)
//
//        val notification = NotificationCompat.Builder(this, MyApplication.CHANNEL_ID)
//            .setContentTitle(getString(R.string.result_service_content_title))
//            .setSmallIcon(R.drawable.notification_icon).setContentText(strContent)
//            .setContentIntent(resultPendingIntent).setOngoing(true).setOnlyAlertOnce(true)
//            .setProgress(progressMax, 0, true).setAutoCancel(true).build()
//
//        startForeground(serviceForegroundID, notification)
//    }

    fun builderForegroundService(typeAudio: Int) {        // build foreground service
        TYPE_AUDIO = typeAudio
        val resultPendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, resultIntent(), PendingIntent.FLAG_UPDATE_CURRENT)

        val notification = NotificationCompat.Builder(this, MyApplication.CHANNEL_ID)
            .setContentTitle(getString(R.string.result_service_content_title))
            .setSmallIcon(R.drawable.notification_icon).setContentText(strContent)
            .setContentIntent(resultPendingIntent).setOngoing(true).setOnlyAlertOnce(true)
            .setProgress(progressMax, 0, true).setAutoCancel(true).build()

        startForeground(serviceForegroundID, notification)
    }

}