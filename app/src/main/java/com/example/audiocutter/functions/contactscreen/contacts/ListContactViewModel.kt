package com.example.audiocutter.functions.contactscreen.contacts

import android.app.Application
import android.text.TextUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.example.audiocutter.base.BaseAndroidViewModel
import com.example.audiocutter.core.ManagerFactory
import com.example.audiocutter.objects.ContactItem
import com.example.audiocutter.util.Utils
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
                    newListContact.add(ContactItemView("", item))
                }
                mListContactItemView = getHeaderListLatter(newListContact)
            } else {
                val newListContacItemView = ArrayList<ContactItemView>()
                for (item in contacts) {
                    val contactItemView = getContactItemView(item.phoneNumber)
                    if (contactItemView != null) {
                        if (item.ringtone != contactItemView.contactItem.ringtone && contactItemView.isHeader == false) {
                            newListContacItemView.add(ContactItemView("", item))
                        } else {
                            newListContacItemView.add(contactItemView)
                        }
                    } else {
                        newListContacItemView.add(ContactItemView("", item))
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
        val newListContact = ArrayList<ContactItemView>()

        for (item in contactList) {     // add them 1 truong headerContact -> conver contactItem.name co dau thanh khong dau
            newListContact.add(ContactItemView(Utils.covertToString(item.contactItem.name)
                .toString(), item.contactItem, false))
        }

        Collections.sort(newListContact, Comparator<ContactItemView> { user1, user2 ->  // sap xep list theo headerContact
            java.lang.String.valueOf(user1.contactHeader.get(0)).toUpperCase()
                .compareTo(java.lang.String.valueOf(user2.contactHeader.get(0)).toUpperCase())
        })

        val firstContact = newListContact.get(0).contactHeader  // neu co cac ky tu dac biet thi them 1 header = "#"
        if (!firstContact[0].isLetter()) {
            listContact.add(ContactItemView("#", ContactItem("#", "", null, null), true))
        }
        var lastHeader: String? = ""
        for (contact in newListContact) {           // gom cac contact vao chung 1 header
            val header: String = contact.contactHeader.get(0).toString().toUpperCase()
            if (header[0].isLetter()) {
                if (!TextUtils.equals(lastHeader, header)) {
                    lastHeader = header
                    listContact.add(ContactItemView(header, contact.contactItem, true))
                }
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