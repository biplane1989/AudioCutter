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
import com.example.audiocutter.core.manager.AudioFileManager
import com.example.audiocutter.objects.AudioFile
import com.example.audiocutter.objects.AudioFileScans
import com.example.audiocutter.objects.StateLoad
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


object AudioFileManagerImpl : AudioFileManager {

    val SUB_PATH = "${Environment.getExternalStorageDirectory()}/AudioCutter"
    private var uri: Uri = Uri.parse("")
    private val SIZE_KB: Long = 1024L
    private val SIZE_MB = SIZE_KB * SIZE_KB
    private val SIZE_GB = SIZE_MB * SIZE_KB
    private val TAG = AudioFileManagerImpl::class.java.name
    lateinit var mContext: Context

    private var _listAllAudioFile = MutableLiveData<AudioFileScans>()
    val listAllAudioFile: LiveData<AudioFileScans>
        get() = _listAllAudioFile

    private var _listAudioByType = MutableLiveData<AudioFileScans>()
    val listAudioByType: LiveData<AudioFileScans>
        get() = _listAudioByType


    private val audioFileObserver = AudioFileObserver(Handler())
    private var listData = mutableListOf<AudioFile>()
    fun init(context: Context) {
        mContext = context
        listData = scanAllFile() as MutableList<AudioFile>
        registerContentObserVerDeleted()
    }


