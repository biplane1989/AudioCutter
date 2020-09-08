package com.example.audiocutter.core.audioManager

import android.content.Context
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.audiocutter.core.manager.AudioFileManager
import com.example.audiocutter.objects.AudioFile
import java.io.File

class AudioFileManagerImpl : AudioFileManager {
    // permission READ_EXTERNAL_STORAGE
    var _listAudioFile = MutableLiveData<List<AudioFile>>()
    val listAudioFile: LiveData<List<AudioFile>>
        get() = _listAudioFile


    override suspend fun findAllAudioFiles(context: Context): LiveData<List<AudioFile>> {


        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            val listData = ArrayList<AudioFile>()

            val projection = arrayOf(
                    MediaStore.Audio.Media.DATA,
                    MediaStore.Audio.Media.TITLE,
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
            while (audioFiles.moveToNext()) {
                val data = audioFiles.getString(clData)
                val name = audioFiles.getString(clName)
                val duration = audioFiles.getString(clDuration)
                val file = File(data)

                Log.d("TAG", "findAllAudioFiles: data :$data \n name : $name  \n duration: $duration \n sie ${file.length()}")

                listData.add(AudioFile(file, name, file.length(), 128, duration.toLong()))

            }
            _listAudioFile.postValue(listData)
        }

        return listAudioFile
    }


    override fun buildAudioFile(filePath: String): AudioFile {
        TODO("Not yet implemented")
    }

}