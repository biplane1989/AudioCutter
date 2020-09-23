package com.example.audiocutter.core.manager.fake

import android.content.Context
import android.os.Environment
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.audiocutter.R
import com.example.audiocutter.core.manager.AudioFileManager
import com.example.audiocutter.objects.AudioFile
import kotlinx.android.synthetic.main.main_screen.view.*
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

class FakeAudioFileManager : AudioFileManager {
    private val audioFileLiveData = MutableLiveData<List<AudioFile>>()
    private val audioFileLiveData1 = MutableLiveData<List<AudioFile>>()
    private val audioFileLiveData2 = MutableLiveData<List<AudioFile>>()

    private var listAudioFile: ArrayList<AudioFile>
    private var listAudioFile1: ArrayList<AudioFile>
    private var listAudioFile2: ArrayList<AudioFile>

//    val file = File(Environment.getExternalStorageDirectory().toString() + "/Music/Lonely.mp3")

    val file3 = File(Environment.getExternalStorageDirectory().toString() + "/Download/lonely.mp3")
    val file4 = File(Environment.getExternalStorageDirectory().toString() + "/Download/aloha.mp3")


    val file1 = File(Environment.getExternalStorageDirectory()
        .toString() + "/Download/doihoamattroi.mp3")
    val file2 = File(Environment.getExternalStorageDirectory().toString() + "/Download/xaodong.mp3")


    init {

        listAudioFile = ArrayList<AudioFile>()
        listAudioFile.add(AudioFile(file1, "file_name1", 10000, 128))
        listAudioFile.add(AudioFile(file2, "file_name2", 10000, 128))
        audioFileLiveData.postValue(listAudioFile)

        listAudioFile1 = ArrayList<AudioFile>()
        listAudioFile1.add(AudioFile(file3, "file_name3", 10000, 128))
        listAudioFile1.add(AudioFile(file4, "file_name4", 10000, 128))
        audioFileLiveData1.postValue(listAudioFile1)

        listAudioFile2 = ArrayList<AudioFile>()
        listAudioFile2.add(AudioFile(file1, "file_name5", 10000, 128))
        listAudioFile2.add(AudioFile(file2, "file_name6", 10000, 128))
        audioFileLiveData2.postValue(listAudioFile2)


//        MainScope().launch {
//            delay(10000)
////            listAudioFile.add(AudioFile(file3, "file_name2", 10000, 128))
//            listAudioFile.removeAt(0)
//            audioFileLiveData.postValue(listAudioFile)
//        }
    }

    override suspend fun findAllAudioFiles(): LiveData<List<AudioFile>> {
        return audioFileLiveData
    }

    override fun buildAudioFile(filePath: String): AudioFile {
        return AudioFile(File(filePath), "file_name1", 10000, 128)
    }

    override suspend fun getListAudioCutter(): LiveData<List<AudioFile>> {

        return audioFileLiveData

    }

    override suspend fun getListAudioMerger(): LiveData<List<AudioFile>> {
        return audioFileLiveData1
    }

    override suspend fun getListAudioMixer(): LiveData<List<AudioFile>> {
        return audioFileLiveData2
    }

    override suspend fun deleteFile(items: List<AudioFile>, typeAudio: Int): Boolean {

        when (typeAudio) {
            0 -> {
                items.forEach {
                    listAudioFile.remove(it)
                }
                audioFileLiveData.postValue(listAudioFile)
                return true
            }
            1 -> {
                items.forEach {
                    listAudioFile1.remove(it)
                }
                audioFileLiveData1.postValue(listAudioFile1)
                return false
            }
            2 -> {
                items.forEach {
                    listAudioFile2.remove(it)
                }
                audioFileLiveData2.postValue(listAudioFile2)
                return true
            }
            else -> return false
        }
    }
}