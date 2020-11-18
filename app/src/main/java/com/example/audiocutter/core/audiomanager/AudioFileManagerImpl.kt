package com.example.audiocutter.core.audiomanager

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.ResolveInfo
import android.database.ContentObserver
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.audiocutter.core.manager.AudioFileManager
import com.example.audiocutter.core.manager.ManagerFactory
import com.example.audiocutter.functions.audiochooser.objects.ItemAppShare
import com.example.audiocutter.objects.AudioFile
import com.example.audiocutter.objects.AudioFileScans
import com.example.audiocutter.objects.StateLoad
import com.example.audiocutter.permissions.PermissionManager
import com.example.core.core.AudioCutter
import com.example.core.core.BitRate
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashSet


object AudioFileManagerImpl : AudioFileManager {
    private lateinit var listAllAudios: ArrayList<AudioFile>
    private lateinit var intent: Intent
    private const val APP_FOLDER_NAME = "AudioCutter"
    private const val CUTTING_FOLDER_NAME = "cutter"
    private const val MERGING_FOLDER_NAME = "merger"
    private const val MIXING_FOLDER_NAME = "mixer"
    private val APP_FOLDER_PATH = "${Environment.getExternalStorageDirectory()}/${APP_FOLDER_NAME}"

    enum class ScanningState {
        IDLE,
        RUNNING,
        WAITING_FOR_CANCELING
    }


    private var uri: Uri = Uri.parse("")
    private lateinit var audioFile: AudioFile
    private val SIZE_KB: Long = 1024L
    private val SIZE_MB = SIZE_KB * SIZE_KB
    private val SIZE_GB = SIZE_MB * SIZE_KB
    private val TAG = AudioFileManagerImpl::class.java.name
    lateinit var mContext: Context
    private lateinit var listAppShares: MutableList<ItemAppShare>
    private var initialized = false
    private var _listAllAudioFile = MutableLiveData<AudioFileScans>()
    private lateinit var audioCutter: AudioCutter
    private var scanningState = ScanningState.IDLE
    val listAllAudioFile: LiveData<AudioFileScans>
        get() = _listAllAudioFile
    private val audioFileObserver = AudioFileObserver(Handler())


