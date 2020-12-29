package com.example.core.core

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.arthenica.mobileffmpeg.Config
import com.arthenica.mobileffmpeg.FFmpeg
import com.arthenica.mobileffmpeg.FFprobe
import com.arthenica.mobileffmpeg.Level
import com.example.core.utils.FileUtil
import kotlinx.coroutines.*
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
        "-y -ss %f -i \'%s\' -c copy -t %f -b:a %dk -af \"volume='(between(t,0,%f)*(t/%d)+between(t,%d,%f)+between(t,%d,%d)*((%d-t)/(%d-%d)))*%f'\":eval=frame -c:a %s \"%s\""
    private val CMD_CUT_AUDIO_TIME_NOT_COPY =
        "-y -ss %f -i \'%s\' -t %f -b:a %dk -af \"volume='(between(t,0,%f)*(t/%d)+between(t,%d,%f)+between(t,%d,%d)*((%d-t)/(%d-%d)))*%f'\":eval=frame -c:a %s \"%s\""

    private val CMD_CUT_AUDIO_FADE_IN_OFF =
        "-y -ss %f -i \'%s\' -c copy -t %f -b:a %dk -af \"volume='(between(t,%d,%f)+between(t,%d,%d)*((%d-t)/(%d-%d)))*%f'\":eval=frame -c:a %s \"%s\""
    private val CMD_CUT_AUDIO_FADE_IN_OFF_NOT_COPY =
        "-y -ss %f -i \'%s\' -t %f -b:a %dk -af \"volume='(between(t,%d,%f)+between(t,%d,%d)*((%d-t)/(%d-%d)))*%f'\":eval=frame -c:a %s \"%s\""

    private val CMD_CUT_AUDIO_TIME_FADE_OUT_OFF =
        "-y -ss %f -i \'%s\' -c copy -t %f -b:a %dk -af \"volume='(between(t,0,%f)*(t/%d)+between(t,%d,%f))*%f'\":eval=frame -c:a %s \"%s\""
    private val CMD_CUT_AUDIO_TIME_FADE_OUT_OFF_NOT_COPY =
        "-y -ss %f -i \'%s\' -t %f -b:a %dk -af \"volume='(between(t,0,%f)*(t/%d)+between(t,%d,%f))*%f'\":eval=frame -c:a %s \"%s\""

    private val CMD_CUT_AUDIO_TIME_FADE_OFF =
        "-y -ss %f -i \'%s\' -c copy -t %f -b:a %dk -af \"volume='(1*%f)'\":eval=frame -c:a %s \"%s\""
    private val CMD_CUT_AUDIO_TIME_FADE_OFF_NOT_COPY =
        "-y -ss %f -i \'%s\' -t %f -b:a %dk -af \"volume='(1*%f)'\":eval=frame -c:a %s \"%s\""

    private val CMD_CONCAT_AUDIO =
        "-y %s -filter_complex \"concat=n=%d:v=0:a=1[a]\" -map \"[a]\" -c:a %s -b:a %dk \"%s\""
    private val CMD_MIX_AUDIO =
        "-y -i \'%s\' -i \'%s\' -filter_complex \"[0:0]volume=%f[a];[1:0]volume=%f[b];[a][b]amix=inputs=2:duration=%s:dropout_transition=0[a]\" -map \"[a]\" -c:a %s -q:a 0 \"%s\""

    private var lastTime = 0        // dung de luu lai tri tri time cua lan editor truoc

    init {
        mainScope.launch {
            Config.setLogLevel(Level.AV_LOG_INFO)
            Config.enableStatisticsCallback {
                if (timeVideo == 0L) {
                    return@enableStatisticsCallback
                }

                if (it.time == lastTime) {          // do moi lan editor se bi luu lai gia tri cuoi cung cua lan truoc, nen dung bien lastTime de loai truong hopo nay
                    Log.d(TAG, "lastTime  @@@@@@@@@@@: " + lastTime)
                    return@enableStatisticsCallback
                }

                val percent = (it.time * 100) / timeVideo
                Log.d(TAG, "time: " + it.time)
                if (itemMergeInfo.state == FFMpegState.IDE && percent >= 100L) {
                    return@enableStatisticsCallback
                }
                audioFileCore.size = it.size * 1204
//                mainScope.launch {
                Log.e(
                    TAG,
                    "percent: $percent status: ${itemMergeInfo.state}   time: ${it.time}   timeVideo: $timeVideo"
                )
                if (itemMergeInfo.state != FFMpegState.CANCEL && itemMergeInfo.state != FFMpegState.FAIL && itemMergeInfo.state != FFMpegState.SUCCESS) {
                    updateItemLiveData(
                        audioFileCore,
                        ((it.time * 100) / timeVideo).toInt(),
                        FFMpegState.RUNNING
                    )
                    lastTime = it.time
                }
//                }
            }
        }
    }

    override suspend fun cut(audioFile: AudioCore, audioCutConfig: AudioCutConfig): AudioCore {
        withContext(Dispatchers.Default) {
            updateItemLiveData(audioFile, 0, FFMpegState.IDE)
            timeVideo = (audioCutConfig.endPosition * 1000).toLong()
            val mimeType =
                if (audioCutConfig.format == AudioFormat.MP3) AudioFormat.MP3.type else AudioFormat.AAC.type
            val codec = if (audioCutConfig.format == AudioFormat.MP3) CODEC_MP3 else CODEC_AAC
            val fileCutPath =
                audioCutConfig.pathFolder.plus("/${audioCutConfig.fileName.plus(mimeType)}")


            val format = getStringFormat(audioCutConfig, audioFile, codec, fileCutPath)
            Log.e(TAG, format)
            val returnCode = FFmpeg.execute(format)
            when (returnCode) {
                Config.RETURN_CODE_SUCCESS -> {
                    updateAudioFile(
                        fileCutPath,
                        audioCutConfig.fileName,
                        audioCutConfig.bitRate.value,
                        timeVideo,
                        mimeType
                    )
                    updateItemLiveData(audioFile, 100, FFMpegState.SUCCESS)
                    return@withContext audioFileCore
                }
                Config.RETURN_CODE_CANCEL -> {
                    updateItemLiveData(audioFileCore, 0, FFMpegState.CANCEL)
                    FileUtil.deleteFile(fileCutPath)
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

    private fun getStringFormat(
        audioCutConfig: AudioCutConfig,
        audioFile: AudioCore,
        codec: String,
        fileCutPath: String
    ): String {

        if (audioCutConfig.inEffect != Effect.OFF && audioCutConfig.outEffect != Effect.OFF) {
            return String.format(
                Locale.ENGLISH,
                if (audioFile.bitRate != audioCutConfig.bitRate.value) CMD_CUT_AUDIO_TIME_NOT_COPY else CMD_CUT_AUDIO_TIME,
                audioCutConfig.startPosition,
                audioFile.file.absolutePath,
                audioCutConfig.endPosition,
                audioCutConfig.bitRate.value,
                if (audioCutConfig.inEffect.time == 0) 0f else (audioCutConfig.inEffect.time - 0.0001).toFloat(),
                audioCutConfig.inEffect.time,
                audioCutConfig.inEffect.time,
                ((audioCutConfig.endPosition.toInt() - audioCutConfig.outEffect.time) - 0.0001).toFloat(),
                audioCutConfig.endPosition.toInt() - audioCutConfig.outEffect.time,
                audioCutConfig.endPosition.toInt(),
                audioCutConfig.endPosition.toInt(),
                audioCutConfig.endPosition.toInt(),
                (audioCutConfig.endPosition.toInt() - audioCutConfig.outEffect.time),
                audioCutConfig.volumePercent / 100f,
                codec,
                fileCutPath
            )
        } else if (audioCutConfig.inEffect == Effect.OFF && audioCutConfig.outEffect == Effect.OFF) {
            return String.format(
                Locale.ENGLISH,
                if (audioFile.bitRate != audioCutConfig.bitRate.value) CMD_CUT_AUDIO_TIME_FADE_OFF_NOT_COPY else CMD_CUT_AUDIO_TIME_FADE_OFF,
                audioCutConfig.startPosition,
                audioFile.file.absolutePath,
                audioCutConfig.endPosition,
                audioCutConfig.bitRate.value,
                audioCutConfig.volumePercent / 100f,
                codec,
                fileCutPath
            )
        } else if (audioCutConfig.inEffect == Effect.OFF) {
            return String.format(
                Locale.ENGLISH,
                if (audioFile.bitRate != audioCutConfig.bitRate.value) CMD_CUT_AUDIO_FADE_IN_OFF_NOT_COPY else CMD_CUT_AUDIO_FADE_IN_OFF,
                audioCutConfig.startPosition,
                audioFile.file.absolutePath,
                audioCutConfig.endPosition,
                audioCutConfig.bitRate.value,
                0,
                ((audioCutConfig.endPosition.toInt() - audioCutConfig.outEffect.time) - 0.0001).toFloat(),
                audioCutConfig.endPosition.toInt() - audioCutConfig.outEffect.time,
                audioCutConfig.endPosition.toInt(),
                audioCutConfig.endPosition.toInt(),
                audioCutConfig.endPosition.toInt(),
                (audioCutConfig.endPosition.toInt() - audioCutConfig.outEffect.time),
                audioCutConfig.volumePercent / 100f,
                codec,
                fileCutPath
            )
        } else {
            return String.format(
                Locale.ENGLISH,
                if (audioFile.bitRate != audioCutConfig.bitRate.value) CMD_CUT_AUDIO_TIME_FADE_OUT_OFF_NOT_COPY else CMD_CUT_AUDIO_TIME_FADE_OUT_OFF,
                audioCutConfig.startPosition,
                audioFile.file.absolutePath,
                audioCutConfig.endPosition,
                audioCutConfig.bitRate.value,
                if (audioCutConfig.inEffect.time == 0) 0f else (audioCutConfig.inEffect.time - 0.0001).toFloat(),
                audioCutConfig.inEffect.time,
                audioCutConfig.inEffect.time,
                audioCutConfig.endPosition,
                audioCutConfig.volumePercent / 100f,
                codec,
                fileCutPath
            )
        }
    }

    override suspend fun mix(
        audioFile1: AudioCore,
        audioFile2: AudioCore,
        audioMixConfig: AudioMixConfig
    ): AudioCore {
        withContext(Dispatchers.Default) {
            updateItemLiveData(audioFileCore, 0, FFMpegState.IDE)

            val fileName = audioMixConfig.fileName
            val mimeType =
                if (audioMixConfig.format == AudioFormat.MP3) AudioFormat.MP3.type else AudioFormat.AAC.type
            val codec = if (audioMixConfig.format == AudioFormat.MP3) CODEC_MP3 else CODEC_AAC
            val filePath = audioMixConfig.pathFolder.plus("/${fileName.plus(mimeType)}")
            timeVideo = if (audioMixConfig.selector == MixSelector.LONGEST) {
                if (audioFile1.time - audioFile2.time >= 0) audioFile1.time else audioFile2.time
            } else {
                if (audioFile1.time - audioFile2.time >= 0) audioFile2.time else audioFile1.time
            }

            val requestCode = FFmpeg.execute(
                String.format(
                    Locale.ENGLISH,
                    CMD_MIX_AUDIO,
                    audioFile1.file.absolutePath,
                    audioFile2.file.absolutePath,
                    (audioMixConfig.volumePercent1 / 100).toFloat(),
                    (audioMixConfig.volumePercent2 / 100).toFloat(),
                    audioMixConfig.selector.type,
                    codec,
                    filePath
                )
            )


            when (requestCode) {
                Config.RETURN_CODE_SUCCESS -> {
                    Log.d(TAG, "mix: RETURN_CODE_SUCCESS")
                    updateAudioFile(filePath, fileName, 0, timeVideo, mimeType)
                    updateItemLiveData(audioFileCore, 100, FFMpegState.SUCCESS)
                    return@withContext audioFileCore
                }
                Config.RETURN_CODE_CANCEL -> {
                    updateItemLiveData(audioFileCore, 0, FFMpegState.CANCEL)
                    FileUtil.deleteFile(filePath)
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
        audioFormat: AudioFormat,
        pathFolder: String
    ): AudioCore {
        timeVideo = 0

        withContext(Dispatchers.Default) {

            updateItemLiveData(audioFileCore, 0, FFMpegState.IDE)

            val mimeType =
                if (audioFormat == AudioFormat.MP3) AudioFormat.MP3.type else AudioFormat.AAC.type
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
                    sizeAudioFile,
                    codec,
                    bitRateFile,
                    pathFileMerge
                )
            )


            when (returnCode) {
                Config.RETURN_CODE_SUCCESS -> {
                    updateAudioFile(pathFileMerge, fileName, bitRateFile, timeVideo, mimeType)
                    updateItemLiveData(audioFileCore, 100, FFMpegState.SUCCESS)
                    return@withContext audioFileCore
                }
                Config.RETURN_CODE_CANCEL -> {
                    updateItemLiveData(audioFileCore, 0, FFMpegState.CANCEL)
                    FileUtil.deleteFile(pathFileMerge)
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

        Log.d(TAG, "updateItemLiveData:  percent: " + percent + " state : " + state)
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