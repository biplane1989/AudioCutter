package com.example.waveform.views

interface WaveformViewListener {
    fun onStartTimeChanged(startTimeMs: Long)
    fun onEndTimeChanged(endTimeMs: Long)
    fun onPlayPositionChanged(positionMs: Int, isPress: Boolean)
    fun onDraggingPlayPos(isFinished: Boolean)
    fun onPlayPosOutOfRange(isEnd:Boolean)
}