    private val _listCuttingAudios = MutableLiveData<AudioFileScans>()
    private val _listMeringAudios = MutableLiveData<AudioFileScans>()
    private val _listMixingAudios = MutableLiveData<AudioFileScans>()
    private var listResolver = mutableListOf<ResolveInfo>()
    private var backgroundScope = CoroutineScope(Dispatchers.Default)
    private lateinit var mediaMetadataRetriever: MediaMetadataRetriever
    override fun init(context: Context) {
        mediaMetadataRetriever = MediaMetadataRetriever()
        if (PermissionManager.hasStoragePermission()) {
            createNecessaryFolders()
            audioCutter = ManagerFactory.getAudioCutter()
            if (initialized) {
                return
            }
            initialized = true
            mContext = context.applicationContext
            intent = Intent()
            intent.action = Intent.ACTION_SEND
            intent.type = "audio/*"
            scanAllFile()
            unRegisterContentObserve()
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

    private fun scanAllFile() {

        var duration: Long
        var bitrate: Int
        if (scanningState == ScanningState.WAITING_FOR_CANCELING) {
            return
        }
        backgroundScope.launch {
            if (scanningState == ScanningState.RUNNING) {
                scanningState = ScanningState.WAITING_FOR_CANCELING
                while (scanningState == ScanningState.WAITING_FOR_CANCELING) {
                    delay(100)
                }
            }
            scanningState = ScanningState.RUNNING
            _listCuttingAudios.postValue(AudioFileScans(ArrayList(), StateLoad.LOADING))
            _listMeringAudios.postValue(AudioFileScans(ArrayList(), StateLoad.LOADING))
            _listMixingAudios.postValue(AudioFileScans(ArrayList(), StateLoad.LOADING))
            _listAllAudioFile.postValue(AudioFileScans(ArrayList(), StateLoad.LOADING))
            val resolver = mContext.contentResolver
            listAllAudios = ArrayList()
            val listMergingAudios = ArrayList<AudioFile>()
            val listMixingAudios = ArrayList<AudioFile>()
            val listCuttingAudios = ArrayList<AudioFile>()
            var projection = arrayOf(
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.DATE_ADDED
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

                    val clData = cursor.getColumnIndex(projection[0])
                    val clName = cursor.getColumnIndex(projection[1])
                    val clID = cursor.getColumnIndex(projection[2])
                    val clTitle = cursor.getColumnIndex(projection[3])
                    val clAlbum = cursor.getColumnIndex(projection[4])
                    val clArtist = cursor.getColumnIndex(projection[5])
                    val clDateAdded = cursor.getColumnIndex(projection[6])


                    cursor.moveToFirst()
                    while (!cursor.isAfterLast) {
                        if (scanningState == ScanningState.WAITING_FOR_CANCELING) {
                            break
                        }
                        Log.d("taih", "ScanningState 2 ${scanningState.name}")
                        var mimeType: String = ""
                        var name: String
                        val data = cursor.getString(clData)
                        val audioInfo = audioCutter.getAudioInfo(data)
                        val file = File(data)
                        val preName = file.name
                        if (preName.contains(".")) {
                            name = preName.substring(0, preName.lastIndexOf("."))
                        } else {
                            name = preName
                        }

                        val id = cursor.getString(clID)
                        val bitmap = getBitmapByPath(data)

                        val title = cursor.getString(clTitle)
                        val album = cursor.getString(clAlbum)
                        val artist = cursor.getString(clArtist)
                        if (preName.contains(".")) {
                            mimeType = preName.substring(preName.lastIndexOf("."), preName.length)
                        }
                        val date = getDateByDateAdded(cursor.getLong(clDateAdded))
                        var genre: String? = "Unknown"
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            genre =
                                cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.GENRE))
                        }
                        Log.d(TAG, "scanAllFile: start get Audio info data ${data}")
                        try {
                            Log.d(TAG, "testQueryDuration - start")
//                            duration = getInfoAudioFile(
//                                File(data),
//                                MediaMetadataRetriever.METADATA_KEY_DURATION
//                            )!!.toInt().toLong()
//                            Log.d(TAG, "testQueryDuration - end")
//                            Log.d(TAG, "testQueryBitrate - start")
//                            bitrate = getInfoAudioFile(
//                                File(data),
//                                MediaMetadataRetriever.METADATA_KEY_BITRATE
//                            )!!.toInt()
                            bitrate = audioInfo?.bitRate ?: BitRate._128kb.value
                            duration = audioInfo?.duration ?: 0
                            Log.d(TAG, "testQueryBitrate - end")
                        } catch (e: Exception) {

                            bitrate = audioInfo?.bitRate ?: BitRate._128kb.value
                            duration = audioInfo?.duration ?: 0
                            e.printStackTrace()
                        }
                        val uri = getUriFromFile(id, resolver, file)
                        Log.d(
                            "TAG",
                            "findAllAudioFiles: data :$data \n name : $name   \n ID  $id  \n" +
                                    " duration: $duration \n sie ${file.length()}  " +
                                    " \n URI $uri \n title :$title \n" +
                                    " album : $album   \n" + " artist  $artist  \n" +
                                    " date: $date \n" + " genre $genre  \n " +
                                    "MimeType $mimeType \n filePAth  ${file.absolutePath} \n parent${file.parent}  \n BitRate $bitrate "
                        )


                        if (file.exists()) {
                            val audioFile = AudioFile(
                                file = file, fileName = name.trim(),
                                size = file.length(), bitRate = bitrate!!,
                                time = duration!!, uri = uri, bitmap = bitmap,
                                title = title, alBum = album, artist = artist, dateAdded = date,
                                genre = genre, mimeType = mimeType
                            )
                            listAllAudios.add(audioFile)

                            val folder = checkFolder(audioFile.file.absolutePath)
                            when (folder) {
                                Folder.TYPE_MIXER -> {
                                    Log.d(TAG, "111: audiofilemixxing ${audioFile.fileName}")
                                    listMixingAudios.add(audioFile)
                                }
                                Folder.TYPE_CUTTER -> {
                                    Log.d(TAG, "111: audiofile cutting ${audioFile.fileName}")
                                    listCuttingAudios.add(audioFile)
                                }
                                Folder.TYPE_MERGER -> {
                                    Log.d(TAG, "111: audiofile mering ${audioFile.fileName}")
                                    listMergingAudios.add(audioFile)
                                }
                                else -> {

                                }
                            }
                        }
                        cursor.moveToNext()
                    }

                }


                if (scanningState == ScanningState.RUNNING) {
                    _listCuttingAudios.postValue(
                        AudioFileScans(
                            listCuttingAudios,
                            StateLoad.LOADDONE
                        )
                    )
                    _listMeringAudios.postValue(
                        AudioFileScans(
                            listMergingAudios,
                            StateLoad.LOADDONE
                        )
                    )
                    _listMixingAudios.postValue(
                        AudioFileScans(
                            listMixingAudios,
                            StateLoad.LOADDONE
                        )
                    )
                    Log.d(TAG, "nmcd :cutting ${listCuttingAudios.size}")
                    Log.d(TAG, "nmcd :merring ${listMergingAudios.size}")
                    Log.d(TAG, "nmcd :mixing ${listMixingAudios.size}")
                    _listAllAudioFile.postValue(AudioFileScans(listAllAudios, StateLoad.LOADDONE))
                }

                scanningState = ScanningState.IDLE
            } catch (e: Exception) {
                Log.d(TAG, "scanAllFile: error " + e.message)
                e.printStackTrace()
                _listAllAudioFile.postValue(AudioFileScans(listAllAudios, StateLoad.LOADFAIL))
            } finally {
                Log.d(TAG, "scanAllFile: done")
                cursor?.close()
                scanningState = ScanningState.IDLE
                Log.d("taih", "ScanningState finally ${scanningState.name}")
            }
        }
    }

    private fun checkFolder(filePath: String): Folder? {
        if (filePath.contains(getFolderPath(Folder.TYPE_MERGER))) {
            return Folder.TYPE_MERGER;
        }
        if (filePath.contains(getFolderPath(Folder.TYPE_MIXER))) {
            return Folder.TYPE_MIXER;
        }
        if (filePath.contains(getFolderPath(Folder.TYPE_CUTTER))) {
            return Folder.TYPE_CUTTER;
        }
        return null;
    }

    override fun getInfoAudioFile(type: Int): String? {
        try {
            return mediaMetadataRetriever.extractMetadata(type)!!

        } catch (e: Exception) {
            e.printStackTrace()
        } catch (e: NullPointerException) {

        } catch (e: NumberFormatException) {

        }
        return ""
    }

    override fun getInfoAudioFile(file: File?, type: Int): String? {
        try {
            if (file != null) {
                val mediaMetadataRetriever = MediaMetadataRetriever()
                mediaMetadataRetriever.setDataSource(file.absolutePath)
                return mediaMetadataRetriever.extractMetadata(type)!!
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } catch (e: NullPointerException) {

        } catch (e: NumberFormatException) {

        }
        return ""
    }


    @SuppressLint("SimpleDateFormat")
    override fun getDateCreatFile(file: File?): String? {
        var lastDate: String = ""
        if (file != null) {
            val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
            lastDate = simpleDateFormat.format(Date(file.lastModified()))
        }
        return lastDate
    }


    override fun findAllAudioFiles(): LiveData<AudioFileScans> {
        return listAllAudioFile
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


    @SuppressLint("SimpleDateFormat")
    private fun getDateByDateAdded(date: Long): String? {
        val time = Date(date * 1000)
        val cal = Calendar.getInstance()
        cal.time = time
        val day = cal.get(Calendar.DAY_OF_MONTH)
        val month = cal.get(Calendar.MONTH)
        val year = cal.get(Calendar.YEAR)
        val hours = cal.get(Calendar.HOUR_OF_DAY)
        val minutes = cal.get(Calendar.MINUTE)

        return "$hours:$minutes , $day/$month/$year"
    }


    private fun getBitmapByPath(path: String?): Bitmap? {
        try {
            path?.let {
                val mMediaMeta = MediaMetadataRetriever()
                val buff: ByteArray?
                val bitmap: Bitmap?
                val bitmapfactory = BitmapFactory.Options()

                mMediaMeta.setDataSource(path)
                buff = mMediaMeta.embeddedPicture
                bitmap = BitmapFactory.decodeByteArray(buff, 0, buff!!.size, bitmapfactory)
                return bitmap

            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    private fun getUriFromFile(id: String, resolver: ContentResolver, file: File): Uri? {
        val uri =
            Uri.parse(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI.toString() + File.separator + id)
        if (uri != null) {
            return uri
        } else {
            val values = ContentValues()
            values.put(MediaStore.Audio.AudioColumns.DISPLAY_NAME, file.name)
            values.put(MediaStore.Audio.AudioColumns.DATA, file.absolutePath)
            values.put(MediaStore.Audio.AudioColumns.TITLE, file.name)
            values.put(MediaStore.Audio.AudioColumns.SIZE, file.length())
            values.put(MediaStore.Audio.AudioColumns.MIME_TYPE, "audio/mp3")

            return resolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values)
        }

    }


    override fun buildAudioFileAvailable(filePath: String): AudioFile {
        for (item in listAllAudios) {
            if (filePath == item.file.absolutePath) {
                audioFile = item
            }
        }
        return audioFile
    }

    override fun buildAudioFileUnAvailable(filePath: String): AudioFile {
        mediaMetadataRetriever.setDataSource(filePath)
        val fileAudio = File(filePath.trim())
        var mimeType = ""
        val abSolutePath = fileAudio.absolutePath.toString()
        val audioInfo = ManagerFactory.getAudioCutter().getAudioInfo(filePath)

        val uri = getUriByPath(fileAudio)
        val duration = audioInfo!!.duration
        if (abSolutePath.contains(".")) {
            mimeType =
                abSolutePath.substring(abSolutePath.lastIndexOf("."), abSolutePath.length)
        }
        var bitrate =
            audioInfo.bitRate
        var name = fileAudio.name
        name = if (name.contains(".")) {
            (name.substring(0, name.lastIndexOf(".")))
        } else {
            name
        }
        return AudioFile(
            fileAudio,
            name,
            fileAudio.length(),
            bitrate,
            duration!!.toLong(),
            uri,
            getBitmapByPath(filePath),
            getInfoAudioFile(MediaMetadataRetriever.METADATA_KEY_TITLE),
            getInfoAudioFile(MediaMetadataRetriever.METADATA_KEY_ALBUM),
            getInfoAudioFile(MediaMetadataRetriever.METADATA_KEY_ARTIST),
            getDateCreatFile(fileAudio),
            getInfoAudioFile(MediaMetadataRetriever.METADATA_KEY_GENRE),
            mimeType
        )
    }

    class AudioFileObserver(handler: Handler?) : ContentObserver(handler) {
        override fun onChange(selfChange: Boolean, uri: Uri?) {
            scanAllFile()
            Log.d(TAG, "onChange: scanAllfile")
        }
    }


    private fun registerContentObserVerDeleted() {
        mContext.contentResolver.registerContentObserver(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            true,
            audioFileObserver
        )
    }

    fun unRegisterContentObserve() {
        mContext.contentResolver.unregisterContentObserver(audioFileObserver)
    }


    override fun insertFileToMediastore(file: File): Boolean {
        return try {
            MediaScannerConnection.scanFile(
                mContext,
                arrayOf(file.absolutePath),
                null
            ) { s, uri ->
                Log.d("insertFile", "on complete ${uri}  string $s")
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
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


    override suspend fun saveFile(audioFile: AudioFile, typeFile: Folder): StateFile =
        withContext(Dispatchers.Main) {
            createNecessaryFolders()
            val stat = Environment.getExternalStorageDirectory().path

            Log.d(TAG, "saveFileToExternal: $stat")
            val file = File(stat)
            val totalSize = file.totalSpace / SIZE_MB
            val availableSize = file.usableSpace / SIZE_MB
            val freeSize = file.freeSpace / SIZE_MB
            val currentSize = audioFile.file.readBytes().size / SIZE_MB

            Log.d(
                TAG,
                "infoCapacity: total $totalSize  available  \n $availableSize \n  freesize $freeSize   \n filesize $currentSize"
            )
            if (!audioFile.file.isFile) {
                StateFile.STATE_FILE_NOT_FOUND

            } else if (availableSize + currentSize < totalSize) {

                val pathParent: String
                try {
                    pathParent = when (typeFile) {
                        Folder.TYPE_CUTTER -> "$APP_FOLDER_PATH/${CUTTING_FOLDER_NAME}"
                        Folder.TYPE_MERGER -> "$APP_FOLDER_PATH/${MERGING_FOLDER_NAME}"
                        Folder.TYPE_MIXER -> "$APP_FOLDER_PATH/${MIXING_FOLDER_NAME}"
                    }
                    Log.d(TAG, "saveFileToExternal:pathParent $pathParent")

                    val dic = File(pathParent)
                    Log.d(TAG, "saveFileToExternal: ${dic.exists()}   ")
                    if (!dic.exists()) {
                        dic.mkdirs()
                    }
                    val fileChild = File(pathParent, audioFile.fileName)
                    val fout = FileOutputStream(fileChild)
                    fout.write(audioFile.file.readBytes())
                    fout.close()
                    StateFile.STATE_SAVE_SUCCESS
                } catch (e: IOException) {
                    e.printStackTrace()
                    Log.d(TAG, "saveFileToExternal: ${e.printStackTrace()} ")
                    StateFile.STATE_SAVE_FAIL
                }
            } else {
                StateFile.STATE_FULL_MEMORY
            }
        }




    override fun getListAudioFileByType(typeFile: Folder): LiveData<AudioFileScans> {
        return when (typeFile) {
            Folder.TYPE_MIXER -> {
                _listMixingAudios
            }
            Folder.TYPE_CUTTER -> {
                _listCuttingAudios
            }
            Folder.TYPE_MERGER -> {
                _listMeringAudios
            }
        }
    }

    override fun getUriByPath(itemFile: File): Uri? {

        val folder = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(MediaStore.Audio.Media._ID, MediaStore.Audio.Media.DATA)
        val cursor: Cursor = mContext.contentResolver.query(
            folder,
            projection,
            MediaStore.Audio.Media.DATA + "=?",
            arrayOf(itemFile.path),
            null
        )!!
        try {
            if (cursor.moveToFirst()) {
                uri = Uri.parse(
                    folder.toString() + File.separator + cursor.getString(
                        cursor.getColumnIndex(MediaStore.Audio.Media._ID)
                    )
                )
            }
            return uri
        } finally {
            cursor.close()
        }
    }

    override fun shareFileAudio(audioFile: AudioFile): Boolean {
        return try {
            val intent = Intent()
            intent.action = Intent.ACTION_SEND
            intent.putExtra(Intent.EXTRA_STREAM, audioFile.uri)
            intent.type = "audio/*"
            mContext.startActivity(Intent.createChooser(intent, "choose a sharing app"))
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }


    override fun getListApprQueryReceiveData(): MutableList<ItemAppShare> {
        listResolver = mContext.packageManager.queryIntentActivities(intent, 0)
        Log.d(TAG, "getListApprQueryReceiveData: ${listResolver.size}")
        listAppShares = ArrayList()
        for (info in listResolver) {
            val item = ItemAppShare(
                info.loadLabel(mContext.packageManager).toString(),
                info.loadIcon(mContext.packageManager)
            )
            listAppShares.add(item)
        }
        return listAppShares

    }

    override fun getListReceiveData(): MutableList<ResolveInfo> {
        return mContext.packageManager.queryIntentActivities(intent, 0)

    }

    override fun reNameToFileAudio(
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
            val pathNew = "$subPath/$newName${audioFile.mimeType}"
            val fileNew = File(pathNew)
            file.renameTo(fileNew)

            val values = ContentValues()
            values.put(MediaStore.Audio.AudioColumns.DISPLAY_NAME, "$newName${audioFile.mimeType}")
            Log.d(TAG, "reNameToFileAudio: path $pathNew")

            val rows: Int = mContext.contentResolver.update(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values,
                null, null
            )
            Log.d(TAG, "insertFile: rows $rows")
            MediaScannerConnection.scanFile(
                mContext,
                arrayOf(fileNew.absolutePath),
                null
            ) { s, uri ->
                Log.d("insertFile", "on complete ${uri}  string $s")
            }

            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    private fun getAllFileName(folder: Folder): HashSet<String> {
        val folderPath = getFolderPath(folder)
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
            Log.d("nmcode", "checkFileNameDuplicate: $it")
            if (name == it) {
                result = true
            }
        }

        return result
    }

    override  fun openWithApp( uri: Uri) {
        val intent = Intent()
        intent.action = Intent.ACTION_VIEW
        intent.setDataAndType(uri, "audio/*")
        mContext.startActivity(intent)
    }


}

