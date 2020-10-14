package com.example.audiocutter.functions.contacts.select

import com.example.audiocutter.objects.AudioFile

data class SelectItemView(var audioFile: AudioFile, var isExpanded: Boolean = false, var isSelect: Boolean = false, var selectItemStatus: SelectItemStatus = SelectItemStatus(), var isRingtoneDefault: Boolean = false)