package com.example.audiocutter.core.result

import android.app.ActivityManager
import android.content.*
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.example.audiocutter.core.audiomanager.Folder
import com.example.audiocutter.core.manager.AudioEditorManager
import com.example.audiocutter.core.manager.BuildAudioCompleted
import com.example.audiocutter.core.manager.ManagerFactory
import com.example.audiocutter.functions.mystudio.Constance
import com.example.audiocutter.functions.resultscreen.objects.*
import com.example.audiocutter.functions.resultscreen.services.ResultService
import com.example.audiocutter.objects.AudioFile
import com.example.audiocutter.util.Utils
import com.example.core.core.*
import kotlinx.coroutines.*
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException


object AudioEditorManagerlmpl : AudioEditorManager {

    private lateinit var mContext: Context
    private val TAG = "giangtd"
    private val listConvertingItem = ArrayList<ConvertingItem>()
    private val listConvertingItemsLiveData = MutableLiveData<List<ConvertingItem>>()  // list chung
    private var currConvertingId = 0
    private val mainScope = MainScope()
    private val currentProcessingItemLiveData = MutableLiveData<ConvertingItem>()
    private var latestConvertingItem: ConvertingItem? = null   //phan tu cuoi cung
    private var lastItemLiveData = MutableLiveData<ConvertingItem>()

    var convertingState: ConvertingState = ConvertingState.WAITING
    fun init(context: Context) {
        mContext = context

        ManagerFactory.getAudioCutter().getAudioMergingInfo().observeForever { audioMering ->
            when (audioMering.state) {
                FFMpegState.IDE -> {
                    convertingState = ConvertingState.WAITING
                    currentProcessingItemLiveData.value?.let {
                        it.percent = audioMering.percent
                        it.state = convertingState
//                        notifyConvertingItemChanged(it)
                        currentProcessingItemLiveData.value = it
                    }
                }
                FFMpegState.RUNNING -> {
                    convertingState = ConvertingState.PROGRESSING
                    currentProcessingItemLiveData.value?.let {
                        it.percent = audioMering.percent
                        it.state = convertingState
//                        notifyConvertingItemChanged(it)
                        currentProcessingItemLiveData.value = it
                    }
                }
                FFMpegState.CANCEL -> {
                    convertingState = ConvertingState.CANCEL
                    currentProcessingItemLiveData.value?.let {
                        it.state = convertingState
//                        notifyConvertingItemChanged(it)
                        currentProcessingItemLiveData.value = it
                    }
                }
                FFMpegState.FAIL -> {
//                    convertingState = ConvertingState.ERROR
                }
                FFMpegState.SUCCESS -> {
//                    convertingState = ConvertingState.SUCCESS
                }
            }
        }
    }

    private fun getProcessingItem(): ConvertingItem? {              // get 1 item da duoc set lai status de loading
        for (item in listConvertingItem) {
            if (item.state == ConvertingState.PROGRESSING) return item
        }
        return null
    }

    private fun getWaitingItem(): ConvertingItem? {         // get item o trang thai waiting
        for (waitingItem in listConvertingItem) {
            if (waitingItem.state == ConvertingState.WAITING) return waitingItem
        }
        return null
    }

    private fun processNextItem() {         // loadinng 1 item tiep theo neu list con
        val waitingItem = getWaitingItem()
        if (waitingItem == null) {      // khi khong con item nao thi bo service di
            if (isMyServiceRunning(ResultService::class.java)) {
                Intent(mContext, ResultService::class.java).also { intent ->
                    mContext.stopService(intent)
                }
            }
        } else {
            waitingItem.state = ConvertingState.PROGRESSING
            mainScope.launch {
                processItem(waitingItem)
            }
        }
    }

//    private fun notifyConvertingItemChanged(item: ConvertingItem?) {        // update trang thai loading item
//        currentProcessingItemLiveData.postValue(item)
//    }

    private fun convertAudioFileToAudioCore(audioFile: AudioFile): AudioCore {
        return AudioCore(audioFile.file, audioFile.fileName, audioFile.size, audioFile.bitRate, audioFile.duration, audioFile.mimeType)
    }

