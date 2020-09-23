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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.*
import kotlin.collections.ArrayList


object AudioFileManagerImpl : AudioFileManager {


    private var uri: Uri = Uri.parse("")
    private val SIZE_KB: Long = 1024L
    private val SIZE_MB = SIZE_KB * SIZE_KB
    private val SIZE_GB = SIZE_MB * SIZE_KB
    private val TAG = AudioFileManagerImpl::class.java.name
    lateinit var mContext: Context
    val listAllByType = ArrayList<AudioFile>()

    private var _listAllAudioFile = MutableLiveData<List<AudioFile>>()
    val listAllAudioFile: LiveData<List<AudioFile>>
        get() = _listAllAudioFile

    private var _listAudioByType = MutableLiveData<List<AudioFile>>(ArrayList())
    val listAudioByType: LiveData<List<AudioFile>>
        get() = _listAudioByType


    private var _listAllAudioByType = MutableLiveData<List<AudioFile>>()
    val listAllAudioByType: LiveData<List<AudioFile>>
        get() = _listAllAudioByType

    val audioFileObserver = AudioFileObserver(Handler())

    fun init(context: Context) {
        mContext = context
    }

    @SuppressLint("RestrictedApi", "SimpleDateFormat")
    override suspend fun findAllAudioFiles(): LiveData<List<AudioFile>> =
        withContext(Dispatchers.IO) {
            var projection: Array<String>
            val resolver = mContext.contentResolver
            val listData = ArrayList<AudioFile>()
            projection = arrayOf(
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.DURATION,
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
                    val clDuration = cursor.getColumnIndex(projection[2])
                    val clID = cursor.getColumnIndex(projection[3])
                    val clTitle = cursor.getColumnIndex(projection[4])
                    val clAlbum = cursor.getColumnIndex(projection[5])
                    val clArtist = cursor.getColumnIndex(projection[6])
                    val clDateAdded = cursor.getColumnIndex(projection[7])


                    cursor.moveToFirst()
                    while (!cursor.isAfterLast) {
                        val data = cursor.getString(clData)
                        val name = cursor.getString(clName)
                        val duration = cursor.getString(clDuration)
                        val id = cursor.getString(clID)
                        val bitmap =
                            getBitmapByPath(data)
                        val title = cursor.getString(clTitle)
                        val album = cursor.getString(clAlbum)
                        val artist = cursor.getString(clArtist)

                        //get time of currentDay by Longtime
                        val date = getDateByDateAdded(cursor.getLong(clDateAdded))
                        var genre: String? = "Unknown"
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            genre =
                                cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.GENRE))
                        }

                        val file = File(data)
                        val uri = getUriFromFile(id, resolver, file)
                        Log.d(
                            "TAG",
                            "findAllAudioFiles: data :$data \n name : $name   \n ID  $id  \n duration: $duration \n sie ${file.length()}   \n URI $uri \n title :$title \n" +
                                    " album : $album   \n" +
                                    " artist  $artist  \n" +
                                    " date: $date \n" +
                                    " genre $genre  "
                        )
                        if (file.exists()) {
                            if (bitmap != null) {
                                listData.add(
                                    AudioFile(
                                        file = file, fileName = name,
                                        size = file.length(), bitRate = 128,
                                        time = duration.toLong(), uri = uri,
                                        bitmap = bitmap, title = title, alBum = album,
                                        artist = artist, dateAdded = date, genre = genre
                                    )
                                )
                            } else {
                                listData.add(
                                    AudioFile(
                                        file = file, fileName = name,
                                        size = file.length(), bitRate = 128,
                                        time = duration.toLong(), uri = uri,
                                        bitmap = null, title = title, alBum = album,
                                        artist = artist, dateAdded = date, genre = genre
                                    )
                                )
                            }
                        }

                        cursor.moveToNext()
                    }
                }
                Log.d(TAG, "sizeListAllAudioFile: ${listData.size}")
                _listAllAudioFile.postValue(listData)

            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                cursor?.close()
            }

            listAllAudioFile
        }

    override suspend fun deleteFile(listAudioFile: List<AudioFile>, typeFile: Folder): Boolean {
        TODO("Not yet implemented")
    }

   /* override fun deleteFile(audioFile: AudioFile): Boolean {
        var rs = false
        val resolver = mContext.contentResolver
        var rowDeleted = 0
        try {
            val contentValues = ContentValues()
            contentValues.put(MediaStore.Audio.AudioColumns.DATA, audioFile.file.absolutePath)
            if (audioFile.file.exists() && audioFile.uri != null) {
                Log.d(TAG, "deleteFile: ${audioFile.uri}")
                rowDeleted = resolver.delete(audioFile.uri!!, null, null)
                if (rowDeleted != 0) {
                    rs = true
                }
            }
            Log.d(TAG, "onClick: result $rs  rowDelete $rowDeleted")
        } catch (e: Exception) {
            e.printStackTrace()
            rs = false
        }
        return rs
    }*/

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


    fun getBitmapByPath(path: String?): Bitmap? {
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
                e.printStackTrace()
            }
        }
        return null
    }

    private fun getUriFromFile(id: String, resolver: ContentResolver, file: File): Uri? {
        var uri =
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
        TODO("Not yet implemented")
    }

    class AudioFileObserver(handler: Handler?) : ContentObserver(handler) {
        override fun onChange(selfChange: Boolean, uri: Uri?) {
            CoroutineScope(Dispatchers.IO).launch {

                Log.d("TAG", "changeList: $uri")
                val listAllAudio = findAllAudioFiles().value
                Log.d(TAG, "changeList: ${listAllAudio!!.size}")
                _listAllAudioFile.postValue(listAllAudio)

            }
        }
    }



    fun registerContentObserVerDeleted() {
        mContext.contentResolver.registerContentObserver(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            true, audioFileObserver
        )
    }

    fun unRegisterContentObserve() {
        mContext.contentResolver.unregisterContentObserver(audioFileObserver)
    }

    override fun saveFile(audioFile: AudioFile, typeFile: Folder): Boolean {
        TODO("Not yet implemented")
    }


