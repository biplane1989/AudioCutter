package com.example.audiocutter.functions.mystudioscreen

import com.example.audiocutter.functions.mystudioscreen.fragment.ItemLoadStatus
import com.example.audiocutter.objects.AudioFile


enum class DeleteState(value: Int) {
    HIDE(1),
    CHECKED(2),
    UNCHECK(3)
}

data class AudioFileView(
    var audioFile: AudioFile,
    var isExpanded: Boolean = false,
    var itemLoadStatus: ItemLoadStatus = ItemLoadStatus()
) {
}