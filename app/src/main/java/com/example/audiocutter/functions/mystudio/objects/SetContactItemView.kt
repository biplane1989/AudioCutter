package com.example.audiocutter.functions.mystudio.objects

import com.example.audiocutter.objects.ContactItem

data class SetContactItemView(var contactHeader: String = "", var searchHeader: String, var contactItem: ContactItem, var isHeader: Boolean = false, var isSelect: Boolean = false, var isSearch: Boolean = true)