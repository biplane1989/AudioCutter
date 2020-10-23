package com.example.audiocutter.core.result

import android.app.ActivityManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.example.audiocutter.core.manager.ManagerFactory
import com.example.audiocutter.core.manager.AudioEditorManager
import com.example.audiocutter.functions.mystudio.Constance
import com.example.audiocutter.functions.resultscreen.objects.*
import com.example.audiocutter.functions.resultscreen.services.ResultService
import com.example.audiocutter.objects.AudioFile
import com.example.core.core.AudioCore
import kotlinx.coroutines.*
import java.io.File


object AudioEditorManagerlmpl : AudioEditorManager {

    lateinit var mContext: Context
    val TAG = "giangtd"
    fun init(context: Context) {
        mContext = context
        ManagerFactory.getAudioCutter().getAudioMergingInfo().observeForever {
            Log.e(TAG, "init: ${it.percent}")
        }
    }

    val CUT_AUDIO = 0
    val MER_AUDIO = 1
    val MIX_AUDIO = 2

    var mService: ResultService? = null
    var mIsBound: Boolean = false
    var notificationID = 4

    private val listConvertingItemData = ArrayList<ConvertingItem>()
    private val listConvertingItems = MutableLiveData<List<ConvertingItem>>()  // list chung
    private var currConvertingId = 0
    private val mainScope = MainScope()
    private val currentProcessingItem = MutableLiveData<ConvertingItem>()


