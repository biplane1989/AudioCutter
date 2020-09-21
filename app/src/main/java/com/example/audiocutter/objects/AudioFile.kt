package com.example.audiocutter.objects

import android.net.Uri
import com.example.audiocutter.core.manager.PlayerInfo
import java.io.File

class AudioFile(
    val file: File,
    val fileName: String,
    val size: Long,
    val bitRate: Int = 128,
    val time: Long = 0,
    var uri: Uri? = null
){
    override fun equals(other: Any?): Boolean {
        if(other is AudioFile){
            return file.absolutePath == other.file.absolutePath
        }
        return super.equals(other)
    }
}

