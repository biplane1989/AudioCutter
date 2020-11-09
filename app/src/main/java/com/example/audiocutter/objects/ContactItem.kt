package com.example.audiocutter.objects

data class ContactItem(val name: String, val phoneNumber: String, val thumb: String?, var ringtone: String?, var isRingtoneDefault: Boolean = false, var fileNameRingtone: String? = null)