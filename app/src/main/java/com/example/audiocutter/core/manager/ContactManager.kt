package com.example.audiocutter.core.manager

import androidx.lifecycle.LiveData
import com.example.audiocutter.functions.contacts.objects.GetContactResult

interface ContactManager {
    fun getListContact(): LiveData<GetContactResult>
}