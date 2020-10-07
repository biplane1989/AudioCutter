package com.example.core.Utils

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

    fun deleteFile(pathFile: String): Boolean {
        val file = File(pathFile)
        if (file.exists()) {
            return file.delete()
        }
        return false
    }
}