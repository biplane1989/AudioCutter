package com.example.audiocutter.core.audioManager

enum class TypeFile(type: String) {
    TYPE_CUTTER("cutter"),
    TYPE_MERGER("merger"),
    TYPE_MIXER("mixer")
}

enum class StateFile(state: Int) {
    STATE_SAVE_SUCCESS(1),
    STATE_SAVE_FAIL(2),
    STATE_FULL_MEMORY(0)
}