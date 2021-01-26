package com.example.audiocutter.core.audiomanager

import android.content.Context
import android.database.ContentObserver
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
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

fun List<AudioFile>.isSame(other: List<AudioFile>?): Boolean {
    if (other == null) {
        return false
    }
    if (other.size != this.size) {
        return false
    }
    for (i in 0 until other.size) {
        if (this[i] != other[i]) {
            return false
        }
    }
    return true
}

object AudioFileManagerImpl : AudioFileManager {
    private val TAG = "AudioFileManagerImpl"
    private val listAllAudioData = ArrayList<AudioFile>()
    private val filePathMapAudioFile = HashMap<String, AudioFile>()
    private val uriPathSet = HashSet<String>()

    private const val APP_FOLDER_NAME = "AudioCutter"
    private const val CUTTING_FOLDER_NAME = "cutter"
    private const val MERGING_FOLDER_NAME = "merger"
    private const val MIXING_FOLDER_NAME = "mixer"
    private val REL_APP_FOLDER_PATH = Environment.DIRECTORY_DOWNLOADS + File.separator + APP_FOLDER_NAME
    private val ABS_APP_FOLDER_PATH = Environment.getExternalStorageDirectory().absolutePath+File.separator +REL_APP_FOLDER_PATH
    lateinit var mContext: Context
    private var initialized = false
    private var listAllAudios = MutableLiveData<AudioFileScans>()
    private lateinit var audioCutter: AudioCutter
    private var audioFileManagerScope = CoroutineScope(Dispatchers.Default)
    private val listCuttingAudios = MutableLiveData<AudioFileScans>()
    private val listMeringAudios = MutableLiveData<AudioFileScans>()
    private val listMixingAudios = MutableLiveData<AudioFileScans>()

