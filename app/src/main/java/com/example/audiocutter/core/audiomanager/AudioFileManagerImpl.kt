package com.example.audiocutter.core.audiomanager

import android.content.ContentValues
import android.content.Context
import android.database.ContentObserver
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.*
import com.example.audiocutter.core.manager.AudioFileManager
import com.example.audiocutter.core.manager.BuildAudioCompleted
import com.example.audiocutter.core.manager.ManagerFactory
import com.example.audiocutter.objects.AudioFile
import com.example.audiocutter.objects.AudioFileScans
import com.example.audiocutter.objects.StateLoad
import com.example.audiocutter.permissions.PermissionManager
import com.example.audiocutter.util.Utils
import com.example.core.core.AudioCutter
import com.example.core.utils.FileUtil
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import java.io.File
import java.util.concurrent.locks.ReentrantLock
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.HashSet


object AudioFileManagerImpl : AudioFileManager {
    private val TAG = "AudioFileManagerImpl"
    private val listAllAudioData = ArrayList<AudioFile>()
    private val filePathMapAudioFile = HashMap<String, AudioFile>()
    private val uriPathSet = HashSet<String>()

    private const val APP_FOLDER_NAME = "AudioCutter"
    private const val CUTTING_FOLDER_NAME = "cutter"
    private const val MERGING_FOLDER_NAME = "merger"
    private const val MIXING_FOLDER_NAME = "mixer"
    private val APP_FOLDER_PATH = "${Environment.getExternalStorageDirectory()}/${APP_FOLDER_NAME}"
    lateinit var mContext: Context
    private var initialized = false
    private var listAllAudios = MutableLiveData<AudioFileScans>()
    private lateinit var audioCutter: AudioCutter
    private var audioFileManagerScope = CoroutineScope(Dispatchers.Default)
    private val listCuttingAudios = MutableLiveData<AudioFileScans>()
    private val listMeringAudios = MutableLiveData<AudioFileScans>()
    private val listMixingAudios = MutableLiveData<AudioFileScans>()

    private val lock = ReentrantLock()


    override fun init(appContext: Context) {
        if (PermissionManager.hasStoragePermission()) {
            createNecessaryFolders()
            audioCutter = ManagerFactory.getAudioCutter()
            if (initialized) {
                return
            }
            initialized = true
            mContext = appContext.applicationContext
            notifyDiskChanged()
            registerContentObserVerDeleted()
        } else {
            initialized = false
        }
    }

    private fun createNecessaryFolders() {
        if (createFolder(APP_FOLDER_PATH)) {
            createFolder("$APP_FOLDER_PATH/$CUTTING_FOLDER_NAME")
            createFolder("$APP_FOLDER_PATH/$MERGING_FOLDER_NAME")
            createFolder("$APP_FOLDER_PATH/$MIXING_FOLDER_NAME")
        }
    }


    private fun createFolder(folderPath: String): Boolean {
        val appFolder = File(folderPath)
        if (!appFolder.exists()) {
            return appFolder.mkdirs()
        }
        return true
    }

    private suspend fun filterAudioByFolder(
        listAudioFiles: List<AudioFile>,
        folderFilter: Folder
    ): List<AudioFile> {
        return coroutineScope {
            val tmpList = ArrayList<AudioFile>()
            run list@{
                listAudioFiles.forEach {
                    if (!isActive) {
                        return@list
                    }
                    val folder = checkFolder(it.file.absolutePath)
                    if (folder == folderFilter) {
                        tmpList.add(it)
                    }
                }
            }


            tmpList
        }

    }

