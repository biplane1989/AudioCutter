package com.example.audiocutter.core.audioManager

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.database.ContentObserver
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.audiocutter.R
import com.example.audiocutter.core.manager.AudioFileManager
import com.example.audiocutter.core.manager.ManagerFactory
import com.example.audiocutter.objects.AudioFile
import com.example.audiocutter.objects.AudioFileScans
import com.example.audiocutter.objects.StateLoad
import com.example.audiocutter.permissions.PermissionManager
import com.example.audiocutter.util.Utils
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
    const val APP_FOLDER_NAME = "AudioCutter"
    const val CUTTING_FOLDER_NAME = "cutter"
    const val MERGING_FOLDER_NAME = "merger"
    const val MIXING_FOLDER_NAME = "mixer"


    enum class ScanningState {
        IDLE,
        RUNNING,
        WAITING_FOR_CANCELING
    }

    val APP_FOLDER_PATH = "${Environment.getExternalStorageDirectory()}/${APP_FOLDER_NAME}"
    private var uri: Uri = Uri.parse("")
    private val SIZE_KB: Long = 1024L
    private val SIZE_MB = SIZE_KB * SIZE_KB
    private val SIZE_GB = SIZE_MB * SIZE_KB
    private val TAG = AudioFileManagerImpl::class.java.name
    lateinit var mContext: Context
    private var initialized = false
    private var pathParent = ""
    private var nameTmp = ""
    private var _listAllAudioFile = MutableLiveData<AudioFileScans>()
    private lateinit var audioCutter: AudioCutter
    private var scanningState = ScanningState.IDLE
    val listAllAudioFile: LiveData<AudioFileScans>
        get() = _listAllAudioFile
    private var _listAudioByType = MutableLiveData<AudioFileScans>()

    val listAudioByType: LiveData<AudioFileScans>
        get() = _listAudioByType
    private val audioFileObserver = AudioFileObserver(Handler())


    private var listData = mutableListOf<AudioFile>()
    private var backgroundScope = CoroutineScope(Dispatchers.Default)

    override fun init(context: Context) {
        if (PermissionManager.hasStoragePermission()) {
            createNecessaryFolders()
            audioCutter = ManagerFactory.getAudioCutter()
            if (initialized) {
                return
            }
            initialized = true
            mContext = context.applicationContext
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
        if (scanningState == ScanningState.WAITING_FOR_CANCELING) {
            Log.d("taih", "return WAITING_FOR_CANCELING")
            return
        }
        backgroundScope.launch {
            Log.d("taih", "ScanningState ${scanningState.name}")
            if (scanningState == ScanningState.RUNNING) {
                scanningState = ScanningState.WAITING_FOR_CANCELING
                while (scanningState == ScanningState.WAITING_FOR_CANCELING) {
                    Log.d("taih", "waiting ")
                    delay(100)
                }
            }
            scanningState = ScanningState.RUNNING

            _listAllAudioFile.postValue(AudioFileScans(ArrayList(), StateLoad.LOADING))
            val resolver = mContext.contentResolver
            val listData = ArrayList<AudioFile>()
            var projection = arrayOf(
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.DATE_ADDED,
                MediaStore.Audio.Media.DURATION
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
                    val clDuration = cursor.getColumnIndex(projection[7])


                    cursor.moveToFirst()
                    while (!cursor.isAfterLast) {
                        if (scanningState == ScanningState.WAITING_FOR_CANCELING) {
                            break
                        }
                        Log.d("taih", "ScanningState 2 ${scanningState.name}")
                        var mimeType: String = ""
                        var name: String
                        val data = cursor.getString(clData)
                        val file = File(data)
                        val preName = file.name
                        if (preName.contains(".")) {
                            name = preName.substring(0, preName.lastIndexOf("."))
                        } else {
                            name = preName
                        }
                        val id = cursor.getString(clID)
//                        val bitmap = getBitmapByPath(data)
                        val bitmap = BitmapFactory.decodeResource(
                            mContext.resources,
                            R.drawable.ic_play_mixing_audio
                        )
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

                        val audioInfo = audioCutter.getAudioInfo(data)

                        val bitRate = audioInfo?.bitRate ?: BitRate._128kb.value
                        val duration = audioInfo?.duration ?: 0

                        Log.d(
                            "TAG",
                            "findAllAudioFiles: data :$data \n name : $name   \n ID  $id  \n" +
                                    " duration: $duration \n sie ${file.length()}  " +
                                    " \n URI $uri \n title :$title \n" +
                                    " album : $album   \n" + " artist  $artist  \n" +
                                    " date: $date \n" + " genre $genre  \n " +
                                    "MimeType $mimeType \n filePAth  ${file.absolutePath} \n parent${file.parent}  \n BitRate $bitRate "
                        )

//                        val duration =
                        //audioCutter.getAudioInfo(data)?.duration


                        //val bitRate = getInfoAudioFile(File(data), MediaMetadataRetriever.METADATA_KEY_BITRATE)?.toInt()
//                        val duration = getInfoAudioFile(File(data), MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong()
                        //val duration = cursor.getString(clDuration).toLong()

                        val uri = getUriFromFile(id, resolver, file)
                        if (file.exists()) {
                            if (bitmap != null) {
                                listData.add(
                                    AudioFile(
                                        file = file,
                                        fileName = name.trim(),
                                        size = file.length(),
                                        bitRate = bitRate!!,
                                        time = duration!!,
                                        uri = uri,
                                        bitmap = bitmap,
                                        title = title,
                                        alBum = album,
                                        artist = artist,
                                        dateAdded = date,
                                        genre = genre,
                                        mimeType = mimeType
                                    )
                                )
                            } else {
                                listData.add(
                                    AudioFile(
                                        file = file,
                                        fileName = name,
                                        size = file.length(),
                                        bitRate = bitRate!!,
                                        time = duration!!,
                                        uri = uri,
                                        bitmap = null,
                                        title = title,
                                        alBum = album,
                                        artist = artist,
                                        dateAdded = date,
                                        genre = genre,
                                        mimeType = mimeType

                                    )
                                )
                            }
                        }
                        cursor.moveToNext()
                    }
                }

                if (scanningState == ScanningState.RUNNING) {
                    Log.d("taih", "ScanningState 1111 ${scanningState.name} ${listData.size}")
                    this@AudioFileManagerImpl.listData.clear()
                    this@AudioFileManagerImpl.listData.addAll(listData)
                    Log.d(TAG, "scanAllFile: size list ${this@AudioFileManagerImpl.listData.size}")
                    _listAllAudioFile.postValue(AudioFileScans(listData, StateLoad.LOADDONE))
                }

                scanningState = ScanningState.IDLE
            } catch (e: Exception) {
                Log.d(TAG, "scanAllFile: error " + e.message)
                e.printStackTrace()
                _listAllAudioFile.postValue(AudioFileScans(listData, StateLoad.LOADFAIL))
            } finally {
                Log.d(TAG, "scanAllFile: done")
                cursor?.close()
                scanningState = ScanningState.IDLE
                Log.d("taih", "ScanningState finally ${scanningState.name}")
            }
        }
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


    override fun buildAudioFile(filePath: String): AudioFile {
        val fileAudio = File(filePath.trim())
        var mimeType = ""
        val abSolutePath = fileAudio.absolutePath.toString()


        val uri = getUriByPath(fileAudio)
        val duration = getInfoAudioFile(fileAudio, MediaMetadataRetriever.METADATA_KEY_DURATION)
        if (abSolutePath.contains(".")) {
            mimeType = abSolutePath.substring(abSolutePath.lastIndexOf("."), abSolutePath.length)
        }

        var bitrate =
            getInfoAudioFile(fileAudio, MediaMetadataRetriever.METADATA_KEY_BITRATE)!!.toInt()
//        val bitrate = 128
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
            getInfoAudioFile(fileAudio, MediaMetadataRetriever.METADATA_KEY_TITLE),
            getInfoAudioFile(fileAudio, MediaMetadataRetriever.METADATA_KEY_ALBUM),
            getInfoAudioFile(fileAudio, MediaMetadataRetriever.METADATA_KEY_ARTIST),
            getDateCreatFile(fileAudio),
            getInfoAudioFile(fileAudio, MediaMetadataRetriever.METADATA_KEY_GENRE),
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
                Log.d(TAG, "getAllFileName: ${it.name}")
            }
        }
        return fileNameHash

    }

    @SuppressLint("SimpleDateFormat")
    override fun genNewAudioFileName(typeFile: Folder): String {
        val random = Random()
        val fileNameHash = getAllFileName(typeFile)
        var fileName = ""
        val day = SimpleDateFormat("dd_MM").format(Date())
        var textName = ""
        fileName = when (typeFile) {
            Folder.TYPE_CUTTER -> {
                "${CUTTING_FOLDER_NAME}_${APP_FOLDER_NAME}_$day"
            }
            Folder.TYPE_MERGER -> {
                "${MERGING_FOLDER_NAME}_${APP_FOLDER_NAME}_$day"
            }
            Folder.TYPE_MIXER -> {
                "${MIXING_FOLDER_NAME}_${APP_FOLDER_NAME}_$day"
            }
        }

//        fileName = "AudioCutter_AudioCutter_lonely(2)"

        fileNameHash.forEach {
            textName += "$it,"
        }
        if (textName.contains(fileName)) {
            fileName = "$fileName(${Utils.getAlphaNumericString(random.nextInt(10))})"
        }
        Log.d(TAG, "genNewAudioFileName: $fileName")
        return fileName
    }


    override fun createValidFileName(name: String, typeFile: Folder): String {
        val random = Random()
        val fileNameHash = getAllFileName(typeFile)
        var fileName = ""
        var textName = ""

        fileNameHash.forEach {
            textName += "$it,"
        }
        fileName = if (textName.contains(name)) {
            "$name(${Utils.getAlphaNumericString(random.nextInt(10))})"
        } else {
            name
        }
        Log.d(TAG, "createValidFileName: $fileName")

        return fileName
    }

    private fun scanListAudioFileByType(typeFile: Folder): List<AudioFile> {
        listData = when (typeFile) {
            Folder.TYPE_MIXER -> {
                (getListByType(
                    listData,
                    "/${APP_FOLDER_NAME}/${MIXING_FOLDER_NAME}/"
                ) as MutableList<AudioFile>)
            }
            Folder.TYPE_MERGER -> {
                (getListByType(
                    listData,
                    "/${APP_FOLDER_NAME}/${MERGING_FOLDER_NAME}/"
                ) as MutableList<AudioFile>)
            }
            Folder.TYPE_CUTTER -> {
                (getListByType(
                    listData,
                    "/${APP_FOLDER_NAME}/${CUTTING_FOLDER_NAME}/"
                ) as MutableList<AudioFile>)
            }
        }
        return listData
    }

    private fun getListByType(listData: MutableList<AudioFile>, text: String): List<AudioFile> {
        val listDataTmp = mutableListOf<AudioFile>()
//        Log.d("giangtd", "getListByType: path : ${Environment.getExternalStorageDirectory()}" + text)
        listData.forEach {
            if (it.file.toString().contains(text)) {
                listDataTmp.add(it)
            }
        }
        return listDataTmp
    }

    override fun getListAudioFileByType(typeFile: Folder): LiveData<AudioFileScans> {
        listData = scanListAudioFileByType(typeFile) as MutableList<AudioFile>
        Log.d(
            "giangtd",
            "getListAudioFileByType: type: " + typeFile + "--- list size : " + listData.size
        )
        _listAudioByType.postValue(AudioFileScans(listData, StateLoad.LOADDONE))
        return listAudioByType
    }

    private fun getUriByPath(itemFile: File): Uri? {

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


}