    private fun scanAllFile(): List<AudioFile> {
        val resolver = mContext.contentResolver
        val listData = ArrayList<AudioFile>()
        var projection = arrayOf(
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DATE_ADDED
        )

        val cursor =
            resolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection, null,
                null, null
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
                    val bitmap =
                        getBitmapByPath(data)
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


                    val bitRate =
                        getInfoAudioFile(file, MediaMetadataRetriever.METADATA_KEY_BITRATE)
//                    val bitRate = 128
                    val duration =
                        getInfoAudioFile(file, MediaMetadataRetriever.METADATA_KEY_DURATION)
//                    val duration = 1000
                    val uri = getUriFromFile(id, resolver, file)
                    Log.d(
                        "TAG",
                        "findAllAudioFiles: data :$data \n name : $name   \n ID  $id  \n" +
                                " duration: $duration \n sie ${file.length()}  " +
                                " \n URI $uri \n title :$title \n" +
                                " album : $album   \n" + " artist  $artist  \n" +
                                " date: $date \n" + " genre $genre  \n " +
                                "MimeType $mimeType \n filePAth  ${file.absolutePath} \n parent${file.parent}  \n BitRate $bitRate "
                    )
                    if (file.exists()) {
                        if (bitmap != null) {
                            listData.add(
                                AudioFile(
                                    file = file, fileName = name.trim(),
                                    size = file.length(), bitRate = bitRate!!.toInt(),
                                    time = duration!!.toLong(), uri = uri,
                                    bitmap = bitmap, title = title,
                                    alBum = album, artist = artist,
                                    dateAdded = date, genre = genre,
                                    mimeType = mimeType
                                )
                            )
                        } else {
                            listData.add(
                                AudioFile(
                                    file = file, fileName = name,
                                    size = file.length(), bitRate = 128,
                                    time = duration!!.toLong(), uri = uri,
                                    bitmap = null, title = title, alBum = album,
                                    artist = artist, dateAdded = date, genre = genre,
                                    mimeType = mimeType

                                )
                            )
                        }
                    }

                    cursor.moveToNext()
                }
            }

            _listAllAudioFile.postValue(AudioFileScans(listData, StateLoad.LOADDONE))

        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            cursor?.close()
        }

        return listData
    }


    override fun getInfoAudioFile(itemFile: File?, type: Int): String? {
        try {
            if (itemFile != null) {
                val mediaMetadataRetriever = MediaMetadataRetriever()
                mediaMetadataRetriever.setDataSource(itemFile.absolutePath)
                return mediaMetadataRetriever.extractMetadata(type)!!
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }

    @SuppressLint("SimpleDateFormat")
    override fun getdateCreatFile(file: File?): String? {
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
        path?.let {
            val mMediaMeta = MediaMetadataRetriever()
            val buff: ByteArray?
            val bitmap: Bitmap?
            val bitmapfactory = BitmapFactory.Options()

            try {
                mMediaMeta.setDataSource(path)
                buff = mMediaMeta.embeddedPicture
                bitmap = BitmapFactory.decodeByteArray(buff, 0, buff!!.size, bitmapfactory)
                return bitmap
            } catch (e: Exception) {
//                e.printStackTrace()
            }
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
        val fileAudio = File(filePath)
        var mimeType = ""
        val abSolutePath = fileAudio.absolutePath.toString()
        val uri = getUriByPath(fileAudio)
        val duration = getInfoAudioFile(fileAudio, MediaMetadataRetriever.METADATA_KEY_DURATION)
        if (abSolutePath.contains(".")) {
            mimeType = abSolutePath.substring(abSolutePath.lastIndexOf("."), abSolutePath.length)
        }
        return AudioFile(
            fileAudio,
            fileAudio.name,
            fileAudio.length(),
            getInfoAudioFile(fileAudio, MediaMetadataRetriever.METADATA_KEY_BITRATE)!!.toInt(),
            duration!!.toLong(),
            uri,
            getBitmapByPath(filePath),
            getInfoAudioFile(fileAudio, MediaMetadataRetriever.METADATA_KEY_TITLE),
            getInfoAudioFile(fileAudio, MediaMetadataRetriever.METADATA_KEY_ALBUM),
            getInfoAudioFile(fileAudio, MediaMetadataRetriever.METADATA_KEY_ARTIST),
            getdateCreatFile(fileAudio),
            getInfoAudioFile(fileAudio, MediaMetadataRetriever.METADATA_KEY_GENRE), mimeType
        )
    }

    class AudioFileObserver(handler: Handler?) : ContentObserver(handler) {
        override fun onChange(selfChange: Boolean, uri: Uri?) {
            CoroutineScope(Dispatchers.IO).launch {
                Log.d("TAG", "changeList: $uri")
                val listAllAudio = scanAllFile()
//                Log.d(TAG, "changeList: ${listAllAudio.size}")
                _listAllAudioFile.postValue(AudioFileScans(listAllAudio, StateLoad.LOADDONE))
            }
        }
    }


    private fun registerContentObserVerDeleted() {
        mContext.contentResolver.registerContentObserver(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            true, audioFileObserver
        )
    }

    fun unRegisterContentObserve() {
        mContext.contentResolver.unregisterContentObserver(audioFileObserver)
    }


    override suspend fun saveFile(audioFile: AudioFile, typeFile: Folder): StateFile =
        withContext(Dispatchers.Main) {
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

            } else
                if (availableSize + currentSize < totalSize) {

                    val pathParent: String
                    try {
                        pathParent = when (typeFile) {
                            Folder.TYPE_CUTTER -> "$SUB_PATH/cutter"
                            Folder.TYPE_MERGER -> "$SUB_PATH/merger"
                            Folder.TYPE_MIXER -> "$SUB_PATH/mixer"
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


    private fun scanListAudioFileByType(typeFile: Folder): List<AudioFile> {
        listData = when (typeFile) {
            Folder.TYPE_MIXER -> {
                (getListByType(listData, "/AudioCutter/mixer/") as MutableList<AudioFile>)
            }
            Folder.TYPE_MERGER -> {
                (getListByType(listData, "/AudioCutter/merger/") as MutableList<AudioFile>)
            }
            Folder.TYPE_CUTTER -> {
                (getListByType(listData, "/AudioCutter/cutter/") as MutableList<AudioFile>)
            }
        }
        return listData
    }

    private fun getListByType(listData: MutableList<AudioFile>, text: String): List<AudioFile> {
        val listDataTmp = mutableListOf<AudioFile>()
        listData.forEach {
            if (it.file.toString().contains(text)) {
                listDataTmp.add(it)
            }
        }
        return listDataTmp
    }

    override fun getListAudioFileByType(typeFile: Folder): LiveData<AudioFileScans> {
        listData = scanListAudioFileByType(typeFile) as MutableList<AudioFile>
        _listAudioByType.postValue(
            AudioFileScans(listData, StateLoad.LOADDONE)
        )
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
                        cursor.getColumnIndex(
                            MediaStore.Audio.Media._ID
                        )
                    )
                )
            }
            return uri
        } finally {
            cursor.close()
        }
    }
}

