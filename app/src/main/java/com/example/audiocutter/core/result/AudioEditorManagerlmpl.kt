package com.example.audiocutter.core.result

import android.app.ActivityManager
import android.content.*
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.example.audiocutter.core.manager.AudioEditorManager
import com.example.audiocutter.core.manager.ManagerFactory
import com.example.audiocutter.core.rington.RingtonManagerImpl
import com.example.audiocutter.functions.mystudio.Constance
import com.example.audiocutter.functions.resultscreen.objects.*
import com.example.audiocutter.functions.resultscreen.services.ResultService
import com.example.audiocutter.objects.AudioFile
import com.example.core.core.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File


object AudioEditorManagerlmpl : AudioEditorManager {

    private lateinit var mContext: Context
    private val TAG = "giangtd"

    private val CUT_AUDIO = 0
    private val MER_AUDIO = 1
    private val MIX_AUDIO = 2

    private var mService: ResultService? = null
    private var mIsBound: Boolean = false

    private val listConvertingItemData = ArrayList<ConvertingItem>()
    private val listConvertingItems = MutableLiveData<List<ConvertingItem>>()  // list chung
    private var currConvertingId = 0
    private val mainScope = MainScope()
    private val currentProcessingItem = MutableLiveData<ConvertingItem>()
    private var latestConvertingItem: ConvertingItem? = null   //phan tu cuoi cung
    fun init(context: Context) {
        mContext = context

        ManagerFactory.getAudioCutter().getAudioMergingInfo().observeForever { audioMering ->

            Log.d(TAG, "init: percent: " + audioMering.percent + " status : " + audioMering.state)
            var convertingState: ConvertingState = ConvertingState.WAITING
            when (audioMering.state) {
                FFMpegState.IDE -> {
                    convertingState = ConvertingState.WAITING
                }
                FFMpegState.RUNNING -> {
                    convertingState = ConvertingState.PROGRESSING
                }
                FFMpegState.CANCEL -> {
                    convertingState = ConvertingState.ERROR
                }
                FFMpegState.FAIL -> {
                    convertingState = ConvertingState.ERROR
                }
                FFMpegState.SUCCESS -> {
                    convertingState = ConvertingState.SUCCESS
                }
            }
            Log.e(TAG, "init: ${audioMering.percent}" + " convertingState: " + audioMering.state + " listConvertingItemData size: " + listConvertingItemData.size)
            currentProcessingItem.value?.let {

                it.percent = audioMering.percent
                it.state = convertingState
                notifyConvertingItemChanged(it)
                Log.d(TAG, "currentProcessingItem init: percent: " + it.percent + " status: " + it.state + " ID : " + it.id + " file name: " + it.getFileName())
            }
        }
    }

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

    private fun getProcessingItem(): ConvertingItem? {              // get 1 item da duoc set lai status de loading
        for (item in listConvertingItemData) {
            if (item.state == ConvertingState.PROGRESSING) return item
        }
        return null
    }

    private fun getWaitingItem(): ConvertingItem? {         // get item o trang thai waiting

        for (waitingItem in listConvertingItemData) {
            Log.d(TAG, "getWaitingItem: " + waitingItem.state)
            if (waitingItem.state == ConvertingState.WAITING) return waitingItem
        }
        return null
    }

