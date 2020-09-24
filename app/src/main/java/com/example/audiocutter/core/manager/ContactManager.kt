package com.example.audiocutter.core.manager

import android.content.Context
import androidx.lifecycle.LiveData
import com.example.audiocutter.objects.ContactItem

interface ContactManager {
    suspend fun getListContact(context: Context): LiveData<List<ContactItem>>
}