package com.example.core.utils

import android.util.Log
import com.arthenica.mobileffmpeg.FFprobe
import com.example.core.core.AudioInfor
import org.json.JSONException
import java.io.File

class FileUtil {
    companion object {
        const val TAG = "FileUtil"
        private const val KEY_MEDIA_TITLE = "title"
        private const val KEY_MEDIA_ARTIST = "artist"
        private const val KEY_MEDIA_GENRE = "genre"
        private const val KEY_MEDIA_COMPOSER = "composer"
        private const val KEY_MEDIA_ALBUM = "album"


        fun checkFileIsExits(pathFile: String): Boolean {
            return if (pathFile.isEmpty()) {
                false
            } else {
                File(pathFile).exists()
            }
        }

        fun createFolder(pathFolder: Array<String>) {
            pathFolder.forEach {
                if (!checkFileIsExits(it)) {
                    File(it).mkdirs()
                }
            }
        }

        fun deleteFile(pathFile: String): Boolean {
            val file = File(pathFile)
            if (file.exists()) {
                return file.delete()
            }
            return false
        }

        fun getAudioInfo(filePath: String): AudioInfor? {
            try {
                val info = FFprobe.getMediaInformation(filePath)
                info?.let {
                    val duration = it.duration.replace(".", "").toLong() / 1000

                    return AudioInfor(
                        it.filename,
                        it.bitrate.toInt(),
                        duration,
                        it.size.toLong(),
                        filePath,
                        it.format,
                        it.getStringProperty(KEY_MEDIA_TITLE),
                        it.getStringProperty(KEY_MEDIA_ALBUM),
                        it.getStringProperty(KEY_MEDIA_ARTIST),
                        it.getStringProperty(KEY_MEDIA_GENRE)
                    )
                }
            } catch (e: Exception) {
                Log.d(TAG, "error message filePath ${filePath} ${e.message}")
            } catch (e: JSONException) {
                Log.d(TAG, "error message filePath ${filePath} ${e.message}")
            }
            return null
        }

    }

}