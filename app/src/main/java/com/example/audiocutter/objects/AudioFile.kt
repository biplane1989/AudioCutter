package com.example.audiocutter.objects

import android.graphics.Bitmap
import android.net.Uri
import java.io.File

class AudioFile(
    val file: File,
    val fileName: String,
    val size: Long,
    val bitRate: Int = 128,
    val time: Long = 0,
    var uri: Uri? = null,
    val bitmap: Bitmap?=null,
    val title: String? = "Unknown",
    val alBum: String? = "Unknown",
    val artist: String? = "Unknown",
    val dateAdded: String? = "Unknown",
    val genre: String? = "Unknown"
) {

        override fun equals(other: Any?): Boolean {
            if (other is AudioFile) {
                return file.absolutePath == other.file.absolutePath
            }
            return super.equals(other)
        }
    }

