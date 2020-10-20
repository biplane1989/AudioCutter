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
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.example.audiocutter.MyApplication
import com.example.audiocutter.R
import com.example.audiocutter.activities.MainActivity
import com.example.audiocutter.core.ManagerFactory
import com.example.audiocutter.functions.mystudio.Constance
import com.example.audiocutter.functions.mystudio.screens.OutputActivity
import com.example.audiocutter.functions.resultscreen.objects.ConvertingItem
import com.example.audiocutter.functions.resultscreen.objects.ConvertingState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ResultService : LifecycleService() {

    val TAG = "giangtd"
    private val mBinder: IBinder = MyBinder()
    private lateinit var mBuilder: NotificationCompat.Builder
    private lateinit var manager: NotificationManagerCompat

    //    var strContent: String? = "tomato"
    var strContent: String? = "Apple"
    val updateProressbar: MutableLiveData<Int> = MutableLiveData()
    val progressMax = 100
    var notificationID = 0
    var TYPE_AUDIO = -1

    @RequiresApi(Build.VERSION_CODES.N)
    val processObserver = Observer<ConvertingItem> { it ->
        builderNotification(it.audioFile.title.toString())
        sendNotification(1, it.percent)

        if (ManagerFactory.getAudioEditorManager().getIDProcessingItem() > 1) {
            if (it.state == ConvertingState.SUCCESS) {
                builderNotification(it.audioFile.title.toString())
                sendNotificationComplte(notificationID++)
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
            builderForegroundService(1)
            observerData()
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

    fun cancelNotidication() {
        stopForeground(true)
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

    fun builderNotification(audioTitle: String) {
        val resultPendingIntent: PendingIntent =
            PendingIntent.getActivity(this, 0, resultIntent(), 0)


        mBuilder = NotificationCompat.Builder(this, MyApplication.CHANNEL_ID)
            .setSmallIcon(R.drawable.list_contact_icon_back).setContentTitle(audioTitle)
            .setContentText(strContent).setOngoing(true).setContentIntent(resultPendingIntent)
            .setOnlyAlertOnce(true).setProgress(progressMax, 0, true).setAutoCancel(true)
    }

    fun sendNotificationComplte(notificationID: Int) {
        mBuilder.setContentText("Download complete").setProgress(0, 0, false).setOngoing(false)
        manager.notify(notificationID, mBuilder.build())
    }

    fun sendNotification(notificationID: Int, data: Int) {

        if (data > 99) {
            mBuilder.setContentText("Download complete").setProgress(0, 0, false).setOngoing(false)
            manager.notify(notificationID, mBuilder.build())
        } else {
            mBuilder.setContentText(data.toString() + "%").setProgress(progressMax, data, false)
                .build()
            manager.notify(notificationID, mBuilder.build())

            updateProressbar.postValue(data)
        }
    }

    fun builderForegroundService(notificationID: Int) {

        val resultPendingIntent: PendingIntent =
            PendingIntent.getActivity(this, 0, resultIntent(), PendingIntent.FLAG_CANCEL_CURRENT)
        val progressMax = 100

        /*val notification = NotificationCompat.Builder(this, MyApplication.CHANNEL_ID)
            .setContentTitle("Orange Download").setSmallIcon(R.drawable.list_contact_icon_back)
            .setContentText(strContent).setContentIntent(resultPendingIntent).setOngoing(true)
            .setOnlyAlertOnce(true).setProgress(progressMax, 0, true).setAutoCancel(true).build()*/

        val notification = NotificationCompat.Builder(this, MyApplication.CHANNEL_ID)
            .setContentTitle("Orange Download").setSmallIcon(R.drawable.list_contact_icon_back)
            .setContentText(strContent).setContentIntent(resultPendingIntent).setOngoing(true)
            .setOnlyAlertOnce(true).setProgress(progressMax, 0, true).setAutoCancel(true).build()

        startForeground(notificationID, notification)
    }

}