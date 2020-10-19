package com.example.audiocutter.functions.mystudio.objects

import com.example.audiocutter.functions.mystudio.ItemLoadStatus
import com.example.audiocutter.functions.resultscreen.objects.ConvertingItem
import com.example.audiocutter.functions.resultscreen.objects.ConvertingState
import com.example.audiocutter.objects.AudioFile


enum class DeleteState(value: Int) {
    HIDE(1), CHECKED(2), UNCHECK(3)
}

data class AudioFileView(var audioFile: AudioFile, var isExpanded: Boolean = false, var itemLoadStatus: ItemLoadStatus = ItemLoadStatus(), val convertingState: ConvertingState, val percent: Int, val id: Int)