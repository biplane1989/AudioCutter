package com.example.core.core

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.arthenica.mobileffmpeg.Config
import com.arthenica.mobileffmpeg.FFmpeg
import com.arthenica.mobileffmpeg.Level
import com.example.core.Utils.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.*


class AudioCutterImpl : AudioCutter {
    private var audioFileUpdate = MutableLiveData<AudioMergingInfo>()
    private val itemMergeInfo = AudioMergingInfo(null, 0, FFMpegState.IDE)
    private var timeVideo: Long = 0
    private var audioFileCore = AudioCore()
    private val CODEC_MP3 = "libmp3lame"
    private val CODEC_AAC = "aac"

private val mainScope = MainScope()
    private val CMD_CUT_AUDIO_TIME =
        "-y -ss %d -i \'%s\' -c copy -t %d -b:a %dk -af \"volume='(between(t,0,%f)*(t/%d)+between(t,%d,%f)+between(t,%d,%d)*((%d-t)/(%d-%d)))*%f'\":eval=frame -c:a %s %s"
    private val CMD_CONCAT_AUDIO =
        "-y %s -filter_complex \"concat=n=%d:v=0:a=1[a]\" -map \"[a]\" -c:a %s -b:a %dk %s"
    private val CMD_MIX_AUDIO =
        "-y -i \'%s\' -i \'%s\' -filter_complex \"[0:0]volume=%f[a];[1:0]volume=%f[b];[a][b]amix=inputs=2:duration=%s:dropout_transition=0[a]\" -map \"[a]\" -c:a %s -q:a 0 %s"

    init {
        Config.setLogLevel(Level.AV_LOG_INFO)
        Config.enableStatisticsCallback {
            val percent = (it.time * 100) / timeVideo
            audioFileCore.size = it.size * 1204
            mainScope.launch {
                Log.e(TAG, "percent: $percent")
                updateItemLiveData(
                    audioFileCore,
                    ((it.time * 100) / timeVideo).toInt(),
                    FFMpegState.RUNNING
                )
            }
        }
    }

    override suspend fun cut(audioFile: AudioCore, audioCutConfig: AudioCutConfig): AudioCore {
        withContext(Dispatchers.Default) {
            timeVideo = (audioCutConfig.endPosition * 1000).toLong()
            val mimeType =
                if (audioCutConfig.format == AudioFormat.MP3) AudioFormat.MP3.type else AudioFormat.ACC.type
            val codec = if (audioCutConfig.format == AudioFormat.MP3) CODEC_MP3 else CODEC_AAC
            val fileCutPath =
                audioCutConfig.pathFolder.plus("/${audioCutConfig.fileName.plus(mimeType)}")


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
                codec,
                fileCutPath
            )
            Log.e(TAG, format)
            val returnCode = FFmpeg.execute(
                format
            )
            when (returnCode) {
                Config.RETURN_CODE_SUCCESS -> {
                    updateAudioFile(
                        fileCutPath,
                        audioCutConfig.fileName,
                        audioCutConfig.bitRate.value,
                        timeVideo,
                        mimeType
                    )
                    updateItemLiveData(audioFile, 100, FFMpegState.RUNNING)
                    return@withContext audioFileCore
                }
                Config.RETURN_CODE_CANCEL -> {
                    updateItemLiveData(audioFileCore, 0, FFMpegState.CANCEL)
                    Utils.deleteFile(fileCutPath)
                }
                else -> {
                    Log.e(
                        Config.TAG,
                        String.format("Async command execution failed with rc=%d.", returnCode)
                    )
                    updateItemLiveData(audioFileCore, 0, FFMpegState.FAIL)
                }
            }
        }


