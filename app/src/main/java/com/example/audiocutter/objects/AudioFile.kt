package com.example.audiocutter.objects

import java.io.File
import java.io.Serializable

class AudioFile(val file: File, val fileName: String,val size: Long, bitRate: Int = 128, time: Long = 0):Serializable

