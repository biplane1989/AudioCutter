package com.example.audiocutter.core.manager

import androidx.lifecycle.LiveData
import com.example.audiocutter.objects.ContactItem

interface ContactManager {
    suspend fun getListContact(): LiveData<List<ContactItem>>
}