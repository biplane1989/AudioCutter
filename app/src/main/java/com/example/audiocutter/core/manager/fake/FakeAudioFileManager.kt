package com.example.audiocutter.core.manager.fake

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.audiocutter.core.audioManager.Folder
import com.example.audiocutter.core.audioManager.StateFile
import com.example.audiocutter.core.manager.AudioFileManager
import com.example.audiocutter.objects.AudioFile
import com.example.audiocutter.objects.AudioFileScans
import com.example.audiocutter.objects.StateLoad
import java.io.File

object FakeAudioFileManager : AudioFileManager {

    private val liveDataCut = MutableLiveData<AudioFileScans>()
    private val liveDataMix = MutableLiveData<AudioFileScans>()
    private val liveDataMer = MutableLiveData<AudioFileScans>()

    private var listCut: ArrayList<AudioFile>
    private var listMix: ArrayList<AudioFile>
    private var listMer: ArrayList<AudioFile>

//    val file = File(Environment.getExternalStorageDirectory().toString() + "/Music/Lonely.mp3")

    val file1 = File(Environment.getExternalStorageDirectory()
        .toString() + "/Download/doihoamattroi.mp3")
    val file2 = File(Environment.getExternalStorageDirectory().toString() + "/Download/xaodong.mp3")
    val file3 = File(Environment.getExternalStorageDirectory().toString() + "/Download/lonely.mp3")
    val file4 = File(Environment.getExternalStorageDirectory().toString() + "/Download/samlam.mp3")
    val fileMapTime = HashMap<String, Int>()

    fun getTime(file: File): Long {
        if (fileMapTime.contains(file.absolutePath)) {
            return fileMapTime.get(file.absolutePath)!!.toLong()
        }
        val mediaPlayer = MediaPlayer()
        mediaPlayer.setDataSource(file.absolutePath)
        mediaPlayer.prepare()
        fileMapTime.put(file.absolutePath, mediaPlayer.duration)
        mediaPlayer.release()
        return getTime(file)
    }

    init {
        listCut = ArrayList<AudioFile>()
        listCut.add(AudioFile(file1, "doihoamattroi", 10000, 128, uri = Uri.parse(file1.absolutePath), time = getTime(file1).toLong()))
        listCut.add(AudioFile(file2, "xaodong", 10000, 128, uri = Uri.parse(file2.absolutePath), time = getTime(file2).toLong()))
        listCut.add(AudioFile(file3, "lonely", 10000, 128, uri = Uri.parse(file3.absolutePath), time = getTime(file3).toLong()))
        listCut.add(AudioFile(file4, "samlam", 10000, 128, uri = Uri.parse(file4.absolutePath), time = getTime(file4).toLong()))
        val audioFileScans = AudioFileScans(listCut, StateLoad.LOADDONE)
        liveDataCut.postValue(audioFileScans)

        listMix = ArrayList<AudioFile>()
        listMix.add(AudioFile(file1, "doihoamattroi", 10000, 128, uri = Uri.parse(file1.absolutePath), time = getTime(file1).toLong()))
        listMix.add(AudioFile(file2, "xaodong", 10000, 128, uri = Uri.parse(file2.absolutePath), time = getTime(file2).toLong()))
        listMix.add(AudioFile(file3, "lonely", 10000, 128, uri = Uri.parse(file3.absolutePath), time = getTime(file3).toLong()))
        listMix.add(AudioFile(file4, "samlam", 10000, 128, uri = Uri.parse(file4.absolutePath), time = getTime(file4).toLong()))
        val audioFileScans1 = AudioFileScans(listMix, StateLoad.LOADDONE)
        liveDataMix.postValue(audioFileScans1)

        listMer = ArrayList<AudioFile>()
        listMer.add(AudioFile(file1, "doihoamattroi", 10000, 128, uri = Uri.parse(file1.absolutePath), time = getTime(file1).toLong()))
        listMer.add(AudioFile(file2, "xaodong", 10000, 128, uri = Uri.parse(file2.absolutePath), time = getTime(file2).toLong()))
        listMer.add(AudioFile(file3, "lonely", 10000, 128, uri = Uri.parse(file3.absolutePath), time = getTime(file3).toLong()))
        listMer.add(AudioFile(file4, "samlam", 10000, 128, uri = Uri.parse(file4.absolutePath), time = getTime(file4).toLong()))
        val audioFileScans2 = AudioFileScans(listMer, StateLoad.LOADDONE)
        liveDataMer.postValue(audioFileScans2)


//        CoroutineScope(Dispatchers.Main).launch {
//            delay(10000)
//            listAudioFile.add(AudioFile(file3, "Apple", 10000, 128, uri = Uri.parse(file3.absolutePath)))
////            listAudioFile.removeAt(0)
//            val audioFileScans = AudioFileScans(listAudioFile, StateLoad.LOADDONE)
//            audioFileLiveData.postValue(audioFileScans)
//        }
    }

    fun addCut(audioFile: AudioFile) {
        listCut.add(audioFile)
        val audioFileScans = AudioFileScans(listCut, StateLoad.LOADDONE)
        liveDataCut.postValue(audioFileScans)
    }

    fun addMix(audioFile: AudioFile) {
        listMix.add(audioFile)
        val audioFileScans = AudioFileScans(listMix, StateLoad.LOADDONE)
        Log.d("giangtd", "addMix: list Fake size: " + listMix.size)
        liveDataMix.postValue(audioFileScans)
    }

    fun addMer(audioFile: AudioFile) {
        listMer.add(audioFile)
        val audioFileScans = AudioFileScans(listMer, StateLoad.LOADDONE)
        liveDataMer.postValue(audioFileScans)
    }


    override fun init(appContext: Context) {
//        TODO("Not yet implemented")
    }

    override fun findAllAudioFiles(): LiveData<AudioFileScans> {
        return liveDataCut
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
                    listCut.remove(it)
                }
                val audioFileScans = AudioFileScans(listCut)
                liveDataCut.postValue(audioFileScans)
                return true
            }
            Folder.TYPE_MERGER -> {
                items.forEach {
                    listMix.remove(it)
                }
                val audioFileScans = AudioFileScans(listMix)
                liveDataMix.postValue(audioFileScans)
                return true
            }
            Folder.TYPE_MIXER -> {
                items.forEach {
                    listMer.remove(it)
                }
                val audioFileScans = AudioFileScans(listMer)
                liveDataMer.postValue(audioFileScans)
                return true
            }
            else -> return false
        }
    }

    override fun getListAudioFileByType(typeFile: Folder): LiveData<AudioFileScans> {
        when (typeFile) {
            Folder.TYPE_CUTTER -> {
                return liveDataCut
            }
            Folder.TYPE_MERGER -> {
                return liveDataMer
            }
            Folder.TYPE_MIXER -> {
                return liveDataMix
            }
        }
        return liveDataCut
    }

    override fun getInfoAudioFile(itemFile: File?, type: Int): String? {
        return "1236598"
    }

    override fun getDateCreatFile(file: File?): String? {
        return "orange"
    }

    override fun getParentFile(typeFile: Folder): String {
        return "orange"
    }

    override fun getPathParentFileByName(name: String, typeFile: Folder): String {
        return "orange"
    }

    fun getDurationByPath(itemFile: File?): String {
        return ""
    }
}