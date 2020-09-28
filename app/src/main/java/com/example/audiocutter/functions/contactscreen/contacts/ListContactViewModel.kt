package com.example.audiocutter.functions.contactscreen.contacts

import android.app.Application
import android.text.TextUtils
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.example.audiocutter.base.BaseAndroidViewModel
import com.example.audiocutter.core.ManagerFactory
import com.example.audiocutter.core.manager.ContactManagerImpl
import com.example.audiocutter.objects.ContactItem
import java.util.*
import kotlin.Comparator
import kotlin.collections.ArrayList

class ListContactViewModel(application: Application) : BaseAndroidViewModel(application) {

    val TAG = "giangtd"
    private val context = getApplication<Application>().applicationContext

    private var mListContactItemView = ArrayList<ContactItemView>()

    suspend fun getData(): LiveData<List<ContactItemView>> {
//        val listContactItem: LiveData<List<ContactItem>> = ContactManagerImpl.getListContact()
        val listContactItem: LiveData<List<ContactItem>> = ManagerFactory.getContactManager()
            .getListContact()

        return Transformations.map(listContactItem) { contacts ->
            if (mListContactItemView.size == 0) {
                val newListContact = ArrayList<ContactItemView>()
                for (item in contacts) {
                    newListContact.add(ContactItemView(item))
                }
                mListContactItemView = getHeaderListLatter(newListContact)
            } else {
                val newListContacItemView = ArrayList<ContactItemView>()
                for (item in contacts) {
                    val contactItemView = getContactItemView(item.phoneNumber)
                    if (contactItemView != null) {
                        if (item.ringtone != contactItemView.contactItem.ringtone && contactItemView.isHeader == false) {
                            newListContacItemView.add(ContactItemView(item))
                        } else {
                            newListContacItemView.add(contactItemView)
                        }
                    } else {
                        newListContacItemView.add(ContactItemView(item))
                    }
                }

                mListContactItemView.clear()
                mListContactItemView = getHeaderListLatter(newListContacItemView)
            }
            mListContactItemView
        }
    }

    // tim ra nhung file con ton tai trong list cu khi co data thay doi
    private fun getContactItemView(phoneNumber: String): ContactItemView? {
        mListContactItemView.forEach {
            if (it.contactItem.phoneNumber.equals(phoneNumber) && it.isHeader == false) {
                return it
            }
        }
        return null
    }

    // tao header cho list contact
    private fun getHeaderListLatter(contactList: ArrayList<ContactItemView>): ArrayList<ContactItemView> {
        val listContact = ArrayList<ContactItemView>()
        Collections.sort(contactList, Comparator<ContactItemView> { user1, user2 ->
            java.lang.String.valueOf(user1.contactItem.name.get(0)).toUpperCase()
                .compareTo(java.lang.String.valueOf(user2.contactItem.name.get(0)).toUpperCase())
        })

        var lastHeader: String? = ""
        for (contact in contactList) {
            val header: String = contact.contactItem.name.get(0).toString().toUpperCase()
            if (!TextUtils.equals(lastHeader, header)) {
                lastHeader = header
                listContact.add(ContactItemView(contact.contactItem, true))
            }
            listContact.add(contact)
        }
        return listContact
    }

    fun searchContact(data: String): ArrayList<ContactItemView> {
        val newListContact = ArrayList<ContactItemView>()
        for (contact in mListContactItemView) {
            if (contact.contactItem.name.toUpperCase().contains(data.toUpperCase())) {
                newListContact.add(contact)
            }
        }
        return newListContact
    }
}