package com.example.audiocutter.core.manager.fake

import android.content.Context
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
        contactItemList.add(ContactItem("2", "Orange", "phoneNumber 2", "thumbnail 2"))
        contactItemList.add(ContactItem("3", "BBBB", "phoneNumber 3", "thumbnail 3"))
        contactItemList.add(ContactItem("3", "ô", "phoneNumber 3", "thumbnail 3"))
        contactItemList.add(ContactItem("3", "ơ", "phoneNumber 3", "thumbnail 3"))
        contactItemList.add(ContactItem("3", "O", "phoneNumber 3", "thumbnail 3"))
        contactItemList.add(ContactItem("3", "O", "phoneNumber 3", "thumbnail 3"))
        contactItemList.add(ContactItem("3", "E", "phoneNumber 3", "thumbnail 3"))
        contactItemList.add(ContactItem("3", "Ê", "phoneNumber 3", "thumbnail 3"))
        contactItemList.add(ContactItem("3", "062", "phoneNumber 3", "thumbnail 3"))
        contactItemList.add(ContactItem("3", "..", "phoneNumber 3", "thumbnail 3"))
        contactItemList.add(ContactItem("3", "@@", "phoneNumber 3", "thumbnail 3"))
        contactItemList.add(ContactItem("3", "###", "phoneNumber 3", "thumbnail 3"))
        contactItemList.add(ContactItem("3", "A", "phoneNumber 3", "thumbnail 3"))
        contactItemList.add(ContactItem("3", "AAA", "phoneNumber 3", "thumbnail 3"))
        contactItemList.add(ContactItem("3", "Ă", "phoneNumber 3", "thumbnail 3"))
        contactItemList.add(ContactItem("3", "a", "phoneNumber 3", "thumbnail 3"))
        contactItemList.add(ContactItem("3", "ấ", "phoneNumber 3", "thumbnail 3"))
        contactItemList.add(ContactItem("3", "â", "phoneNumber 3", "thumbnail 3"))
        contactItemList.add(ContactItem("3", "ẵ", "phoneNumber 3", "thumbnail 3"))
        contactLiveData.postValue(contactItemList)
    }

    override suspend fun getListContact(context: Context): LiveData<List<ContactItem>> {
        return contactLiveData
    }
}
