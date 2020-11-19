package com.example.audiocutter.ext

fun Long.convertToAudioDuration(): String {
    val totalSeconds = this / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds - (hours * 3600))/ 60
    val seconds = totalSeconds - (hours * 3600) - (minutes * 60)
    if (hours != 0L) {
        return "%02d : %02d : %02d".format(hours, minutes, seconds)
    } else {
        return "%02d : %02d".format(minutes, seconds)
    }
}