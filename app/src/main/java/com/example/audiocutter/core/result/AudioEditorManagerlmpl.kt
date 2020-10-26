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
import com.example.audiocutter.core.manager.AudioEditorManager
import com.example.audiocutter.core.manager.ManagerFactory
import com.example.audiocutter.functions.mystudio.Constance
import com.example.audiocutter.functions.resultscreen.objects.*
import com.example.audiocutter.functions.resultscreen.services.ResultService
import com.example.audiocutter.objects.AudioFile
import com.example.core.core.AudioCore
import com.example.core.core.AudioMixConfig
import com.example.core.core.FFMpegState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File


object AudioEditorManagerlmpl : AudioEditorManager {

    lateinit var mContext: Context
    val TAG = "giangtd"

    fun init(context: Context) {
        mContext = context

        ManagerFactory.getAudioCutter().getAudioMergingInfo().observeForever { audioMering ->

            mainScope.launch {
                var convertingState: ConvertingState = ConvertingState.WAITING
                when (audioMering.state) {
                    FFMpegState.IDE -> {
                        convertingState = ConvertingState.WAITING
                    }
                    FFMpegState.RUNNING -> {
                        convertingState = ConvertingState.PROGRESSING
                        if (audioMering.percent == 100) {
                            convertingState = ConvertingState.SUCCESS
                        }
                    }
                    FFMpegState.CANCEL -> {
//                        convertingState = ConvertingState.SUCCESS
                    }
                    FFMpegState.FAIL -> {
                        convertingState = ConvertingState.ERROR
                    }
                }
                Log.e(TAG, "init: ${audioMering.percent}")
                audioMering.audioFile?.let {
                    for (item in listConvertingItemData) {
                        if (item.state == ConvertingState.PROGRESSING) {
                            item.state = convertingState
                            item.percent = audioMering.percent
                            item.audioFile = AudioFile(it.file, it.fileName, it.size, it.bitRate)

                            currentProcessingItem.postValue(item)
                        }
                    }
//                currentProcessingItem.postValue(ConvertingItem(currConvertingId, convertingState, audioMering.percent, AudioFile(it.file, it.fileName, it.size, it.bitRate)))
                }
            }
        }
    }

    val CUT_AUDIO = 0
    val MER_AUDIO = 1
    val MIX_AUDIO = 2

    var mService: ResultService? = null
    var mIsBound: Boolean = false

    private val listConvertingItemData = ArrayList<ConvertingItem>()
    private val listCopyConvertingItemData = ArrayList<ConvertingItem>()
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
            Log.d(TAG, "getWaitingItem: " + waitingItem.state)
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
        listCopyConvertingItemData.add(item)

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

    override fun mixAudio(audioFile1: AudioFile, audioFile2: AudioFile, mixingConfig: AudioMixConfig, outFile: AudioFile) {

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
        listCopyConvertingItemData.add(item)

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
        listCopyConvertingItemData.add(item)

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

        mainScope.launch {
            notifyConvertingItemChanged(null)

            item.state = ConvertingState.PROGRESSING
            notifyConvertingItemChanged(item)

            if (item is MergingConvertingItem) {
                val listAudioCore = ArrayList<AudioCore>()
                item.listAudioFiles.forEach {
                    listAudioCore.add(AudioCore(it.file, it.fileName, it.size, it.bitRate, it.time, it.mimeType))
                }
                ManagerFactory.getAudioCutter()
                    .merge(listAudioCore, item.audioFile.fileName, item.mergingConfig.audioFormat, item.audioFile.file.parent)
            }

            if (item is MixingConvertingItem) {
                currentProcessingItem.postValue(item)

                val audioCore1 = AudioCore(item.audioFile1.file, item.audioFile1.fileName, item.audioFile1.size, item.audioFile1.bitRate, item.audioFile1.time, item.audioFile1.mimeType)
                val audioCore2 = AudioCore(item.audioFile2.file, item.audioFile2.fileName, item.audioFile2.size, item.audioFile2.bitRate, item.audioFile2.time, item.audioFile2.mimeType)

                ManagerFactory.getAudioCutter().mix(audioCore1, audioCore2, item.mixingConfig)
            }

            if (item is CuttingConvertingItem) {

            }

            item.state = ConvertingState.SUCCESS
            notifyConvertingItemChanged(item)

            listConvertingItemData.remove(item)                             // demo dong bo data voi luu tru thu muc
            listConvertingItems.postValue(listConvertingItemData)
            processNextItem()
        }
    }

    override fun cancel(id: Int) {
        mainScope.launch {
            /*for (item in listConvertingItemData) {
                if (item.id == id) {
                    when (item.state) {
                        ConvertingState.WAITING -> {
//                            for (item in listConvertingItemData) {
//                                if (item.id == id) {
                            listConvertingItemData.remove(item)
//                                }
//                            }
                        }
                        ConvertingState.PROGRESSING -> {

                            listConvertingItemData.remove(item)
                            ManagerFactory.getAudioCutter().cancelTask()
                            mService?.cancelNotidication(id)

//                            for (item in listConvertingItemData) {
//                                if (item.id == id) {
//                                }
//                            }
                        }
                    }
                }
            }*/

            val iterator: MutableIterator<ConvertingItem> = listConvertingItemData.iterator()
            while (iterator.hasNext()) {
                val value = iterator.next()
                if (value.id == id) {
                    when (value.state) {
                        ConvertingState.WAITING -> {
                            iterator.remove()
                        }
                        ConvertingState.PROGRESSING -> {
                            iterator.remove()
                            ManagerFactory.getAudioCutter().cancelTask()
                            mService?.cancelNotidication(id)
                        }
                    }
                }
            }
            listConvertingItems.postValue(listConvertingItemData)
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
        return currConvertingId
    }

    override fun getConvertingItem(): ConvertingItem {
        return listCopyConvertingItemData.get(listCopyConvertingItemData.size - 1)
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