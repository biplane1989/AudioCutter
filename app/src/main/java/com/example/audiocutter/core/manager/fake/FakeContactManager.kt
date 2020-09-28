package com.example.audiocutter.core.manager.fake

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.audiocutter.core.manager.ContactManager
import com.example.audiocutter.objects.ContactItem
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class FakeContactManager : ContactManager {
    private val contactLiveData = MutableLiveData<List<ContactItem>>()

    init {
        val contactItemList = ArrayList<ContactItem>()
        contactItemList.add(ContactItem("A", "1", "thumbnail 3", null))
        contactItemList.add(ContactItem("AAA", "2", "thumbnail 3", "@@@@"))
        contactItemList.add(ContactItem("name 1", "3", "thumbnail 1", "@@@@"))
        contactItemList.add(ContactItem("Orange", "4", "thumbnail 2", "@@@@"))
        contactItemList.add(ContactItem("BBBB", "5", "thumbnail 3", null))
        contactItemList.add(ContactItem("ô", "6", "thumbnail 3", null))
        contactItemList.add(ContactItem("ơ", "7", "thumbnail 3", "@@@@"))
        contactItemList.add(ContactItem("O", "8", "thumbnail 3", "@@@@"))
        contactItemList.add(ContactItem("O", "9", "thumbnail 3", null))
        contactItemList.add(ContactItem("E", "11", "thumbnail 3", "@@@@"))
        contactItemList.add(ContactItem("Ê", "121", "thumbnail 3", null))
        contactItemList.add(ContactItem("062", "13", "thumbnail 3", "@@@@"))
        contactItemList.add(ContactItem("..", "12", "thumbnail 3", "@@@@"))
        contactItemList.add(ContactItem("@@", "14", "thumbnail 3", "@@@@"))
        contactItemList.add(ContactItem("###", "15", "thumbnail 3", "@@@@"))
        contactItemList.add(ContactItem("Ă", "16", "thumbnail 3", "@@@@"))
        contactItemList.add(ContactItem("a", "17", "thumbnail 3", "@@@@"))
        contactItemList.add(ContactItem("ấ", "123", "thumbnail 3", "@@@@"))
        contactItemList.add(ContactItem("â", "1234", "thumbnail 3", "@@@@"))
        contactItemList.add(ContactItem("ẵ", "12345", "thumbnail 3", "@@@@"))

        contactLiveData.postValue(contactItemList)

        MainScope().launch {
            delay(10000)
//            contactItemList.add(ContactItem("AAAhhhhh", "321", "thumbnail 3", null))
//            contactItemList.removeAt(4)
//            val item = contactItemList.get(0).copy()
//            item.ringtone = "orange"
//            contactItemList.set(0, item)
            contactLiveData.postValue(contactItemList)
        }
    }

    override suspend fun getListContact(): LiveData<List<ContactItem>> {
        return contactLiveData
    }
}
