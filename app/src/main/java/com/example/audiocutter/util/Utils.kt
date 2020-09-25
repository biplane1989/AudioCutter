package com.example.audiocutter.util

import com.example.audiocutter.objects.AudioFile
import java.io.File

object Utils {
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

    fun changeNameFile(audioFile: AudioFile, fileName: String): File? {
        val filePath = audioFile.file.absolutePath
        var fileFrom = File(filePath)
        var fileTo = File(audioFile.file.parent, fileName)
        if (fileFrom.exists()) {
            fileFrom.renameTo(fileTo)
            return fileFrom
        }
        return null
    }
}