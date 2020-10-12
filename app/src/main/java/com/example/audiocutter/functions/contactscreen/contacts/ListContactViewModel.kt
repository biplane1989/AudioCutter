package com.example.audiocutter.functions.contactscreen.contacts

import android.content.Context
import android.text.TextUtils
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.example.audiocutter.base.BaseAndroidViewModel
import com.example.audiocutter.base.BaseViewModel
import com.example.audiocutter.core.ManagerFactory
import com.example.audiocutter.objects.ContactItem
import com.example.audiocutter.util.Utils
import java.util.*
import kotlin.Comparator
import kotlin.collections.ArrayList

class ListContactViewModel : BaseViewModel() {

    val TAG = "giangtd"
    private var mListContactItemView = ArrayList<ContactItemView>()

    fun getData(): LiveData<List<ContactItemView>> {
        val listContactItem: LiveData<GetContactResult> = ManagerFactory.getContactManager()
            .getListContact()

        return Transformations.map(listContactItem) { contacts ->
            if (mListContactItemView.size == 0) {
                val newListContact = ArrayList<ContactItemView>()

                for (item in contacts.listContactItem) {
                    newListContact.add(ContactItemView("", item))
                }

                mListContactItemView = getHeaderListLatter(newListContact)
            } else {
                val newListContacItemView = ArrayList<ContactItemView>()
                for (item in contacts.listContactItem) {
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

        if (contactList.size > 0) {
            for (item in contactList) {     // add them 1 truong headerContact -> conver contactItem.name co dau thanh khong dau
                newListContact.add(
                    ContactItemView(
                        Utils.covertToString(item.contactItem.name)
                            .toString(), item.contactItem, false
                    )
                )
            }


            Collections.sort(
                newListContact,
                Comparator<ContactItemView> { user1, user2 ->  // sap xep list theo headerContact
                    java.lang.String.valueOf(user1.contactHeader.get(0)).toUpperCase()
                        .compareTo(
                            java.lang.String.valueOf(user2.contactHeader.get(0)).toUpperCase()
                        )
                })

            val firstContact =
                newListContact.get(0).contactHeader  // neu co cac ky tu dac biet thi them 1 header = "#"
            if (!firstContact[0].isLetter()) {
                listContact.add(ContactItemView("#", ContactItem("", "", null, null), true))
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
        }
        return listContact
    }

    fun searchContact(data: String): List<ContactItemView> {
        val newListContact = ArrayList<ContactItemView>()
        for (contact in mListContactItemView) {
            if (contact.contactHeader.toUpperCase().contains(data.toUpperCase())) {
                newListContact.add(contact)
            }
        }
        return newListContact
    }

    // check ringtone contact co phai la ringtone default khong?
    fun checkRingtoneDefault(context: Context, uri: String): Boolean {
        if (TextUtils.equals(uri, Utils.getUriRingtoneDefault(context).toString())) return true
        return false
    }

    // set lai ringtone mac dinh cho list contact
    fun setListDefaultRingtone(
        context: Context,
        listContact: List<ContactItem>
    ): List<ContactItem> {
        val newListContact = ArrayList<ContactItem>()
        for (item in listContact) {
            if (checkRingtoneDefault(context, item.ringtone.toString())) {
                val newItem = item.copy()
                newItem.ringtone = null
                newListContact.add(newItem)
            } else {
                newListContact.add(item)
            }
        }
        return newListContact
    }
}