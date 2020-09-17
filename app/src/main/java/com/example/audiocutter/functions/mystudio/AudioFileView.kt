package com.example.audiocutter.functions.mystudio

import androidx.lifecycle.MutableLiveData
import com.example.audiocutter.core.manager.PlayerState
import com.example.audiocutter.functions.mystudio.audiocutter.ItemLoadStatus
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