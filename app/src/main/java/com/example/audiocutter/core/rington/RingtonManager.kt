package com.example.audiocutter.core.rington

import android.content.Context
import com.example.audiocutter.objects.AudioFile

interface RingtonManager {

    fun setAlarmManager(context: Context, audioFile: AudioFile): Boolean

    fun setNotificationSound(context: Context, audioFile: AudioFile): Boolean

    fun setRingTone(context: Context, audioFile: AudioFile): Boolean

    fun setRingToneWithContactNumber(context: Context, audioFile: AudioFile, contactNumber: String): Boolean
}