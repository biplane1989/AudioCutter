package com.example.audiocutter.core.audioCutter

import android.os.Environment
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.arthenica.mobileffmpeg.Config
import com.arthenica.mobileffmpeg.FFmpeg
import com.arthenica.mobileffmpeg.Level
import com.example.audiocutter.core.manager.AudioCutConfig
import com.example.audiocutter.core.manager.AudioCutter
import com.example.audiocutter.core.manager.AudioMergingInfo
import com.example.audiocutter.core.manager.AudioMixConfig
import com.example.audiocutter.objects.AudioFile
import com.example.audiocutter.util.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.util.*


class AudioCutterImpl : AudioCutter {
    private var audioFileUpdate = MutableLiveData<AudioMergingInfo>()
    private val itemMergeInfo = AudioMergingInfo(null, 0)

    private val PATH_DEFAUL_FOLDER = "${Environment.getExternalStorageDirectory()}/AudioCutter/"
    private val PATH_CUT_FOLDER = "${PATH_DEFAUL_FOLDER}cutter"
    private val PATH_MERGE_FOLDER = "${PATH_DEFAUL_FOLDER}merger"
    private val PATH_MIXER_FOLDER = "${PATH_DEFAUL_FOLDER}mixer"

    private val CMD_CUT_AUDIO_TIME =
        "-y -ss %d -i %s -t %d -b:a %dk -af volume='between(t,0,%f)*(t/%d)+between(t,%d,%f)+between(t,%d,%d)*((%d-t)/(%d-%d))':eval=frame %s"

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
            Log.e(TAG, it.toString())
        }
    }

    //between(t, 0, volumeStart) * (t / volumeStart) + between(t, volumeStart + 1, endTime - volumeEnd) + between(t, endTime - volumeEnd, endTime) * ((endTime - t) / (endTime - volumeEnd));
    override suspend fun cut(audioFile: AudioFile, audioCutConfig: AudioCutConfig): AudioFile {
        var audioFileCut = audioFile

        val fileNameReplace = audioFile.fileName.replace(" ", "").trim().plus(audioFile.mimeType)
        audioFileCut.fileName = audioCutConfig.fileName.plus(audioFile.mimeType)
        audioFileCut.bitRate = audioCutConfig.bitRate.value
        audioFileCut.time = audioCutConfig.endPosition.toLong()

//        var itemFile = Utils.changeNameFile(audioFile, fileNameReplace)

        updateItemLiveData(audioFileCut, 0)

        val format = String.format(
            Locale.ENGLISH,
            CMD_CUT_AUDIO_TIME,
            audioCutConfig.startPosition,
            audioFile.file.absolutePath.replace(" ", "%20"),
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
            PATH_CUT_FOLDER.plus("/${audioFileCut.fileName}")
        )

        val returnCode = FFmpeg.executeAsync(
            format
        ) { id, returnCode ->
            when (returnCode) {
                Config.RETURN_CODE_SUCCESS -> {
                    Log.e(Config.TAG, "Async command execution completed successfully.")
                }
                Config.RETURN_CODE_CANCEL -> {
                    Log.e(Config.TAG, "Async command execution cancelled by user.")
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

        return dataProcessing(audioFile)
    }

    override suspend fun mix(
        audioFile1: AudioFile,
        audioFile2: AudioFile,
        audioMixConfig: AudioMixConfig
    ): AudioFile {
       return dataProcessing(audioFile1)
    }

    override suspend fun merge(listAudioFile: List<AudioFile>, fileName: String): AudioFile {
        return dataProcessing(listAudioFile[0])
    }

    override fun getAudioMergingInfo(): LiveData<AudioMergingInfo> {
        return audioFileUpdate
    }

    private suspend fun dataProcessing(audioFile: AudioFile): AudioFile {
        return withContext(Dispatchers.Default) {
            var count = 0
            while (count <= 10) {
                updateItemLiveData(audioFile, count * 10)
                delay(1000)
                count++
            }
            audioFile
        }
    }

    private fun updateItemLiveData(audioFile: AudioFile, percent: Int) {
        itemMergeInfo.audioFile = audioFile
        itemMergeInfo.percent = percent
        audioFileUpdate.postValue(itemMergeInfo)
    }

    companion object {
        private const val TAG = "AudioCutterImpl"
    }
}