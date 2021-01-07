package com.example.audiocutter.functions.resultscreen.services

import android.app.PendingIntent
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.Observer
import com.example.audiocutter.MyApplication
import com.example.audiocutter.R
import com.example.audiocutter.activities.MainActivity
import com.example.audiocutter.core.manager.ManagerFactory
import com.example.audiocutter.functions.mystudio.Constance
import com.example.audiocutter.functions.resultscreen.objects.*

class ResultService : LifecycleService() {

    private val TAG = "giangtd"
    private val mBinder: IBinder = MyBinder()
    private lateinit var manager: NotificationManagerCompat
    private var strContent: String? = ""

    private val progressMax = 100
    private var serviceForegroundID = -1

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

    inner class MyBinder : Binder() {
        val service: ResultService
            get() = this@ResultService
    }

    override fun onCreate() {
        super.onCreate()
        manager = NotificationManagerCompat.from(this@ResultService)
        observerData()
    }

    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
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

    private fun sendNotificationComplte(notificationID: Int, audioTitle: String, typeAudio: Int) {          // send 1 notification khi hoan thanh
        Log.d("taihhhhh", "sendNotificationComplte notificationID ${notificationID} audio type : ${typeAudio}")
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
            .setContentText(getString(R.string.result_sercice_loading_complete)).setOngoing(true)
            .setContentIntent(resultPendingIntent).setOnlyAlertOnce(true).setProgress(0, 0, false)
            .setOngoing(false).setAutoCancel(true)
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
            .setContentText(getString(R.string.result_service_loading_fail)).setOngoing(true)
            .setContentIntent(resultPendingIntent).setOnlyAlertOnce(true).setProgress(0, 0, false)
            .setOngoing(false).setAutoCancel(true)
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

    fun refeshNotification() {
        manager.cancelAll()
    }

    fun builderForegroundService(typeAudio: Int) {        // build foreground service
//        val resultPendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, resultIntent(serviceForegroundID, typeAudio), PendingIntent.FLAG_UPDATE_CURRENT)

//        Log.d(TAG, "builderForegroundService: ${resultPendingIntent}")

        val notification = NotificationCompat.Builder(this, MyApplication.CHANNEL_ID)
            .setContentTitle(getString(R.string.result_service_content_title))
            .setSmallIcon(R.drawable.notification_icon).setContentText(strContent)
//            .setContentIntent(resultPendingIntent).setOngoing(true).setOnlyAlertOnce(true)
            .setOngoing(true).setOnlyAlertOnce(true).setProgress(progressMax, 0, true)
            .setAutoCancel(true).build()

        startForeground(serviceForegroundID, notification)
    }

}