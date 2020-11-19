package com.example.audiocutter.objects

import android.graphics.Bitmap
import android.net.Uri
import java.io.File
import java.util.*

class AudioFile(
    val file: File,
    val fileName: String,
    val size: Long,
    val bitRate: Int = 128,
    var duration: Long = 0,
    var uri: Uri? = null,
    val bitmap: Bitmap? = null,
    val title: String? = "",
    val alBum: String? = "",
    val artist: String? = "",
    val modified: Long = -1,
    val genre: String? = "",
    val mimeType: String? = "",
    var modifiedStr: String=""
) {

    init {
        if (modified == -1L) {
            modifiedStr = ""
        }
        val time = Date(modified * 1000)
        val cal = Calendar.getInstance()
        cal.time = time
        val day = cal.get(Calendar.DAY_OF_MONTH)
        val month = cal.get(Calendar.MONTH)
        val year = cal.get(Calendar.YEAR)
        val hours = cal.get(Calendar.HOUR_OF_DAY)
        val minutes = cal.get(Calendar.MINUTE)
        modifiedStr = "$hours:$minutes , $day/$month/$year"

    }
}