    val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, iBinder: IBinder) {
            val binder = iBinder as ResultService.MyBinder
            mService = binder.service
            mIsBound = true
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            mIsBound = false
        }
    }

    private fun getProcessingItem(): ConvertingItem? {
        for (waitingItem in listConvertingItemData) {
            if (waitingItem.state == ConvertingState.PROGRESSING) return waitingItem
        }
        return null
    }

    private fun getWaitingItem(): ConvertingItem? {
        for (waitingItem in listConvertingItemData) {
            if (waitingItem.state == ConvertingState.WAITING) return waitingItem
        }
        return null
    }

    override fun cutAudio(audioFile: AudioFile, cuttingConfig: CuttingConfig, outFile: File) {

        if (!isMyServiceRunning(ResultService::class.java)) {
            notifyConvertingItemChanged(null)
            Log.d(TAG, "cutAudio: is not runing")
            Intent(mContext, ResultService::class.java).also {
                it.putExtra(Constance.TYPE_AUDIO, CUT_AUDIO)
                mContext.bindService(it, serviceConnection, Context.BIND_AUTO_CREATE)
                Log.d(TAG, "bindService: ")
            }
        }
        currConvertingId++
        val item = CuttingConvertingItem(currConvertingId, ConvertingState.WAITING, 0, audioFile, cuttingConfig)
        listConvertingItemData.add(item)

        val processingItem = getProcessingItem()
        if (processingItem == null) {
            processNextItem()
        } else {

        }
        listConvertingItems.postValue(listConvertingItemData)

        Log.d(TAG, "currConvertingId: " + currConvertingId)
        for (item in listConvertingItemData) {
            Log.d(TAG, "item : " + item.id + " size: " + listConvertingItemData.size)
        }
    }

    override fun mixAudio(audioFile1: AudioFile, audioFile2: AudioFile, mixingConfig: MixingConfig, outFile: AudioFile) {

        if (!isMyServiceRunning(ResultService::class.java)) {
            notifyConvertingItemChanged(null)
            Intent(mContext, ResultService::class.java).also {
                it.putExtra(Constance.TYPE_AUDIO, MIX_AUDIO)
                mContext.bindService(it, serviceConnection, Context.BIND_AUTO_CREATE)
                Log.d(TAG, "bindService: ")
            }
        }
        currConvertingId++
        val item = MixingConvertingItem(currConvertingId, ConvertingState.WAITING, 0, outFile, audioFile1, audioFile2, mixingConfig)
        listConvertingItemData.add(item)

        val processingItem = getProcessingItem()
        if (processingItem == null) {
            processNextItem()
        } else {

        }
        listConvertingItems.postValue(listConvertingItemData)
    }

    override fun mergeAudio(listAudioFiles: List<AudioFile>, mergingConfig: MergingConfig, outFile: AudioFile) {

        if (!isMyServiceRunning(ResultService::class.java)) {
            notifyConvertingItemChanged(null)
            Intent(mContext, ResultService::class.java).also {
                it.putExtra(Constance.TYPE_AUDIO, MER_AUDIO)
                mContext.bindService(it, serviceConnection, Context.BIND_AUTO_CREATE)
                Log.d(TAG, "bindService: ")
            }
        }
        currConvertingId++
        val item = MergingConvertingItem(currConvertingId, ConvertingState.WAITING, 0, outFile, listAudioFiles, mergingConfig)
        listConvertingItemData.add(item)

        val processingItem = getProcessingItem()
        if (processingItem == null) {
            processNextItem()
        } else {

        }
        listConvertingItems.postValue(listConvertingItemData)
    }

    private fun processNextItem() {
        val waitingItem = getWaitingItem()
        if (waitingItem == null) {      // khi khong con item nao thi bo service di
            if (isMyServiceRunning(ResultService::class.java)) {
                Intent(mContext, ResultService::class.java).also { intent ->
                    mContext.unbindService(serviceConnection)
                }
                Log.d(TAG, "unbindService: ")
            }
        } else {
            waitingItem.state = ConvertingState.PROGRESSING
            mainScope.launch {
                processItem(waitingItem)
            }
        }
    }

    private fun notifyConvertingItemChanged(item: ConvertingItem?) {
        currentProcessingItem.postValue(item)
    }

    private suspend fun processItem(item: ConvertingItem) = withContext(Dispatchers.Default) {
        if (item is MergingConvertingItem){
            var listAudioCore= ArrayList<AudioCore>()
            item.listAudioFiles.forEach {
                listAudioCore.add(AudioCore(it.file,it.fileName,it.size,it.bitRate,it.time,it.mimeType))
            }
            ManagerFactory.getAudioCutter().merge(listAudioCore,item.audioFile.fileName,item.mergingConfig.audioFormat,item.audioFile.file.parent)
        }
        item.percent = 0
        notifyConvertingItemChanged(item)
        for (index in 0..100) {
            delay(100)
            item.percent = index
            notifyConvertingItemChanged(item)
        }
        item.state = ConvertingState.SUCCESS
        notifyConvertingItemChanged(item)

        listConvertingItemData.remove(item)                             // demo dong bo data voi luu tru thu muc
        listConvertingItems.postValue(listConvertingItemData)
        processNextItem()
    }

    override fun cancel(id: Int) {
        for (item in listConvertingItemData) {
            if (item.id == id) {
                when (item.state) {
                    ConvertingState.WAITING -> {
                        for (item in listConvertingItemData) {
                            if (item.id == id) {
                                listConvertingItemData.remove(item)
                            }
                        }
                    }
                    ConvertingState.PROGRESSING -> {
                        for (item in listConvertingItemData) {
                            if (item.id == id) {
                                listConvertingItemData.remove(item)
                            }
                        }
                        mService?.cancelNotidication(id)
                    }
                }
            }
        }
        listConvertingItems.postValue(listConvertingItemData)
    }

    override fun getCurrentProcessingItem(): LiveData<ConvertingItem> { // tra ra live data cho update progressbar
        return currentProcessingItem
    }

    override fun getListCuttingItems(): LiveData<List<ConvertingItem>> {  // tra live data cho list pending cho cutting
        return Transformations.map(listConvertingItems) {
            val listItem = ArrayList<ConvertingItem>()
            it.forEach {
                if (it is CuttingConvertingItem) {
                    listItem.add(it)
                }
            }
            listItem
        }
    }

    override fun getListMergingItems(): LiveData<List<ConvertingItem>> {
        return Transformations.map(listConvertingItems) {
            val listItem = ArrayList<ConvertingItem>()
            it.forEach {
                if (it is MergingConvertingItem) {
                    listItem.add(it)
                }
            }
            listItem
        }
    }

    override fun getListMixingItems(): LiveData<List<ConvertingItem>> {
        return Transformations.map(listConvertingItems) {
            val listItem = ArrayList<ConvertingItem>()
            it.forEach {
                if (it is MixingConvertingItem) {
                    listItem.add(it)
                }
            }
            listItem
        }
    }

    override fun getIDProcessingItem(): Int {
        return currConvertingId
    }

    override fun getConvertingItem(): ConvertingItem {
        return listConvertingItemData.get(listConvertingItemData.size - 1)
    }

    private fun isMyServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = mContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager?
        for (service in manager!!.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }
}