        return audioFileCore
    }

    override suspend fun mix(
        audioFile1: AudioCore, audioFile2: AudioCore, audioMixConfig: AudioMixConfig
    ): AudioCore {
        withContext(Dispatchers.Default) {
            val fileName = audioMixConfig.fileName
            val mimeType =
                if (audioMixConfig.format == AudioFormat.MP3) AudioFormat.MP3.type else AudioFormat.ACC.type
            val codec = if (audioMixConfig.format == AudioFormat.MP3) CODEC_MP3 else CODEC_AAC
            val filePath = audioMixConfig.pathFolder.plus("/${fileName.plus(mimeType)}")
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
                    audioMixConfig.selector.type, codec,
                    filePath
                )
            )
            when (requestCode) {
                Config.RETURN_CODE_SUCCESS -> {
                    updateAudioFile(
                        filePath,
                        fileName,
                        0,
                        timeVideo,
                        mimeType
                    )
                    updateItemLiveData(audioFileCore, 100, FFMpegState.RUNNING)
                    return@withContext audioFileCore
                }
                Config.RETURN_CODE_CANCEL -> {
                    updateItemLiveData(audioFileCore, 0, FFMpegState.CANCEL)
                    Utils.deleteFile(filePath)
                }
                else -> {
                    Log.e(
                        Config.TAG,
                        String.format("Async command execution failed with rc=%d.", requestCode)
                    )
                    updateItemLiveData(audioFileCore, 0, FFMpegState.FAIL)
                }
            }

        }
        return audioFileCore
    }

    override suspend fun cancelTask(): Boolean {
        FFmpeg.cancel()
        return true
    }

    override suspend fun merge(
        listAudioFile: List<AudioCore>,
        fileName: String,
        audioFormat: AudioFormat, pathFolder: String
    ): AudioCore {
        withContext(Dispatchers.Default) {
            val mimeType =
                if (audioFormat == AudioFormat.MP3) AudioFormat.MP3.type else AudioFormat.ACC.type
            val codec = if (audioFormat == AudioFormat.MP3) CODEC_MP3 else CODEC_AAC
            val sizeAudioFile = listAudioFile.size
            val pathFileMerge = pathFolder.plus("/${fileName.plus(mimeType)}")
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
                    sizeAudioFile, codec,
                    bitRateFile, pathFileMerge
                )
            )

            when (returnCode) {
                Config.RETURN_CODE_SUCCESS -> {
                    updateAudioFile(
                        pathFileMerge,
                        fileName,
                        bitRateFile,
                        timeVideo,
                        mimeType
                    )
                    updateItemLiveData(audioFileCore, 100, FFMpegState.RUNNING)
                    return@withContext audioFileCore
                }
                Config.RETURN_CODE_CANCEL -> {
                    updateItemLiveData(audioFileCore, 0, FFMpegState.CANCEL)
                    Utils.deleteFile(pathFileMerge)
                }
                else -> {
                    Log.e(
                        Config.TAG,
                        String.format("Async command execution failed with rc=%d.", returnCode)
                    )
                    updateItemLiveData(audioFileCore, 0, FFMpegState.FAIL)
                }
            }
        }
        return audioFileCore
    }

    override fun getAudioMergingInfo(): LiveData<AudioMergingInfo> {
        return audioFileUpdate
    }

    private fun updateItemLiveData(audioFile: AudioCore, percent: Int, state: FFMpegState) {
        itemMergeInfo.audioFile = audioFile
        itemMergeInfo.percent = percent
        itemMergeInfo.state = state
        audioFileUpdate.postValue(itemMergeInfo)
    }

    private fun updateAudioFile(
        path: String,
        name: String,
        bitRate: Int,
        fileTime: Long,
        fileType: String
    ) {
        val fileAudio = File(path)
        audioFileCore.file = fileAudio
        audioFileCore.fileName = name
        audioFileCore.size = fileAudio.length()
        audioFileCore.bitRate = bitRate
        audioFileCore.time = fileTime
        audioFileCore.mimeType = fileType
    }


    companion object {
        private const val TAG = "AudioCutterImpl"
    }
}