    private val lock = ReentrantLock()
    private var isFirstTimeToScanning = true

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
        if (createFolder(ABS_APP_FOLDER_PATH)) {
            createFolder("$ABS_APP_FOLDER_PATH/$CUTTING_FOLDER_NAME")
            createFolder("$ABS_APP_FOLDER_PATH/$MERGING_FOLDER_NAME")
            createFolder("$ABS_APP_FOLDER_PATH/$MIXING_FOLDER_NAME")
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
                        Log.d(TAG, "queryMediaStore: $filePath")
                        val id = it.getString(clID)
                        filterFunc(id, filePath, it.getLong(clDateAdded) * 1000)
                        hasRow = it.moveToNext()
                    }
                }
            } finally {
                cursor?.close()
            }
        }

    private suspend fun scan() = coroutineScope {
        Log.d(TAG, "start scanning")
        val oldListAudios = listAllAudios.value?.listAudioFiles
        changeStateLoading()
        val listMergingAudio = ArrayList<AudioFile>()
        val listCuttingAudio = ArrayList<AudioFile>()
        val listMixingAudio = ArrayList<AudioFile>()
        withLock {
            listAllAudioData.clear()
            uriPathSet.clear()
            queryMediaStore { mediaId, filePath, dateStr ->
                val file = File(filePath)
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
            if (isActive) {
                if (!listAllAudioData.isSame(oldListAudios)) {
                    isFirstTimeToScanning = false
                    changeStateLoadDone(
                        listAllAudioData,
                        listCuttingAudio,
                        listMergingAudio,
                        listMixingAudio
                    )
                }
            }
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
        if (filePath.contains(getRelFolderPath(Folder.TYPE_MERGER))) {
            return Folder.TYPE_MERGER
        }
        if (filePath.contains(getRelFolderPath(Folder.TYPE_MIXER))) {
            return Folder.TYPE_MIXER
        }
        if (filePath.contains(getRelFolderPath(Folder.TYPE_CUTTER))) {
            return Folder.TYPE_CUTTER
        }
        return null
    }

    override fun hasAudioFileWithUri(uri: String): Boolean {
        return uriPathSet.contains(uri)
    }

    override fun getAudioFiles(): LiveData<AudioFileScans> {
        return listAllAudios
    }

    override fun deleteFile(listAudioFile: List<AudioFile>, typeFile: Folder): Boolean {

        val resolver = mContext.contentResolver
        try {
            listAudioFile.forEach { audioFile ->
                if (audioFile.file.exists() && audioFile.uri != null) {
                    if (audioFile.file.delete()) {
                        resolver.delete(audioFile.uri!!, null, null)
                    } else {
                        notifyDiskChanged()
                        return false
                    }

                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        notifyDiskChanged()
        return true
    }

    override fun buildAudioFile(filePath: String, listener: BuildAudioCompleted) {
        if (File(filePath).exists()) {
            insertToMediaStore(filePath, listener)
        } else {
            listener(null)
        }
    }

    private fun insertToMediaStore(filePath: String, listener: BuildAudioCompleted) {
        MediaScannerConnection.scanFile(mContext, arrayOf(filePath), null) { s, uri ->
            audioFileManagerScope.launch {
                synchronized(this) {
                    val audioInfo = FileUtil.getAudioInfo(filePath)
                    if (audioInfo != null) {
                        val audioFile =
                            Utils.convertToAudioFile(audioInfo, System.currentTimeMillis(), uri)
                        withLock {
                            val oldAudioFile = findAudioFile(audioInfo.filePath)
                            if (oldAudioFile != null) {
                                oldAudioFile.copy(audioFile)
                            } else {
                                listAllAudioData.add(audioFile)
                                changeStateLoadDone()
                            }
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
            })
    }

    override fun getFolderPath(typeFile: Folder): String {
        createNecessaryFolders()
        var pathParent = ""
        try {
            pathParent = when (typeFile) {
                Folder.TYPE_CUTTER -> "$ABS_APP_FOLDER_PATH${File.separator}${CUTTING_FOLDER_NAME}"
                Folder.TYPE_MERGER -> "$ABS_APP_FOLDER_PATH${File.separator}${MERGING_FOLDER_NAME}"
                Folder.TYPE_MIXER -> "$ABS_APP_FOLDER_PATH${File.separator}${MIXING_FOLDER_NAME}"
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return pathParent
    }

    override fun getRelFolderPath(typeFile: Folder): String {
       return when (typeFile) {
            Folder.TYPE_CUTTER -> "$REL_APP_FOLDER_PATH${File.separator}${CUTTING_FOLDER_NAME}"
            Folder.TYPE_MERGER -> "$REL_APP_FOLDER_PATH${File.separator}${MERGING_FOLDER_NAME}"
            Folder.TYPE_MIXER -> "$REL_APP_FOLDER_PATH${File.separator}${MIXING_FOLDER_NAME}"
        }
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
                     "$ABS_APP_FOLDER_PATH/$MIXING_FOLDER_NAME"
                 }
                 Folder.TYPE_MERGER -> {
                     "$ABS_APP_FOLDER_PATH/$MERGING_FOLDER_NAME"
                 }
                 Folder.TYPE_CUTTER -> {
                     "$ABS_APP_FOLDER_PATH/$CUTTING_FOLDER_NAME"
                 }
             }
             audioFile.uri?.let {
                 val file = audioFile.file
                 val pathNew = "$subPath/$newName.${audioFile.mimeType}"
                 val fileNew = File(pathNew)
                 if (file.renameTo(fileNew)) {
                     MediaScannerConnection.scanFile(
                         mContext,
                         arrayOf(fileNew.absolutePath),
                         null
                     ) { s, uri ->
                         notifyDiskChanged()
                     }
                 }
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

    private fun changeStateLoadDone(
        listAllAudioData: ArrayList<AudioFile>,
        listCuttingAudio: ArrayList<AudioFile>,
        listMergingAudio: ArrayList<AudioFile>,
        listMixingAudio: ArrayList<AudioFile>
    ) {
        listAllAudios.postValue(AudioFileScans(ArrayList(listAllAudioData), StateLoad.LOADDONE))
        listCuttingAudios.postValue(AudioFileScans(ArrayList(listCuttingAudio), StateLoad.LOADDONE))
        listMeringAudios.postValue(AudioFileScans(ArrayList(listMergingAudio), StateLoad.LOADDONE))
        listMixingAudios.postValue(AudioFileScans(ArrayList(listMixingAudio), StateLoad.LOADDONE))
    }

    private fun changeStateLoadDone() {
        withLock {
            val listMergingAudio = ArrayList<AudioFile>()
            val listCuttingAudio = ArrayList<AudioFile>()
            val listMixingAudio = ArrayList<AudioFile>()
            listAllAudioData.forEach {
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
            changeStateLoadDone(
                listAllAudioData,
                listCuttingAudio,
                listMergingAudio,
                listMixingAudio
            )
        }

    }

    private fun changeStateLoading() {
        if (isFirstTimeToScanning) {
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

