package com.example.audiocutter.core.audioCutter

import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.arthenica.mobileffmpeg.Config
import com.arthenica.mobileffmpeg.FFmpeg
import com.arthenica.mobileffmpeg.Level
import com.example.audiocutter.core.manager.*
import com.example.audiocutter.objects.AudioFile
import com.example.audiocutter.util.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.*


class AudioCutterImpl : AudioCutter {
    private var audioFileUpdate = MutableLiveData<AudioMergingInfo>()
    private val itemMergeInfo = AudioMergingInfo(null, 0)
    private var timeVideo: Long = 0
    private var audioFileCore = AudioFile()

    private val PATH_DEFAUL_FOLDER = "${Environment.getExternalStorageDirectory()}/AudioCutter/"
    private val PATH_CUT_FOLDER = "${PATH_DEFAUL_FOLDER}cutter"
    private val PATH_MERGE_FOLDER = "${PATH_DEFAUL_FOLDER}merger"
    private val PATH_MIXER_FOLDER = "${PATH_DEFAUL_FOLDER}mixer"
    private val mainScope = MainScope()
    private val CMD_CUT_AUDIO_TIME =
        "-y -ss %d -i \'%s\' -t %d -b:a %dk -af \"volume='(between(t,0,%f)*(t/%d)+between(t,%d,%f)+between(t,%d,%d)*((%d-t)/(%d-%d)))*%f'\":eval=frame %s"
    private val CMD_CONCAT_AUDIO =
        "-y %s -filter_complex \"concat=n=%d:v=0:a=1[a]\" -map \"[a]\" -codec:a libmp3lame -b:a %dk %s"
    private val CMD_MIX_AUDIO =
        "-y -i \'%s\' -i \'%s\' -filter_complex \"[0:0]volume=%f[a];[1:0]volume=%f[b];[a][b]amix=inputs=2:duration=%s:dropout_transition=0[a]\" -map \"[a]\" -c:a libmp3lame -q:a 0 %s"

    init {
        Utils.createFolder(
            arrayOf(
                PATH_DEFAUL_FOLDER,
                PATH_CUT_FOLDER,
                PATH_MERGE_FOLDER,
                PATH_MIXER_FOLDER
            )
        )
        Config.setLogLevel(Level.AV_LOG_INFO)
        Config.enableStatisticsCallback {
            val percent = (it.time * 100) / timeVideo
            audioFileCore.size = it.size * 1204
            mainScope.launch {
                Log.e(TAG, "percent: $percent")
                updateItemLiveData(audioFileCore, ((it.time * 100) / timeVideo).toInt())
            }
        }
    }

    override suspend fun cut(audioFile: AudioFile, audioCutConfig: AudioCutConfig): AudioFile {
        withContext(Dispatchers.Default) {
            timeVideo = (audioCutConfig.endPosition * 1000).toLong()
            val fileCutPath =
                PATH_CUT_FOLDER.plus("/${audioCutConfig.fileName.plus(audioFile.mimeType)}")

            updateItemLiveData(audioFileCore, 0)

            val format = String.format(
                Locale.ENGLISH,
                CMD_CUT_AUDIO_TIME,
                audioCutConfig.startPosition,
                audioFile.file.absolutePath,
                audioCutConfig.endPosition,
                audioCutConfig.bitRate.value,
                if (audioCutConfig.inEffect.time == 0) 0f else (audioCutConfig.inEffect.time - 0.0001).toFloat(),
                audioCutConfig.inEffect.time,
                audioCutConfig.inEffect.time,
                ((audioCutConfig.endPosition - audioCutConfig.outEffect.time) - 0.0001).toFloat(),
                audioCutConfig.endPosition - audioCutConfig.outEffect.time,
                audioCutConfig.endPosition,
                audioCutConfig.endPosition,
                audioCutConfig.endPosition,
                (audioCutConfig.endPosition - audioCutConfig.outEffect.time),
                (audioCutConfig.volumePercent / 100).toFloat(),
                fileCutPath
            )

            val returnCode = FFmpeg.execute(
                format
            )
            when (returnCode) {
                Config.RETURN_CODE_SUCCESS -> {
                    updateAudioFile(
                        fileCutPath,
                        audioCutConfig.fileName,
                        audioCutConfig.bitRate.value,
                        null,
                        timeVideo,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        audioFile.mimeType!!
                    )
                    updateItemLiveData(audioFile, 100)
                    return@withContext audioFileCore
                }
                Config.RETURN_CODE_CANCEL -> {
                    Utils.deleteFile(fileCutPath)
                }
                else -> {
                    Log.e(
                        Config.TAG,
                        String.format("Async command execution failed with rc=%d.", returnCode)
                    )
                    Config.printLastCommandOutput(Log.ERROR)
                }
            }
        }


        return audioFileCore
    }

