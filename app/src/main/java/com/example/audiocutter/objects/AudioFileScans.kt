package com.example.audiocutter.objects

data class AudioFileScans(
    var listAudioFiles: List<AudioFile>,
    var state: StateLoad? = StateLoad.LOADING
)

//D:\AndroidStudioProjects\0026.AudioCutter\app\src\main\java\com\example\audiocutter\objects\AudioFileScans.kt
enum class StateLoad {
    LOADING, LOADDONE, LOADFAIL
}