package com.example.audiocutter.objects

import android.graphics.Bitmap
import android.net.Uri
import java.io.File
import java.io.Serializable

data class AudioFile(
    var file: File,
    var fileName: String,
    var size: Long,
    var bitRate: Int = 128,
    var time: Long = 0,
    var uri: Uri? = null,
    var bitmap: Bitmap? = null,
    var title: String? = "",
    var alBum: String? = "",
    var artist: String? = "",
    var dateAdded: String? = "",
    var genre: String? = "",
    var mimeType: String? = ""
) : Serializable {
    override fun equals(other: Any?): Boolean {
        if (other is AudioFile) {
            return file.absolutePath == other.file.absolutePath
        }
        return super.equals(other)
    }
}