    private fun processNextItem() {         // loadinng 1 item tiep theo neu list con

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

    private fun notifyConvertingItemChanged(item: ConvertingItem?) {        // update trang thai loading item
        currentProcessingItem.postValue(item)
        Log.d(TAG, "notifyConvertingItemChanged: percent " + item?.percent + "ID : " + item?.id)
    }

    private suspend fun processItem(item: ConvertingItem) = withContext(Dispatchers.Default) {      // thuc hien mix or mer or cut

        mainScope.launch {
            notifyConvertingItemChanged(null)

            item.state = ConvertingState.PROGRESSING
            notifyConvertingItemChanged(item)
            var audioResult: AudioCore? = null
            if (item is MergingConvertingItem) {
                val listAudioCore = ArrayList<AudioCore>()
                item.listAudioFiles.forEach {
                    listAudioCore.add(AudioCore(it.file, it.fileName, it.size, it.bitRate, it.duration, it.mimeType))
                }
                audioResult = ManagerFactory.getAudioCutter()
                    .merge(listAudioCore, item.mergingConfig.fileName, item.mergingConfig.audioFormat, item.mergingConfig.pathFolder)
            }

            if (item is MixingConvertingItem) {
                currentProcessingItem.postValue(item)

                val audioCore1 = AudioCore(item.audioFile1.file, item.audioFile1.fileName, item.audioFile1.size, item.audioFile1.bitRate, item.audioFile1.duration, item.audioFile1.mimeType)
                val audioCore2 = AudioCore(item.audioFile2.file, item.audioFile2.fileName, item.audioFile2.size, item.audioFile2.bitRate, item.audioFile2.duration, item.audioFile2.mimeType)

                audioResult = ManagerFactory.getAudioCutter()
                    .mix(audioCore1, audioCore2, item.mixingConfig)
            }

            if (item is CuttingConvertingItem) {

                val audioCore = AudioCore(item.audioFile.file, item.audioFile.fileName, item.audioFile.size, item.audioFile.bitRate, item.audioFile.duration, item.audioFile.mimeType)
                audioResult = ManagerFactory.getAudioCutter().cut(audioCore, item.cuttingConfig)

            }

            synchronized(listConvertingItemData) {
                listConvertingItemData.remove(item)
            }

            if (audioResult != null) {                  // converting progress co thanh cong hay khong
                ManagerFactory.getAudioFileManager().buildAudioFile(audioResult.file.absolutePath) {
                    it?.let {
                        item.outputAudioFile = it
                        item.state = ConvertingState.SUCCESS
                        latestConvertingItem = item
                    }
                }
                item.state = ConvertingState.SUCCESS
                item.percent = 100
                notifyConvertingItemChanged(item)

//               item.outputAudioFile?.file?.delete()
//                item.state = ConvertingState.ERROR
//                notifyConvertingItemChanged(item)
//                latestConvertingItem = null


            } else {
                item.state = ConvertingState.ERROR
                notifyConvertingItemChanged(item)
                latestConvertingItem = null
                Log.d(TAG, "processItem: null")
            }

            // demo dong bo data voi luu tru thu muc trong mystudio
            listConvertingItems.postValue(listConvertingItemData)


            processNextItem()
        }
    }

    override fun cutAudio(audioFile: AudioFile, cuttingConfig: AudioCutConfig) {        // add them 1 item loai cut vao list

        if (!isMyServiceRunning(ResultService::class.java)) {
            notifyConvertingItemChanged(null)
            Intent(mContext, ResultService::class.java).also {
                it.putExtra(Constance.TYPE_AUDIO, CUT_AUDIO)
                mContext.bindService(it, serviceConnection, Context.BIND_AUTO_CREATE)
                Log.d(TAG, "bindService: ")
            }
        }
        currConvertingId++
        val item = CuttingConvertingItem(currConvertingId, ConvertingState.WAITING, 0, audioFile, cuttingConfig)
        synchronized(listConvertingItemData) {
            listConvertingItemData.add(item)
            latestConvertingItem = item
        }

        val processingItem = getProcessingItem()
        if (processingItem == null) {
            processNextItem()
        }
        listConvertingItems.postValue(listConvertingItemData)

    }

    override fun mixAudio(audioFile1: AudioFile, audioFile2: AudioFile, mixingConfig: AudioMixConfig) {

        if (!isMyServiceRunning(ResultService::class.java)) {
            notifyConvertingItemChanged(null)
            Intent(mContext, ResultService::class.java).also {
                it.putExtra(Constance.TYPE_AUDIO, MIX_AUDIO)
                mContext.bindService(it, serviceConnection, Context.BIND_AUTO_CREATE)
                Log.d(TAG, "bindService: ")
            }
        }
        currConvertingId++
        val item = MixingConvertingItem(currConvertingId, ConvertingState.WAITING, 0, audioFile1, audioFile2, mixingConfig)
        synchronized(listConvertingItemData) {
            listConvertingItemData.add(item)
            latestConvertingItem = item
        }

        val processingItem = getProcessingItem()
        if (processingItem == null) {
            processNextItem()
        }
        listConvertingItems.postValue(listConvertingItemData)

    }

    override fun mergeAudio(listAudioFiles: List<AudioFile>, mergingConfig: AudioMergingConfig) {

        if (!isMyServiceRunning(ResultService::class.java)) {
            notifyConvertingItemChanged(null)
            Intent(mContext, ResultService::class.java).also {
                it.putExtra(Constance.TYPE_AUDIO, MER_AUDIO)
                mContext.bindService(it, serviceConnection, Context.BIND_AUTO_CREATE)
                Log.d(TAG, "bindService: ")
            }
        }
        currConvertingId++
        val item = MergingConvertingItem(currConvertingId, ConvertingState.WAITING, 0, listAudioFiles, mergingConfig)
        synchronized(listConvertingItemData) {
            listConvertingItemData.add(item)
            latestConvertingItem = item
        }
        val processingItem = getProcessingItem()
        if (processingItem == null) {
            processNextItem()
        }
        listConvertingItems.postValue(listConvertingItemData)
    }

    override fun cancel(int: Int) {              // cancel 1 tien trinh loading
        mainScope.launch {
            val iterator: MutableIterator<ConvertingItem> = listConvertingItemData.iterator()
            while (iterator.hasNext()) {
                val value = iterator.next()
                if (value.id == int) {
                    when (value.state) {
                        ConvertingState.WAITING -> {
                            iterator.remove()
                        }
                        ConvertingState.PROGRESSING -> {
                            iterator.remove()
                            ManagerFactory.getAudioCutter().cancelTask()
                            mService?.cancelNotidication(int)
                        }
                        else -> {
                            // nothing
                        }
                    }
                }
            }
            if (listConvertingItemData.size > 0) {                      // khi cancel item cuoi cung thi phai set lai latestConvertingItem = list.size() -1
                latestConvertingItem = listConvertingItemData.get(listConvertingItemData.size - 1)
            } else {
                latestConvertingItem = null
            }
            listConvertingItems.postValue(listConvertingItemData)
        }
    }

    override fun getCurrentProcessingItem(): LiveData<ConvertingItem?> { // tra ra live data cho update progressbar
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

    override fun getLatestConvertingItem(): ConvertingItem? {   // tra lai item cuoi cung trong list           // do la thamm chieu nen data se tu dong bo
        return latestConvertingItem
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