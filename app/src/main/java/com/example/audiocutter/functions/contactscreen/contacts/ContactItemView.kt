package com.example.audiocutter.functions.contactscreen.contacts

import com.example.audiocutter.objects.ContactItem

data class ContactItemView(var contactHeader: String = "", var contactItem: ContactItem, var isHeader: Boolean = false)