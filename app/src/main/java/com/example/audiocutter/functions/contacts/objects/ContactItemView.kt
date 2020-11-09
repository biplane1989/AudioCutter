package com.example.audiocutter.functions.contacts.objects

import android.graphics.Bitmap
import com.example.audiocutter.objects.ContactItem

data class ContactItemView(var contactHeader: String = "", var contactItem: ContactItem, var isHeader: Boolean = false)