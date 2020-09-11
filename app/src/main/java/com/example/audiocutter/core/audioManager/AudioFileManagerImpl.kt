package com.example.audiocutter.core.audioManager

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.audiocutter.core.manager.AudioFileManager
import com.example.audiocutter.objects.AudioFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


class AudioFileManagerImpl : AudioFileManager {
    // permission READ_EXTERNAL_STORAGE
    //permisstion WRITE_EXTERNAL_STORAGE
    var uri: Uri = Uri.parse("")
    private val SIZE_KB: Long = 1024L
    private val SIZE_MB = SIZE_KB * SIZE_KB
    private val SIZE_GB = SIZE_MB * SIZE_KB
    private val TAG = AudioFileManagerImpl::class.java.name

    private var _listAllAudioFile = MutableLiveData<List<AudioFile>>()
    val listAllAudioFile: LiveData<List<AudioFile>>
        get() = _listAllAudioFile

    private var _listAudioByType = MutableLiveData<List<AudioFile>>()
    val listAudioByType: LiveData<List<AudioFile>>
        get() = _listAudioByType


    override suspend fun findAllAudioFiles(context: Context): LiveData<List<AudioFile>> {
        try {
            val resolver = context.contentResolver
            val listData = ArrayList<AudioFile>()
            val projection = arrayOf(
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media._ID
            )

            val cursor =
                resolver.query(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    projection,
                    null,
                    null,
                    null
                )!!


            val clData = cursor.getColumnIndex(projection[0])
            val clName = cursor.getColumnIndex(projection[1])
            val clDuration = cursor.getColumnIndex(projection[2])
            val clID = cursor.getColumnIndex(projection[3])

            cursor.moveToFirst()
            while (!cursor.isAfterLast) {


                val data = cursor.getString(clData)
                val name = cursor.getString(clName)
                val duration = cursor.getString(clDuration)
                val id = cursor.getString(clID)
                val file = File(data)
                val uri = getUriFromFile(id, resolver, file)
                Log.d(
                    "TAG",
                    "findAllAudioFiles: data :$data \n name : $name   \n ID  $id  \n duration: $duration \n sie ${file.length()}   \n URI $uri"
                )
                listData.add(
                    AudioFile(
                        file = file,
                        fileName = name,
                        size = file.length(),
                        bitRate = 128,
                        time = duration.toLong(),
                        uri = uri
                    )
                )
                cursor.moveToNext()
            }
            _listAllAudioFile.postValue(listData)
            cursor.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return listAllAudioFile
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

    override suspend fun getListAudioCutter(): LiveData<List<AudioFile>> {
        TODO("Not yet implemented")
    }

    override suspend fun getListAudioMerger(): LiveData<List<AudioFile>> {
        TODO("Not yet implemented")
    }

    override suspend fun getListAudioMixer(): LiveData<List<AudioFile>> {
        TODO("Not yet implemented")
    }


    suspend fun saveFileToExternal(audioFile: AudioFile, typeFile: TypeFile): StateFile {
//   android 10 androidmanifest    android:requestLegacyExternalStorage="true"

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
            return StateFile.STATE_FILE_NOT_FOUND
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
                return StateFile.STATE_SAVE_SUCCESS

            } catch (e: IOException) {
                e.printStackTrace()
                Log.d(TAG, "saveFileToExternal: ${e.printStackTrace()} ")
                return StateFile.STATE_SAVE_FAIL
            }
        } else {
            Log.d(TAG, "saveFileToExternal: out of memory")
            return StateFile.STATE_FULL_MEMORY
        }
    }

    suspend fun getListFileByType(typeFile: TypeFile, context: Context): LiveData<List<AudioFile>> {

        val listData = ArrayList<AudioFile>()
        val SUB_PATH = "${Environment.getExternalStorageDirectory()}/AudioCutter"

        val pathParent: String
        try {
            when (typeFile) {
                TypeFile.TYPE_CUTTER -> pathParent = "$SUB_PATH/cutter"
                TypeFile.TYPE_MERGER -> pathParent = "$SUB_PATH/merger"
                TypeFile.TYPE_MIXER -> pathParent = "$SUB_PATH/mixer"
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
                    val uri = getUriByPath(itemFile, context)


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

                Log.d(TAG, "size: ${listData.size}")

            }

        } catch (e: IOException) {
            e.printStackTrace()
            Log.d(TAG, "exception: ${e.printStackTrace()}")
        }


        return listAudioByType
    }

    private fun getUriByPath(itemFile: File, context: Context): Uri? {

        val folder = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(MediaStore.Audio.Media._ID, MediaStore.Audio.Media.DATA)
        val cursor: Cursor = context.getContentResolver().query(
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

