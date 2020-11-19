package com.example.audiocutter.core.result

import android.app.ActivityManager
import android.content.*
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

    lateinit var mContext: Context
    val TAG = "giangtd"

    val CUT_AUDIO = 0
    val MER_AUDIO = 1
    val MIX_AUDIO = 2

    var mService: ResultService? = null
    var mIsBound: Boolean = false

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
        for (waitingItem in listConvertingItemData) {
            if (waitingItem.state == ConvertingState.PROGRESSING) return waitingItem
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
    }

    private suspend fun processItem(item: ConvertingItem) = withContext(Dispatchers.Default) {      // thuc hien mix or mer or cut
        Log.d("taih", "processItem cut")
        mainScope.launch {
            notifyConvertingItemChanged(null)

            item.state = ConvertingState.PROGRESSING
            notifyConvertingItemChanged(item)

            if (item is MergingConvertingItem) {
                val listAudioCore = ArrayList<AudioCore>()
                item.listAudioFiles.forEach {
                    listAudioCore.add(AudioCore(it.file, it.fileName, it.size, it.bitRate, it.duration, it.mimeType))
                }
                val audioResult = ManagerFactory.getAudioCutter()
                    .merge(listAudioCore, item.mergingConfig.fileName, item.mergingConfig.audioFormat, item.mergingConfig.pathFolder)
                val audioFile = AudioFile(audioResult.file, audioResult.fileName, audioResult.size, audioResult.bitRate, audioResult.time, Uri.parse(audioResult.file.toString()))
                if (Build.VERSION.SDK_INT < 29) {
                    audioFile.uri = addMediaStore(audioFile.file.absolutePath.toString())
                }
                item.outputAudioFile = audioFile
            }

            if (item is MixingConvertingItem) {
                currentProcessingItem.postValue(item)

                val audioCore1 = AudioCore(item.audioFile1.file, item.audioFile1.fileName, item.audioFile1.size, item.audioFile1.bitRate, item.audioFile1.duration, item.audioFile1.mimeType)
                val audioCore2 = AudioCore(item.audioFile2.file, item.audioFile2.fileName, item.audioFile2.size, item.audioFile2.bitRate, item.audioFile2.duration, item.audioFile2.mimeType)

                val audioResult = ManagerFactory.getAudioCutter()
                    .mix(audioCore1, audioCore2, item.mixingConfig)
                val audioFile = AudioFile(audioResult.file, audioResult.fileName, audioResult.size, audioResult.bitRate, audioResult.time, Uri.parse(audioResult.file.toString()))
                if (Build.VERSION.SDK_INT < 29) {
                    audioFile.uri = addMediaStore(audioFile.file.absolutePath.toString())
                }
                item.outputAudioFile = audioFile
            }

            if (item is CuttingConvertingItem) {

                val audioCore = AudioCore(item.audioFile.file, item.audioFile.fileName, item.audioFile.size, item.audioFile.bitRate, item.audioFile.duration, item.audioFile.mimeType)
                val audioResult = ManagerFactory.getAudioCutter().cut(audioCore, item.cuttingConfig)

                val audioFile = AudioFile(audioResult.file, audioResult.fileName, audioResult.size, audioResult.bitRate, audioResult.time, Uri.parse(audioResult.file.toString()))
                if (Build.VERSION.SDK_INT < 29) {
                    audioFile.uri = addMediaStore(audioFile.file.absolutePath.toString())
                }
                item.outputAudioFile = audioFile        // gan lai audio file tu audioresult duoc tra ve sau khi cutting cho ConvertingItem
            }

            synchronized(listConvertingItemData) {
                listConvertingItemData.remove(item)
            }
            // demo dong bo data voi luu tru thu muc trong mystudio
            listConvertingItems.postValue(listConvertingItemData)
            notifyConvertingItemChanged(item)

            processNextItem()
        }
    }

    private fun addMediaStore(filePath: String): Uri? {
        val resolver: ContentResolver = RingtonManagerImpl.mContext.getContentResolver()
        val file = File(filePath)
        if (file.exists()) {
            val values = ContentValues()
            values.put(MediaStore.Audio.AudioColumns.DISPLAY_NAME, file.name)
            values.put(MediaStore.Audio.AudioColumns.DATA, file.absolutePath)
            values.put(MediaStore.Audio.AudioColumns.TITLE, file.name)
            values.put(MediaStore.Audio.AudioColumns.SIZE, file.length())
            values.put(MediaStore.Audio.AudioColumns.MIME_TYPE, "audio/*")
            return resolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values)
        }
        return null
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
        } else {

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
        } else {

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
        } else {

        }
        listConvertingItems.postValue(listConvertingItemData)
    }

    override fun cancel(id: Int) {              // cancel 1 tien trinh loading
        mainScope.launch {
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
            if (listConvertingItemData.size > 0) {                      // khi cancel item cuoi cung thi phai set lai latestConvertingItem = list.size() -1
                latestConvertingItem = listConvertingItemData.get(listConvertingItemData.size - 1)
            } else {
                latestConvertingItem = null
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