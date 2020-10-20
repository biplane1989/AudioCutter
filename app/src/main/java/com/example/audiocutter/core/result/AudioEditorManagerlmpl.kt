package com.example.audiocutter.core.result

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.example.audiocutter.functions.mystudio.Constance
import com.example.audiocutter.functions.resultscreen.objects.*
import com.example.audiocutter.functions.resultscreen.services.ResultService
import com.example.audiocutter.objects.AudioFile
import kotlinx.coroutines.*
import java.io.File
import kotlin.collections.ArrayList


object AudioEditorManagerlmpl : AudioEditorManager {

    lateinit var mContext: Context
    val TAG = "giangtd"
    fun init(context: Context) {
        mContext = context
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

//        if (!mIsBound) {
        Intent(mContext, ResultService::class.java).also {
            it.putExtra(Constance.TYPE_AUDIO, CUT_AUDIO)
            mContext.bindService(it, serviceConnection, Context.BIND_AUTO_CREATE)
            Log.d(TAG, "bindService: ")
        }
//        }
        val item = CuttingConvertingItem(currConvertingId, ConvertingState.WAITING, 0, audioFile, cuttingConfig)
        listConvertingItemData.add(item)
        currConvertingId++
        val processingItem = getProcessingItem()
        if (processingItem == null) {
            processNextItem()
        } else {

        }
        listConvertingItems.postValue(listConvertingItemData)
    }

    override fun mixAudio(audioFile1: AudioFile, audioFile2: AudioFile, mixingConfig: MixingConfig, outFile: AudioFile) {
        Intent(mContext, ResultService::class.java).also {
            it.putExtra(Constance.TYPE_AUDIO, MIX_AUDIO)
            mContext.bindService(it, serviceConnection, Context.BIND_AUTO_CREATE)
            Log.d(TAG, "bindService: ")
        }
        val item = MixingConvertingItem(currConvertingId, ConvertingState.WAITING, 0, outFile, audioFile1, audioFile2, mixingConfig)
        listConvertingItemData.add(item)
        currConvertingId++
        val processingItem = getProcessingItem()
        if (processingItem == null) {
            processNextItem()
        } else {

        }
        listConvertingItems.postValue(listConvertingItemData)
    }

    override fun mergeAudio(listAudioFiles: List<AudioFile>, mergingConfig: MergingConfig, outFile: AudioFile) {
        Intent(mContext, ResultService::class.java).also {
            it.putExtra(Constance.TYPE_AUDIO, MER_AUDIO)
            mContext.bindService(it, serviceConnection, Context.BIND_AUTO_CREATE)
            Log.d(TAG, "bindService: ")
        }

        val item = MergingConvertingItem(currConvertingId, ConvertingState.WAITING, 0, outFile, listAudioFiles, mergingConfig)
        listConvertingItemData.add(item)
        currConvertingId++
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

            if (mIsBound) {
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

    private fun notifyConvertingItemChanged(item: ConvertingItem) {
        currentProcessingItem.postValue(item)
//        listConvertingItems.postValue(listConvertingItemData)
    }

    private suspend fun processItem(item: ConvertingItem) = withContext(Dispatchers.Default) {
        item.percent = 0
        notifyConvertingItemChanged(item)
//        listConvertingItems.postValue(listConvertingItemData)
        (0..99).forEach {
            delay(100)
            item.percent++
            notifyConvertingItemChanged(item)
        }
//        listConvertingItems.postValue(listConvertingItemData)
        item.state = ConvertingState.SUCCESS
        notifyConvertingItemChanged(item)
        processNextItem()
    }


    override fun cancel(int: Int) {
        when (listConvertingItemData.get(int).state) {
            ConvertingState.WAITING -> {
                listConvertingItemData.drop(int)
            }
            ConvertingState.PROGRESSING -> {
                mService?.cancelNotidication()
            }
        }
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
        return listConvertingItemData.size - 1
    }

    override fun getConvertingItem(): ConvertingItem {
        return listConvertingItemData.get(listConvertingItemData.size - 1)
    }
}