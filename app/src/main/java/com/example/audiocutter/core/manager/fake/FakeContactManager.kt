package com.example.audiocutter.core.manager.fake

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.audiocutter.core.manager.ContactManager
import com.example.audiocutter.objects.AudioFile
import com.example.audiocutter.objects.ContactItem
import java.io.File

class FakeContactManager : ContactManager {
    private val contactLiveData = MutableLiveData<List<ContactItem>>()

    init {
        val contactItemList = ArrayList<ContactItem>()
        contactItemList.add(ContactItem("1", "name 1", "phoneNumber 1", "thumbnail 1"))
        contactItemList.add(ContactItem("2", "name 2", "phoneNumber 2", "thumbnail 2"))
        contactItemList.add(ContactItem("3", "name 3", "phoneNumber 3", "thumbnail 3"))
        contactLiveData.postValue(contactItemList)
    }

    override suspend fun getListContact(): LiveData<List<ContactItem>> {
        return contactLiveData
    }
}
