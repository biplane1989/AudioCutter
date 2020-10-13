package com.example.audiocutter.core.manager

import androidx.lifecycle.LiveData
import com.example.audiocutter.functions.contactscreen.contacts.GetContactResult

interface ContactManager {
    fun getListContact(): LiveData<GetContactResult>
}