    override suspend fun mix(
        audioFile1: AudioFile, audioFile2: AudioFile, audioMixConfig: AudioMixConfig
    ): AudioFile {
        withContext(Dispatchers.Default) {
            val fileName = audioMixConfig.fileName
            val mimeType = ".mp3"
            val filePath = PATH_MIXER_FOLDER.plus("/${fileName.plus(mimeType)}")
            timeVideo = if (audioMixConfig.selector == MixSelector.LONGEST) {
                if (audioFile1.time - audioFile2.time >= 0) audioFile1.time else audioFile2.time
            } else {
                if (audioFile1.time - audioFile2.time >= 0) audioFile2.time else audioFile1.time
            }
            var requestCode = FFmpeg.execute(
                String.format(
                    Locale.ENGLISH,
                    CMD_MIX_AUDIO,
                    audioFile1.file.absolutePath,
                    audioFile2.file.absolutePath,
                    (audioMixConfig.volumePercent1 / 100).toFloat(),
                    (audioMixConfig.volumePercent2 / 100).toFloat(),
                    audioMixConfig.selector.type,
                    filePath
                )
            )
            when (requestCode) {
                Config.RETURN_CODE_SUCCESS -> {
                    updateAudioFile(
                        filePath,
                        fileName,
                        0,
                        null,
                        timeVideo,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        mimeType
                    )
                    updateItemLiveData(audioFileCore, 100)
                    return@withContext audioFileCore
                }
                Config.RETURN_CODE_CANCEL -> {
                    Utils.deleteFile(filePath)
                }
                else -> {
                    Log.e(
                        Config.TAG,
                        String.format("Async command execution failed with rc=%d.", requestCode)
                    )
                    Config.printLastCommandOutput(Log.ERROR)
                }
            }

        }
        return audioFileCore
    }

    override suspend fun cancelTask() {
        FFmpeg.cancel()
    }

    override suspend fun merge(listAudioFile: List<AudioFile>, fileName: String): AudioFile {
        withContext(Dispatchers.Default) {
            val mimeType = ".mp3"
            val sizeAudioFile = listAudioFile.size
            val pathFileMerge = PATH_MERGE_FOLDER.plus("/${fileName.plus(mimeType)}")
            val bitRateFile = 256

            var cmd_input = ""
            listAudioFile.forEach {
                timeVideo += it.time
                cmd_input += (" -i '${it.file.absolutePath}'")
            }
            val returnCode = FFmpeg.execute(
                String.format(
                    Locale.ENGLISH,
                    CMD_CONCAT_AUDIO,
                    cmd_input,
                    sizeAudioFile,
                    bitRateFile, pathFileMerge
                )
            )

            when (returnCode) {
                Config.RETURN_CODE_SUCCESS -> {
                    updateAudioFile(
                        pathFileMerge,
                        fileName,
                        bitRateFile,
                        null,
                        timeVideo,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        mimeType
                    )
                    updateItemLiveData(audioFileCore, 100)
                    return@withContext audioFileCore
                }
                Config.RETURN_CODE_CANCEL -> {
                    Utils.deleteFile(pathFileMerge)
                }
                else -> {
                    Log.e(
                        Config.TAG,
                        String.format("Async command execution failed with rc=%d.", returnCode)
                    )
                    Config.printLastCommandOutput(Log.ERROR)
                }
            }
        }
        return audioFileCore
    }

    override fun getAudioMergingInfo(): LiveData<AudioMergingInfo> {
        return audioFileUpdate
    }

    private fun updateItemLiveData(audioFile: AudioFile, percent: Int) {
        itemMergeInfo.audioFile = audioFile
        itemMergeInfo.percent = percent
        audioFileUpdate.postValue(itemMergeInfo)
    }

    private fun updateAudioFile(
        path: String,
        name: String,
        bitRate: Int,
        fileUri: Uri?, fileTime: Long,
        fileBitmap: Bitmap?,
        fileTitle: String?,
        fileArtist: String?,
        fileAlbum: String?, fileDate: String?, fileGenre: String?, fileType: String
    ) {
        val fileAudio = File(path)
        audioFileCore.file = fileAudio
        audioFileCore.fileName = name
        audioFileCore.size = fileAudio.length()
        audioFileCore.bitRate = bitRate
        audioFileCore.time = fileTime
        audioFileCore.uri = fileUri
        audioFileCore.bitmap = fileBitmap
        audioFileCore.title = fileTitle
        audioFileCore.alBum = fileAlbum
        audioFileCore.artist = fileArtist
        audioFileCore.dateAdded = fileDate
        audioFileCore.genre = fileGenre
        audioFileCore.mimeType = fileType
    }

    companion object {
        private const val TAG = "AudioCutterImpl"
    }
}