package com.example.waveform.views

interface WaveformViewListener {
    fun onStartTimeChanged(startTimeMs: Long)
    fun onEndTimeChanged(endTimeMs: Long)
    fun onPlayPositionChanged(positionMs: Int, isPress: Boolean)
    fun onCountAudioSelected(positionMs: Long, isFirstTime: Boolean)
}