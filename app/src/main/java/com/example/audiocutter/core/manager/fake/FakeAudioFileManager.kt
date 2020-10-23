package com.example.audiocutter.core.manager.fake

import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.audiocutter.core.audioManager.Folder
import com.example.audiocutter.core.audioManager.StateFile
import com.example.audiocutter.core.manager.AudioFileManager
import com.example.audiocutter.objects.AudioFile
import com.example.audiocutter.objects.AudioFileScans
import com.example.audiocutter.objects.StateLoad
import kotlinx.coroutines.*
import java.io.File

class FakeAudioFileManager : AudioFileManager {

    private val audioFileLiveData = MutableLiveData<AudioFileScans>()
    private val audioFileLiveData1 = MutableLiveData<AudioFileScans>()
    private val audioFileLiveData2 = MutableLiveData<AudioFileScans>()

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
        listAudioFile.add(AudioFile(file1, "file_name1", 10000, 128, uri = Uri.parse(file1.absolutePath)))
        listAudioFile.add(AudioFile(file2, "file_name2", 10000, 128, uri = Uri.parse(file2.absolutePath)))
        listAudioFile.add(AudioFile(file2, "file_name2", 10000, 128, uri = Uri.parse(file2.absolutePath)))
        listAudioFile.add(AudioFile(file2, "file_name2", 10000, 128, uri = Uri.parse(file2.absolutePath)))
        val audioFileScans = AudioFileScans(listAudioFile, StateLoad.LOADDONE)
        audioFileLiveData.postValue(audioFileScans)

        listAudioFile1 = ArrayList<AudioFile>()
        listAudioFile1.add(AudioFile(file3, "file_name3", 10000, 128, uri = Uri.parse(file3.absolutePath)))
        listAudioFile1.add(AudioFile(file4, "file_name4", 10000, 128, uri = Uri.parse(file4.absolutePath)))
        listAudioFile1.add(AudioFile(file4, "file_name4", 10000, 128, uri = Uri.parse(file4.absolutePath)))
        listAudioFile1.add(AudioFile(file4, "file_name4", 10000, 128, uri = Uri.parse(file4.absolutePath)))
        val audioFileScans1 = AudioFileScans(listAudioFile1, StateLoad.LOADDONE)
        audioFileLiveData1.postValue(audioFileScans1)

        listAudioFile2 = ArrayList<AudioFile>()
        listAudioFile2.add(AudioFile(file1, "file_name5", 10000, 128, uri = Uri.parse(file1.absolutePath)))
        listAudioFile2.add(AudioFile(file2, "file_name6", 10000, 128, uri = Uri.parse(file2.absolutePath)))
        val audioFileScans2 = AudioFileScans(listAudioFile2, StateLoad.LOADDONE)
        audioFileLiveData2.postValue(audioFileScans2)


        CoroutineScope(Dispatchers.Main).launch {
            delay(10000)
            listAudioFile.add(AudioFile(file3, "Apple", 10000, 128, uri = Uri.parse(file3.absolutePath)))
//            listAudioFile.removeAt(0)
            val audioFileScans = AudioFileScans(listAudioFile, StateLoad.LOADDONE)
            audioFileLiveData.postValue(audioFileScans)
        }
    }

    override fun init(appContext: Context) {
        TODO("Not yet implemented")
    }

    override fun findAllAudioFiles(): LiveData<AudioFileScans> {
        return audioFileLiveData
    }

    override fun buildAudioFile(filePath: String): AudioFile {
        return AudioFile(File(filePath), "file_name1", 10000, 128)
    }

    override suspend fun saveFile(audioFile: AudioFile, typeFile: Folder): StateFile {
        TODO()
    }

    override fun deleteFile(items: List<AudioFile>, typeFile: Folder): Boolean {
        when (typeFile) {
            Folder.TYPE_CUTTER -> {
                items.forEach {
                    listAudioFile.remove(it)
                }
                val audioFileScans = AudioFileScans(listAudioFile)
                audioFileLiveData.postValue(audioFileScans)
                return true
            }
            Folder.TYPE_MERGER -> {
                items.forEach {
                    listAudioFile1.remove(it)
                }
                val audioFileScans = AudioFileScans(listAudioFile1)
                audioFileLiveData1.postValue(audioFileScans)
                return true
            }
            Folder.TYPE_MIXER -> {
                items.forEach {
                    listAudioFile2.remove(it)
                }
                val audioFileScans = AudioFileScans(listAudioFile2)
                audioFileLiveData2.postValue(audioFileScans)
                return true
            }
            else -> return false
        }
    }

    override fun getListAudioFileByType(typeFile: Folder): LiveData<AudioFileScans> {
        when (typeFile) {
            Folder.TYPE_CUTTER -> {
                return audioFileLiveData
            }
            Folder.TYPE_MERGER -> {
                return audioFileLiveData1
            }
            Folder.TYPE_MIXER -> {
                return audioFileLiveData2
            }
        }
        return audioFileLiveData
    }

    override fun getInfoAudioFile(itemFile: File?, type: Int): String? {
        TODO("Not yet implemented")
    }

    override fun getDateCreatFile(file: File?): String? {
        TODO("Not yet implemented")
    }

    override fun getParentFile(typeFile: Folder): String {
        TODO("Not yet implemented")
    }

    override fun getPathParentFileByName(name: String, typeFile: Folder): String {
        TODO("Not yet implemented")
    }

    fun getDurationByPath(itemFile: File?): String {
        return ""
    }
}