package com.example.audiocutter.functions.contactscreen.contacts

import android.app.Application
import android.text.TextUtils
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.example.audiocutter.base.BaseAndroidViewModel
import com.example.audiocutter.base.BaseViewModel
import com.example.audiocutter.core.ManagerFactory
import com.example.audiocutter.core.manager.ContactManagerImpl
import com.example.audiocutter.functions.contactscreen.ContactItemView
import com.example.audiocutter.functions.mystudioscreen.AudioFileView
import com.example.audiocutter.functions.mystudioscreen.DeleteState
import com.example.audiocutter.objects.ContactItem
import com.example.audiocutter.util.Utils
import java.util.*
import java.util.function.Consumer
import kotlin.Comparator
import kotlin.collections.ArrayList

class ListContactViewModel(application: Application) : BaseAndroidViewModel(application) {

    val TAG = "giangtd"
    private val context = getApplication<Application>().applicationContext

    private var mListContactItemView = ArrayList<ContactItemView>()


    suspend fun getData(): LiveData<List<ContactItemView>> {
//        val listContactItem: LiveData<List<ContactItem>> = ContactManagerImpl.getListContact(context)
        val listContactItem: LiveData<List<ContactItem>> = ManagerFactory.getContactManager()
            .getListContact(context)

        return Transformations.map(listContactItem) { contacts ->
            if (mListContactItemView.size == 0) {
                for (item in contacts) {
                    mListContactItemView.add(ContactItemView(item))
                }
            } else {
                val newListContacItemView = ArrayList<ContactItemView>()
                for (item in contacts) {
                    val contactItemView = getContactItemView(item.phoneNumber)
                    if (contactItemView != null) {
                        newListContacItemView.add(contactItemView)
                    } else {
//                        newListContacItemView.add(contactItemView!!)
                    }
                }
                mListContactItemView = newListContacItemView
            }


            val newListContact = getHeaderListLatter(mListContactItemView)
            mListContactItemView = newListContact
            mListContactItemView
        }
    }

    // tim ra nhung file con ton tai trong list cu khi co data thay doi
    private fun getContactItemView(phoneNumber: String): ContactItemView? {
        mListContactItemView.forEach {
            if (it.contactItem.phoneNumber.equals(phoneNumber)) {
                return it
            }
        }
        return null
    }

    // tao header cho list contact
    private fun getHeaderListLatter(contactList: ArrayList<ContactItemView>): ArrayList<ContactItemView> {
        var listContact = ArrayList<ContactItemView>()
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

}