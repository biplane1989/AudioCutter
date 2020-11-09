package com.example.audiocutter.functions.contacts.objects

import com.example.audiocutter.objects.ContactItem

data class GetContactResult(var completed: Boolean = false, var listContactItem: List<ContactItem> = ArrayList())