    private suspend fun processItem(item: ConvertingItem) = withContext(Dispatchers.Default) {      // thuc hien mix or mer or cut
        clearCacheFolder()

        val intent = Intent(mContext, ResultService::class.java)
        intent.setAction(Constance.SERVICE_ACTION_BUILD_FORGROUND_SERVICE)
        mContext.startService(intent)

//        notifyConvertingItemChanged(null)
//        item.state = ConvertingState.PROGRESSING
//        notifyConvertingItemChanged(item)

        withContext(Dispatchers.Main){
            currentProcessingItemLiveData.value = null
            item.state = ConvertingState.PROGRESSING
            currentProcessingItemLiveData.value = item
        }

        var audioResult: AudioCore? = null
        when (item) {
            is MergingConvertingItem -> {
                val listAudioCore = item.listAudioFiles.map { convertAudioFileToAudioCore(it) }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    // change folder to cache folder after using media store to copy to dest folder
                    val configCopy = item.mergingConfig.copy()
                    configCopy.absFolderPath = mContext.cacheDir.absolutePath
                    audioResult = ManagerFactory.getAudioCutter()
                        .merge(listAudioCore, item.mergingConfig.fileName, item.mergingConfig.audioFormat, configCopy.absFolderPath)
                    audioResult?.let {
                        if (!moveFileFromCacheFolerToDestFolder(it, configCopy.relFolderPath)) {
                            audioResult = null
                        } else {
                            it.file = File(item.mergingConfig.absFolderPath + File.separator + it.file.name)
                        }
                    }
                } else {
                    audioResult = ManagerFactory.getAudioCutter()
                        .merge(listAudioCore, item.mergingConfig.fileName, item.mergingConfig.audioFormat, item.mergingConfig.absFolderPath)
                }
            }
            is MixingConvertingItem -> {
                val audioCore1 = convertAudioFileToAudioCore(item.audioFile1)
                val audioCore2 = convertAudioFileToAudioCore(item.audioFile2)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    // change folder to cache folder after using media store to copy to dest folder
                    val configCopy = item.mixingConfig.copy()
                    configCopy.absFolderPath = mContext.cacheDir.absolutePath
                    audioResult = ManagerFactory.getAudioCutter()
                        .mix(audioCore1, audioCore2, configCopy)
                    audioResult?.let {
                        if (!moveFileFromCacheFolerToDestFolder(it, configCopy.relFolderPath)) {
                            audioResult = null
                        } else {
                            it.file = File(item.mixingConfig.absFolderPath + File.separator + it.file.name)
                        }
                    }
                } else {
                    audioResult = ManagerFactory.getAudioCutter()
                        .mix(audioCore1, audioCore2, item.mixingConfig)
                }
            }
            is CuttingConvertingItem -> {
                val audioCore = convertAudioFileToAudioCore(item.audioFile)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    // change folder to cache folder after using media store to copy to dest folder
                    val configCopy = item.cuttingConfig.copy()
                    configCopy.absFolderPath = mContext.cacheDir.absolutePath
                    audioResult = ManagerFactory.getAudioCutter().cut(audioCore, configCopy)
                    audioResult?.let {
                        if (!moveFileFromCacheFolerToDestFolder(it, configCopy.relFolderPath)) {
                            audioResult = null
                        } else {
                            it.file = File(item.cuttingConfig.absFolderPath + File.separator + it.file.name)
                        }
                    }
                } else {
                    audioResult = ManagerFactory.getAudioCutter().cut(audioCore, item.cuttingConfig)
                }

            }
        }

        synchronized(listConvertingItem) {
            listConvertingItem.remove(item)
        }

        audioResult?.let {      // thanh cong
            ManagerFactory.getAudioFileManager().buildAudioFile(it.file.absolutePath) {
                if (it != null) {
                    item.outputAudioFile = it
                    item.state = ConvertingState.SUCCESS
                    onConvertingItemDetached(item)

                    currentProcessingItemLiveData.value = item
                    currentProcessingItemLiveData.value = null

                    listConvertingItemsLiveData.postValue(listConvertingItem)
//                    listConvertingItemsLiveData.value = listConvertingItem
                    processNextItem()

                } else {
                    item.outputAudioFile = it
                    item.state = ConvertingState.ERROR
                    onConvertingItemDetached(item)

                    currentProcessingItemLiveData.value = item
                    currentProcessingItemLiveData.value = null

                    listConvertingItemsLiveData.postValue(listConvertingItem)
//                    listConvertingItemsLiveData.value = listConvertingItem
                    processNextItem()
                }
            }
        } ?: run {    // error or cancel
            withContext(Dispatchers.Main) {
                if (convertingState == ConvertingState.ERROR) {
                    item.state = ConvertingState.ERROR

                } else {
                    item.state = ConvertingState.CANCEL
                }

                currentProcessingItemLiveData.value = item
                currentProcessingItemLiveData.value = null

                latestConvertingItem = null
                listConvertingItemsLiveData.postValue(listConvertingItem)
//                listConvertingItemsLiveData.value = listConvertingItem
                processNextItem()
            }
        }

    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun moveFileFromCacheFolerToDestFolder(audioCore: AudioCore, relFolder: String): Boolean {
        val tmpAudioFilePath = mContext.cacheDir.absolutePath + "/" + audioCore.file.name
        val fileName = audioCore.file.name

        val resolver = mContext.contentResolver
        val values = ContentValues()
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
        values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/*")
        values.put(MediaStore.MediaColumns.RELATIVE_PATH, relFolder)
        val uri = resolver.insert(MediaStore.Files.getContentUri("external"), values)
        uri?.let {
            val outputStream = resolver.openOutputStream(it)
            var reader: FileInputStream? = null
            try {
                reader = FileInputStream(tmpAudioFilePath)
                val byteArray = ByteArray(2048)

                var byteCount = reader.read(byteArray)
                while (byteCount != -1) {
                    outputStream?.write(byteArray, 0, byteCount)
                    byteCount = reader.read(byteArray)
                }
                return true
            } catch (e: FileNotFoundException) {

            } finally {
                outputStream?.close()
                reader?.close()
                clearCacheFolder()
            }
        }
        return false
    }

    private fun clearCacheFolder() {
        val folder = mContext.cacheDir
        folder.listFiles()?.forEach {
            it.delete()
        }
    }

    override fun cutAudio(audioFile: AudioFile, cuttingConfig: AudioCutConfig) {        // add them 1 item loai cut vao list
        currConvertingId++
        val item = CuttingConvertingItem(currConvertingId, ConvertingState.WAITING, 0, audioFile, cuttingConfig, audioFile)
        synchronized(listConvertingItem) {
            listConvertingItem.add(item)
            Utils.addGeneratedName(Folder.TYPE_CUTTER, File(cuttingConfig.absFolderPath + File.separator + cuttingConfig.fileName))
            latestConvertingItem = item
//            lastItemLiveData.postValue(item)
            lastItemLiveData.value = item
        }

        val processingItem = getProcessingItem()
        if (processingItem == null) {
            processNextItem()
        }

//        listConvertingItemsLiveData.postValue(listConvertingItem)
        listConvertingItemsLiveData.value = listConvertingItem
    }

    override fun mixAudio(audioFile1: AudioFile, audioFile2: AudioFile, mixingConfig: AudioMixConfig) {
        currConvertingId++
        val item = MixingConvertingItem(currConvertingId, ConvertingState.WAITING, 0, audioFile1, audioFile2, mixingConfig, null)
        synchronized(listConvertingItem) {
            listConvertingItem.add(item)
            Utils.addGeneratedName(Folder.TYPE_MIXER, File(mixingConfig.absFolderPath + File.separator + mixingConfig.fileName))
            latestConvertingItem = item
//            lastItemLiveData.postValue(item)
            lastItemLiveData.value = item
        }

        val processingItem = getProcessingItem()
        if (processingItem == null) {
            processNextItem()
        }
//        listConvertingItemsLiveData.postValue(listConvertingItem)
        listConvertingItemsLiveData.value = listConvertingItem
    }

    override fun mergeAudio(listAudioFiles: List<AudioFile>, mergingConfig: AudioMergingConfig) {
        currConvertingId++
        val item = MergingConvertingItem(currConvertingId, ConvertingState.WAITING, 0, listAudioFiles, mergingConfig, null)
        synchronized(listConvertingItem) {
            listConvertingItem.add(item)
            Utils.addGeneratedName(Folder.TYPE_MERGER, File(mergingConfig.absFolderPath + File.separator + mergingConfig.fileName))
            latestConvertingItem = item
//            lastItemLiveData.postValue(item)
            lastItemLiveData.value = item
        }
        val processingItem = getProcessingItem()
        if (processingItem == null) {
            processNextItem()
        }
        Log.d(TAG, "mergeAudio: ")
//        listConvertingItemsLiveData.postValue(listConvertingItem)
        listConvertingItemsLiveData.value = listConvertingItem
    }


    private fun onConvertingItemDetached(convertingItem: ConvertingItem) {
        if (convertingItem is MixingConvertingItem) {
            Utils.removeGeneratedName(Folder.TYPE_MIXER, File(convertingItem.mixingConfig.absFolderPath + File.separator + convertingItem.mixingConfig.fileName))
        }
        if (convertingItem is CuttingConvertingItem) {
            Utils.removeGeneratedName(Folder.TYPE_CUTTER, File(convertingItem.cuttingConfig.absFolderPath + File.separator + convertingItem.cuttingConfig.fileName))
        }
        if (convertingItem is MergingConvertingItem) {
            Utils.removeGeneratedName(Folder.TYPE_MERGER, File(convertingItem.mergingConfig.absFolderPath + File.separator + convertingItem.mergingConfig.fileName))
        }
    }

    override fun cancel(id: Int) {              // cancel 1 tien trinh loading
        mainScope.launch {
            val iterator: MutableIterator<ConvertingItem> = listConvertingItem.iterator()
            while (iterator.hasNext()) {
                val value = iterator.next()
                if (value.id == id) {
                    onConvertingItemDetached(value)
                    when (value.state) {
                        ConvertingState.WAITING -> {
                            iterator.remove()
                        }
                        ConvertingState.PROGRESSING -> {
                            iterator.remove()
                            ManagerFactory.getAudioCutter().cancelTask()

                            val intent = Intent(mContext, ResultService::class.java)
                            intent.setAction(Constance.SERVICE_ACTION_CANCEL_NOTIFICATION)
                            intent.putExtra(Constance.SERVICE_ACTION_CANCEL_ID, id)
                            mContext.startService(intent)

                        }
                        else -> {
                            // nothing
                        }
                    }
                    break
                }
            }
            if (listConvertingItem.size > 0) {                      // khi cancel item cuoi cung thi phai set lai latestConvertingItem = list.size() -1
                latestConvertingItem = listConvertingItem.get(listConvertingItem.size - 1)
            } else {
                latestConvertingItem = null
            }
//            listConvertingItemsLiveData.postValue(listConvertingItem)
            listConvertingItemsLiveData.value = listConvertingItem
        }
    }

    override fun getCurrentProcessingItem(): LiveData<ConvertingItem?> { // tra ra live data cho update progressbar
        return currentProcessingItemLiveData
    }

    override fun getLastItem(): LiveData<ConvertingItem> {
        return lastItemLiveData
    }

    override fun refeshNotification() {
        val intent = Intent(mContext, ResultService::class.java)
        intent.setAction(Constance.SERVICE_ACTION_REFESHER_NOTIFICATION)
        mContext.startService(intent)
    }

    override fun getListCuttingItems(): LiveData<List<ConvertingItem>> {  // tra live data cho list pending cho cutting
        return Transformations.map(listConvertingItemsLiveData) {
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
        return Transformations.map(listConvertingItemsLiveData) {
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
        return Transformations.map(listConvertingItemsLiveData) {
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

    fun isMyServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = mContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager?
        manager?.let {
            for (service in it.getRunningServices(Int.MAX_VALUE)) {
                if (serviceClass.name == service.service.className) {
                    return true
                }
            }
        }
        return false
    }
}