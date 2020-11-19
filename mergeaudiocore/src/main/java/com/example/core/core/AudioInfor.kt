package com.example.core.core

class AudioInfor(
    val fileName: String,
    val bitRate: Int,
    val duration: Long,
    val size: Long,
    val filePath: String,
    val format: String,
    val title: String? = null,
    val alBum: String? = null,
    val artist: String? = null,
    val genre: String? = null
)