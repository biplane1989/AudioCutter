package com.example.audiocutter.objects

import android.graphics.Bitmap
import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import java.io.File
import java.io.Serializable

class AudioFile(
    val file: File,
    val fileName: String,
    val size: Long,
    val bitRate: Int = 128,
    val time: Long = 0,
    var uri: Uri? = null,
    val bitmap: Bitmap? = null,
    val title: String? = "",
    val alBum: String? = "",
    val artist: String? = "",
    val dateAdded: String? = "",
    val genre: String? = "",
    val mimeType: String? = ""
)

