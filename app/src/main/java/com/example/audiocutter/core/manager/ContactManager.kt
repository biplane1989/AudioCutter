package com.example.audiocutter.core.manager

import androidx.lifecycle.LiveData
import com.example.audiocutter.functions.contacts.objects.GetContactResult
import com.example.audiocutter.objects.ContactItem

interface ContactManager {
    fun setup()
    fun release()
    fun getListContact(): LiveData<GetContactResult>
    fun scanContact()
}