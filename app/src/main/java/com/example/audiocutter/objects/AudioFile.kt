package com.example.audiocutter.objects

import java.io.File

class AudioFile(val file: File, val fileName: String, size: Long, bitRate: Int = 128, time: Long = 0)

