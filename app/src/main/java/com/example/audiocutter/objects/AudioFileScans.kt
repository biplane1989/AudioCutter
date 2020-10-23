package com.example.audiocutter.objects

data class AudioFileScans(
    var listAudioFiles: List<AudioFile>,
    var state: StateLoad? = StateLoad.LOADING
)

enum class StateLoad {
    LOADING, LOADDONE, LOADFAIL
}