package com.example.audiocutter.functions.contacts.screens

import android.app.Application
import android.content.Context
import android.text.TextUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.example.audiocutter.base.BaseAndroidViewModel
import com.example.audiocutter.core.manager.ManagerFactory
import com.example.audiocutter.functions.contacts.objects.ContactItemView
import com.example.audiocutter.objects.ContactItem
import com.example.audiocutter.util.Utils
import java.util.*
import kotlin.Comparator
import kotlin.collections.ArrayList

class ListContactViewModel(application: Application) : BaseAndroidViewModel(application) {

    private val mContext = getApplication<Application>().applicationContext
    private val contactManager = ManagerFactory.createNewContactManager(mContext)

    var loadingStatus: MutableLiveData<Boolean> = MutableLiveData()
    var isEmptyStatus: MutableLiveData<Boolean> = MutableLiveData()
    private val listContactItem = MediatorLiveData<List<ContactItemView>>()

    init {
        contactManager.setup()
    }

    fun scan() {
        runOnBackground {
            contactManager.scanContact()
        }
    }

    fun getData(): LiveData<List<ContactItemView>> {
        listContactItem.addSource(contactManager.getListContact()) { contacts ->
            if (contacts.completed) {
                loadingStatus.postValue(false)
                if (contacts.listContactItem.size > 0) {
                    isEmptyStatus.postValue(false)
                    val newListContacItemView = ArrayList<ContactItemView>()
                    for (item in contacts.listContactItem) {
                        newListContacItemView.add(ContactItemView("", item, false))
                    }
                    listContactItem.postValue(getHeaderListLatter(newListContacItemView))
                } else {
                    isEmptyStatus.postValue(true)
                }

            } else {
                loadingStatus.postValue(true)
            }
        }
        return listContactItem
    }

    fun getLoadingStatus(): LiveData<Boolean> {
        return loadingStatus
    }

    fun getIsEmptyStatus(): LiveData<Boolean> {
        return isEmptyStatus
    }

    // tao header cho list contact
    private fun getHeaderListLatter(contactList: ArrayList<ContactItemView>): ArrayList<ContactItemView> {
        val listContact = ArrayList<ContactItemView>()
        val newListContact = ArrayList<ContactItemView>()

        if (contactList.size > 0) {
            for (item in contactList) {     // add them 1 truong headerContact -> conver contactItem.name co dau thanh khong dau
                newListContact.add(ContactItemView(Utils.covertToString(item.contactItem.name)
                    .toString(), item.contactItem, false))
            }

            Collections.sort(newListContact, Comparator<ContactItemView> { user1, user2 ->  // sap xep list theo headerContact
                java.lang.String.valueOf(user1.contactHeader.get(0)).toUpperCase(Locale.ROOT)
                    .compareTo(java.lang.String.valueOf(user2.contactHeader.get(0))
                        .toUpperCase(Locale.UK))
            })

            val firstContact = newListContact.get(0).contactHeader  // neu co cac ky tu dac biet thi them 1 header = "#"
            if (!firstContact[0].isLetter()) {
                listContact.add(ContactItemView("#", ContactItem("", "", null, null, false, ""), true))
            }
            var lastHeader: String? = ""
            for (contact in newListContact) {           // gom cac contact vao chung 1 header
                val header: String = contact.contactHeader.get(0).toString()
                    .toUpperCase(Locale.ROOT)
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

    fun searchContact(data: String): ArrayList<ContactItemView> {
        listContactItem.value?.let {
            val newListContact = ArrayList<ContactItemView>()
            for (contact in it) {
                if (contact.contactHeader.toUpperCase(Locale.ROOT)
                        .contains(data.toUpperCase(Locale.ROOT))) {
                    newListContact.add(contact)
                }
            }
//            listContactItem.postValue(newListContact)
            return newListContact
        }
        return ArrayList()
    }

    // check ringtone contact co phai la ringtone default khong?
    fun checkRingtoneDefault(context: Context, uri: String): Boolean {
        if (TextUtils.equals(uri, Utils.getUriRingtoneDefault(context).toString())) return true
        return false
    }

    // set lai ringtone mac dinh cho list contact
    fun setListDefaultRingtone(context: Context, listContact: List<ContactItem>): List<ContactItem> {
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

    override fun onCleared() {
        super.onCleared()
        contactManager.release()
    }
}