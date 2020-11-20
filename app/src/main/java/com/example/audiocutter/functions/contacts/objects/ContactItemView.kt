package com.example.audiocutter.functions.contacts.objects

import com.example.audiocutter.objects.ContactItem

data class ContactItemView(var contactHeader: String = "", var searchHeader: String = "", var contactItem: ContactItem, var isHeader: Boolean = false)