/* override suspend fun saveFile(audioFile: AudioFile, typeFile: TypeFile): StateFile =
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
            if (!audioFile.file.isFile || !audioFile.file.isDirectory) {
                StateFile.STATE_FILE_NOT_FOUND
            }
            if (availableSize + currentSize < totalSize) {
                val SUB_PATH = "${Environment.getExternalStorageDirectory()}/AudioCutter"
                val pathParent: String
                try {
                    when (typeFile) {
                        TypeFile.TYPE_CUTTER -> pathParent = "$SUB_PATH/cutter"
                        TypeFile.TYPE_MERGER -> pathParent = "$SUB_PATH/merger"
                        TypeFile.TYPE_MIXER -> pathParent = "$SUB_PATH/mixer"
                    }
                    Log.d(TAG, "saveFileToExternal:pathParent $pathParent")

                    val dic = File(pathParent)
                    if (!dic.exists()) {
                        dic.mkdirs()
                    }
                    withContext(Dispatchers.IO) {
                        val fileChild = File(pathParent, audioFile.fileName)
                        val fout = FileOutputStream(fileChild)
                        fout.write(audioFile.file.readBytes())
                        fout.close()
                    }
                    Log.d(TAG, "saveFileToExternal: save success")
                    StateFile.STATE_SAVE_SUCCESS

                } catch (e: IOException) {
                    e.printStackTrace()
                    Log.d(TAG, "saveFileToExternal: ${e.printStackTrace()} ")
                    StateFile.STATE_SAVE_FAIL
                }
            } else {
                Log.d(TAG, "saveFileToExternal: out of memory")
                StateFile.STATE_FULL_MEMORY
            }
        }*/

    override suspend fun getListAudioFileByType(typeFile: Folder): LiveData<List<AudioFile>> {
        return withContext(Dispatchers.IO) {

            val listData = ArrayList<AudioFile>()
            val SUB_PATH = "${Environment.getExternalStorageDirectory()}/AudioCutter"

            val pathParent: String
            try {
                when (typeFile) {
                    Folder.TYPE_CUTTER -> pathParent = "$SUB_PATH/cutter"
                    Folder.TYPE_MERGER -> pathParent = "$SUB_PATH/merger"
                    Folder.TYPE_MIXER -> pathParent = "$SUB_PATH/mixer"
                }
                Log.d(TAG, "getListFileByType: $pathParent")
                val file = File(pathParent)
                val listFiles = file.listFiles()
                listFiles?.let {

                    for (itemFile in it) {
                        val mediaMetadataRetriever = MediaMetadataRetriever()
                        mediaMetadataRetriever.setDataSource(itemFile.absolutePath)
                        val duration =
                            mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)!!
                        val uri = getUriByPath(itemFile)

                        uri?.let {
                            listData.add(
                                AudioFile(
                                    itemFile,
                                    itemFile.name,
                                    itemFile.length(),
                                    128,
                                    duration.toLong(),
                                    uri = uri
                                )
                            )
                        }
                        Log.d(
                            TAG,
                            "getListFileByType :duration $duration   name  ${itemFile.name}   size  ${itemFile.length()}   URI $uri"
                        )
                    }
                    _listAudioByType.postValue(listData)

                    Log.d("taih", "size: ${listData.size}")
                }
            } catch (e: Exception) {
                Log.d(TAG, "exception: ${e.printStackTrace()}")
            }
            listAudioByType
        }
    }

    private fun getUriByPath(itemFile: File): Uri? {

        val folder = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(MediaStore.Audio.Media._ID, MediaStore.Audio.Media.DATA)
        val cursor: Cursor = mContext.getContentResolver().query(
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


    suspend fun getAllListByType()
            : LiveData<List<AudioFile>> {
        listAllByType.clear()

        val listTypeCutter = getListAudioFileByType(Folder.TYPE_CUTTER).value!!
        val listTypeMerger = getListAudioFileByType(Folder.TYPE_MERGER).value!!
        val listTypeMixer = getListAudioFileByType(Folder.TYPE_MIXER).value!!


        Log.d(TAG, "getAllListByType: ${listTypeCutter.size}")
        Log.d(TAG, "getAllListByType: ${listTypeMerger.size}")
        Log.d(TAG, "getAllListByType: ${listTypeMixer.size}")

        listAllByType.addAll(listTypeCutter)

        listAllByType.addAll(listTypeMerger)
        listAllByType.addAll(listTypeMixer)

        _listAllAudioByType.postValue(listAllByType)

        Log.d(
            TAG,
            "getAllListByType: livedata ${_listAllAudioByType.value?.size}   listDatasize ${listAllByType.size}"
        )
        return listAllAudioByType

    }


}

