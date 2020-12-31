package com.example.audiocutter.objects

import android.graphics.Bitmap
import android.net.Uri
import java.io.File
import java.util.*

class AudioFile(var file: File, var fileName: String, var size: Long, var bitRate: Int = 128, var duration: Long = 0, var uri: Uri? = null, var bitmap: Bitmap? = null, var title: String? = "", var alBum: String? = "", var artist: String? = "", var modified: Long = -1, var genre: String? = "", var mimeType: String? = "", var modifiedStr: String = "") {
    fun getFilePath(): String {
        return file.absolutePath
    }

    init {
        if (modified == -1L) {
            modifiedStr = ""
        }
        val time = Date(modified)
        val cal = Calendar.getInstance()
        cal.time = time
        val day = cal.get(Calendar.DAY_OF_MONTH)
        val month = cal.get(Calendar.MONTH)
        val year = cal.get(Calendar.YEAR)
        val hours = cal.get(Calendar.HOUR_OF_DAY)
        val minutes = cal.get(Calendar.MINUTE)
        modifiedStr = "$hours:$minutes , $day/$month/$year"

    }

    override fun equals(other: Any?): Boolean {
        if (other is AudioFile) {

            return getFilePath() == other.getFilePath() && size == other.size && bitRate == other.bitRate && uri == other.uri && modified == other.modified && mimeType == other.mimeType

        }
        return super.equals(other)
    }

    override fun hashCode(): Int {
        return getFilePath().hashCode()
    }

    fun copy(audioFile: AudioFile) {
        file = audioFile.file
        fileName = audioFile.fileName
        size = audioFile.size
        bitRate = audioFile.bitRate
        duration = audioFile.duration
        uri = audioFile.uri
        bitmap = audioFile.bitmap
        title = audioFile.title
        alBum = audioFile.alBum
        artist = audioFile.artist
        modified = audioFile.modified
        genre = audioFile.genre
        mimeType = audioFile.mimeType
        modifiedStr = audioFile.modifiedStr
    }
}

