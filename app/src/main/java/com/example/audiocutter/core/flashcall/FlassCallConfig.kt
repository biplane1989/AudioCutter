package com.example.audiocutter.core.flashcall

data class FlashMode(val bellEnable: Boolean, val vibrateEnable: Boolean, val silentEnable: Boolean)
data class FlashTimer(val enable: Boolean, val startTime: Long, val endTime: Long)
data class FlashCallConfig(
    val enable: Boolean,
    val incomingCallEnable: Boolean,
    val notificationEnable: Boolean,
    val notFiredWhenInUsed: Boolean,
    val lightningSpeed: Long,
    val numberOfLightning: Int,
    val flashMode: FlashMode,
    val flashTimer: FlashTimer
)
