package com.example.audiocutter.core.audioManager

import android.content.Context
import android.media.MediaMetadataRetriever
import android.os.Environment
import android.os.StatFs
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.audiocutter.core.manager.AudioFileManager
import com.example.audiocutter.objects.AudioFile
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


class AudioFileManagerImpl : AudioFileManager {
    // permission READ_EXTERNAL_STORAGE
    //permisstion WRITE_EXTERNAL_STORAGE
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
            val listData = ArrayList<AudioFile>()

            val projection = arrayOf(
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.DURATION

            )
            val audioFiles =
                context.contentResolver.query(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    projection,
                    null,
                    null,
                    null
                )!!


            val clData = audioFiles.getColumnIndex(projection[0])
            val clName = audioFiles.getColumnIndex(projection[1])
            val clDuration = audioFiles.getColumnIndex(projection[2])

            audioFiles.moveToFirst()
            while (!audioFiles.isAfterLast) {
                val data = audioFiles.getString(clData)
                val name = audioFiles.getString(clName)
                val duration = audioFiles.getString(clDuration)
                val file = File(data)

                Log.d(
                    "TAG",
                    "findAllAudioFiles: data :$data \n name : $name  \n duration: $duration \n sie ${file.length()}"
                )

                listData.add(AudioFile(file, name, file.length(), 128, duration.toLong()))
                audioFiles.moveToNext()
            }
            _listAllAudioFile.postValue(listData)

            audioFiles.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return listAllAudioFile
    }


    override fun buildAudioFile(filePath: String): AudioFile {
        TODO("Not yet implemented")
    }


    suspend fun saveFileToExternal(audioFile: AudioFile, typeFile: TypeFile): StateFile {


        val stat = StatFs(Environment.getExternalStorageDirectory().getAbsolutePath())

        val blockSize = stat.blockSize.toLong()
        val totalSize = (stat.getBlockCount() * blockSize) / SIZE_MB
        val availableSize = (stat.getAvailableBlocks() * blockSize) / SIZE_MB
        val freeSize = (stat.getFreeBlocks() * blockSize) / SIZE_MB
        val currentSize = audioFile.file.readBytes().size

        Log.d(
            TAG,
            "infoCapacity: total $totalSize  available  \n $availableSize \n  freesize $freeSize   \n filesize $currentSize"
        )

        if (availableSize + (currentSize / SIZE_MB) < totalSize) {
            val SUB_PATH = "${Environment.getExternalStorageDirectory().getPath()}/AudioCutter"
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
                val fileChild = File(pathParent, audioFile.fileName)
                val fout = FileOutputStream(fileChild)

                fout.write(audioFile.file.readBytes())
                fout.close()

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

    suspend fun getListFileByType(typeFile: TypeFile): LiveData<List<AudioFile>> {
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
                  Log.d(
                      TAG,
                      "getListFileByType :duration $duration   name${itemFile.name}   size  ${itemFile.length()} "
                  )
                  listData.add(
                      AudioFile(
                          itemFile, itemFile.name, itemFile.length(), 128, duration.toLong()
                      )
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


}