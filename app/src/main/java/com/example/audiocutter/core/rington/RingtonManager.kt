package com.example.audiocutter.core.rington

import android.content.Context
import android.net.Uri
import com.example.audiocutter.objects.AudioFile

interface RingtonManager {

    fun setAlarmManager(audioFile: AudioFile): Boolean

    fun setNotificationSound(audioFile: AudioFile): Boolean

    fun setRingTone(audioFile: AudioFile): Boolean

    fun setRingToneWithContactNumber(audioFile: AudioFile, contactNumber: String): Boolean

    fun setRingToneWithContactNumberAndUri(path: String, contactNumber: String): Boolean
}