    private suspend fun queryMediaStore(filterFunc: (String, String, Long) -> Unit) =
        coroutineScope {
            val resolver = mContext.contentResolver
            val projection = arrayOf(
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DATE_ADDED,
                MediaStore.Audio.Media._ID
            )
            val cursor = resolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                null
            )
            try {
                cursor?.let {

                    val clData = it.getColumnIndex(projection[0])
                    val clDateAdded = it.getColumnIndex(projection[1])
                    val clID = it.getColumnIndex(projection[2])
                    var hasRow = it.moveToFirst()
                    while (isActive && hasRow) {
                        val filePath = it.getString(clData)
                        val id = it.getString(clID)
                        filterFunc(id, filePath, it.getLong(clDateAdded))
                        hasRow = it.moveToNext()
                    }
                }
            } finally {
                cursor?.close()
            }
        }

    private suspend fun scan() = coroutineScope {
        Log.d(TAG, "start scanning")
        val startTime = System.currentTimeMillis()
        changeStateLoading()
        val listMergingAudio = ArrayList<AudioFile>()
        val listCuttingAudio = ArrayList<AudioFile>()
        val listMixingAudio = ArrayList<AudioFile>()
        withLock {
            listAllAudioData.clear()
            uriPathSet.clear()
            queryMediaStore { mediaId, filePath, dateStr ->
                val file = File(filePath)
                Log.d(TAG, "scanning file ${filePath}")
                if (file.exists()) {
                    val audioFile = readOrGetCacheAudioFile(filePath, mediaId, dateStr)
                    audioFile?.let {
                        listAllAudioData.add(it)
                        uriPathSet.add(it.uri.toString())
                        val folder = checkFolder(it.getFilePath())
                        when (folder) {
                            Folder.TYPE_CUTTER -> {
                                listCuttingAudio.add(it)
                            }
                            Folder.TYPE_MERGER -> {
                                listMergingAudio.add(it)
                            }
                            Folder.TYPE_MIXER -> {
                                listMixingAudio.add(it)
                            }
                            else -> {
                            }
                        }
                    }
                }
            }
        }
        if (isActive) {
            listAllAudios.postValue(AudioFileScans(listAllAudioData, StateLoad.LOADDONE))
            listCuttingAudios.postValue(AudioFileScans(listCuttingAudio, StateLoad.LOADDONE))
            listMeringAudios.postValue(AudioFileScans(listMergingAudio, StateLoad.LOADDONE))
            listMixingAudios.postValue(AudioFileScans(listMixingAudio, StateLoad.LOADDONE))
        }

    }

    private fun readOrGetCacheAudioFile(
        filePath: String,
        mediaId: String,
        modified: Long
    ): AudioFile? {
        val cachedAudioFile = filePathMapAudioFile.get(filePath)
        if (cachedAudioFile?.modified != modified) {
            synchronized(this) {
                val audioInfo = FileUtil.getAudioInfo(filePath)
                audioInfo?.let {
                    filePathMapAudioFile.put(
                        filePath,
                        Utils.convertToAudioFile(
                            it,
                            modified,
                            Uri.parse(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI.toString() + File.separator + mediaId)
                        )
                    )
                }
            }


        }
        return filePathMapAudioFile.get(filePath)
    }

    private fun checkFolder(filePath: String): Folder? {
        if (filePath.contains(getFolderPath(Folder.TYPE_MERGER))) {
            return Folder.TYPE_MERGER
        }
        if (filePath.contains(getFolderPath(Folder.TYPE_MIXER))) {
            return Folder.TYPE_MIXER
        }
        if (filePath.contains(getFolderPath(Folder.TYPE_CUTTER))) {
            return Folder.TYPE_CUTTER
        }
        return null
    }

    override fun hasAudioFileWithUri(uri: String): Boolean {
        return uriPathSet.contains(uri)
    }

    override fun findAllAudioFiles(): LiveData<AudioFileScans> {
        return listAllAudios
    }

    override fun deleteFile(listAudioFile: List<AudioFile>, typeFile: Folder): Boolean {
        var rs = false
        val resolver = mContext.contentResolver
        var rowDeleted: Int
        try {
            listAudioFile.forEach { audioFile ->
                if (audioFile.file.exists() && audioFile.uri != null) {
                    Log.d(TAG, "deleteFile: ${audioFile.uri}")
                    rowDeleted = resolver.delete(audioFile.uri!!, null, null)
                    val result = audioFile.file.delete()
                    if (rowDeleted != 0 && result) {
                        rs = true
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            rs = false
        }
        return rs
    }

    override fun buildAudioFile(filePath: String, listener: BuildAudioCompleted) {
        if (File(filePath).exists()) {
            insertToMediaStore(filePath, listener)
        } else {
            listener(null)
        }
    }

    private fun insertToMediaStore(filePath: String, listener: BuildAudioCompleted) {
        MediaScannerConnection.scanFile(
            mContext,
            arrayOf(filePath),
            null
        ) { s, uri ->
            audioFileManagerScope.launch {
                synchronized(this) {
                    val audioInfo = FileUtil.getAudioInfo(filePath)
                    if (audioInfo != null) {
                        val audioFile = Utils.convertToAudioFile(
                            audioInfo,
                            System.currentTimeMillis(),
                            uri
                        )
                        withLock {
                            val oldAudioFile = findAudioFile(filePath)
                            oldAudioFile?.let {
                                withLock {
                                    listAllAudioData.remove(it)
                                }
                            }
                            listAllAudioData.add(audioFile)
                            filePathMapAudioFile.put(audioFile.getFilePath(), audioFile)
                            listAllAudios.postValue(
                                AudioFileScans(
                                    listAllAudioData,
                                    StateLoad.LOADDONE
                                )
                            )
                        }
                        listener(audioFile)
                    } else {
                        listener(null)
                    }
                }

            }
        }
    }


    override fun findAudioFile(filePath: String): AudioFile? {

        withLock {
            listAllAudioData.forEach {
                if (it.file.absolutePath == filePath) {
                    return it
                }
            }
        }
        return null
    }


    private fun registerContentObserVerDeleted() {
        mContext.contentResolver.registerContentObserver(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            true,
            object : ContentObserver(Handler(Looper.getMainLooper())) {
                override fun onChange(selfChange: Boolean, uri: Uri?) {
                    notifyDiskChanged()
                }
            }
        )
    }

    override fun getFolderPath(typeFile: Folder): String {
        createNecessaryFolders()
        var pathParent = ""
        try {
            pathParent = when (typeFile) {
                Folder.TYPE_CUTTER -> "$APP_FOLDER_PATH/${CUTTING_FOLDER_NAME}"
                Folder.TYPE_MERGER -> "$APP_FOLDER_PATH/${MERGING_FOLDER_NAME}"
                Folder.TYPE_MIXER -> "$APP_FOLDER_PATH/${MIXING_FOLDER_NAME}"
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return pathParent
    }


    override fun getListAudioFileByType(typeFile: Folder): LiveData<AudioFileScans> {
        return when (typeFile) {
            Folder.TYPE_MIXER -> {
                listMixingAudios
            }
            Folder.TYPE_CUTTER -> {
                listCuttingAudios
            }
            Folder.TYPE_MERGER -> {
                listMeringAudios
            }
        }
    }

    override fun renameToFileAudio(
        newName: String,
        audioFile: AudioFile,
        typeFile: Folder
    ): Boolean {
        try {
            val subPath = when (typeFile) {
                Folder.TYPE_MIXER -> {
                    "$APP_FOLDER_PATH/$MIXING_FOLDER_NAME"
                }
                Folder.TYPE_MERGER -> {
                    "$APP_FOLDER_PATH/$MERGING_FOLDER_NAME"
                }
                Folder.TYPE_CUTTER -> {
                    "$APP_FOLDER_PATH/$CUTTING_FOLDER_NAME"
                }
            }
            val file = audioFile.file
            val pathNew = "$subPath/$newName.${audioFile.mimeType}"
            val fileNew = File(pathNew)
            file.renameTo(fileNew)
            MediaScannerConnection.scanFile(
                mContext,
                arrayOf(fileNew.absolutePath),
                null
            ) { s, uri ->
                notifyDiskChanged()
            }

            return true
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d(TAG, "renameToFileAudio error ${e.message}")
            return false
        }
    }

    private fun getAllFileName(folderType: Folder): HashSet<String> {
        val folderPath = getFolderPath(folderType)
        val folder = File(folderPath)
        val fileNameHash = HashSet<String>()
        if (folder.exists()) {
            folder.listFiles()?.forEach {
                if (it.name.contains(".")) {
                    fileNameHash.add(it.name.substring(0, (it.name).lastIndexOf(".")))
                } else {
                    fileNameHash.add(it.name)
                }
            }
        }
        return fileNameHash

    }

    override fun checkFileNameDuplicate(name: String, typeFile: Folder): Boolean {
        val fileNameHash = getAllFileName(typeFile)
        var result = false
        fileNameHash.forEach {
            if (name == it) {
                result = true
            }
        }
        return result
    }


    private fun changeStateLoading() {
        var audioFileScan = listMixingAudios.value
        if (audioFileScan == null || audioFileScan.state != StateLoad.LOADING) {
            listMixingAudios.postValue(AudioFileScans(ArrayList(), StateLoad.LOADING))
        }
        audioFileScan = listCuttingAudios.value
        if (audioFileScan == null || audioFileScan.state != StateLoad.LOADING) {
            listCuttingAudios.postValue(AudioFileScans(ArrayList(), StateLoad.LOADING))
        }
        audioFileScan = listMeringAudios.value
        if (audioFileScan == null || audioFileScan.state != StateLoad.LOADING) {
            listMeringAudios.postValue(AudioFileScans(ArrayList(), StateLoad.LOADING))
        }
        audioFileScan = listAllAudios.value
        if (audioFileScan == null || audioFileScan.state != StateLoad.LOADING) {
            listAllAudios.postValue(AudioFileScans(ArrayList(), StateLoad.LOADING))
        }

    }


    private val scanFileChannel = Channel<Any>(Channel.CONFLATED)
    private var scanFileJob: Job? = null
    private fun notifyDiskChanged() {
        audioFileManagerScope.launch {
            scanFileChannel.send(true)
        }
    }

    init {
        audioFileManagerScope.launch {
            while (true) {
                val signal = scanFileChannel.receive()
                scanFileJob?.let {
                    if (!it.isCompleted) {
                        Log.d(TAG, "waiting scanning")
                        it.cancelAndJoin()
                    }
                }
                scanFileJob = audioFileManagerScope.launch {
                    scan()
                }
            }
        }
    }

    private inline fun withLock(func: () -> Unit) {
        lock.lock()
        try {
            func()
        } finally {
            lock.unlock()
        }
    }


}

