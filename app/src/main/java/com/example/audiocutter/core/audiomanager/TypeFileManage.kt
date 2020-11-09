package com.example.audiocutter.core.audiomanager

enum class Folder(type: String) {
    TYPE_CUTTER("cutter"),
    TYPE_MERGER("merger"),
    TYPE_MIXER("mixer")
}

enum class StateFile(state: Int) {
    STATE_FULL_MEMORY(0),
    STATE_SAVE_SUCCESS(1),
    STATE_SAVE_FAIL(2),
    STATE_FILE_NOT_FOUND(3)

}