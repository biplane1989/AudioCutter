package com.example.core.core

import java.io.File

class AudioCore(
    var file: File, var fileName: String,
    var size: Long,
    var bitRate: Int = 128,
    var time: Long = 0,
    var mimeType: String? = ""
) {
    override fun equals(other: Any?): Boolean {
        if (other is AudioCore) {
            return file.absolutePath == other.file.absolutePath
        }
        return super.equals(other)
    }

    constructor() : this(File(""), "", 0, BitRate._128kb.value, 0, "")
}