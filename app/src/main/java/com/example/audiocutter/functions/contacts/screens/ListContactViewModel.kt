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
import kotlinx.coroutines.delay
import java.util.*
import kotlin.Comparator
import kotlin.collections.ArrayList

class ListContactViewModel(application: Application) : BaseAndroidViewModel(application) {

    private val mContext = getApplication<Application>().applicationContext
    private val contactManager = ManagerFactory.createNewContactManager(mContext)

    private var loadingStatus: MutableLiveData<Boolean> = MutableLiveData()
    private var isEmptyStatus: MutableLiveData<Boolean> = MutableLiveData()

    private var mListContact = ArrayList<ContactItemView>()
    private val listContactItem = MediatorLiveData<List<ContactItemView>>()

    init {
        contactManager.setup()
    }

    fun scan() {
        runOnBackground {
            loadingStatus.postValue(true)
            contactManager.scanContact()
        }
    }

    fun getData(): LiveData<List<ContactItemView>> {
        listContactItem.addSource(contactManager.getListContact()) { contacts ->
            loadingStatus.postValue(true)
            if (contacts.completed) {
                loadingStatus.postValue(false)
                if (contacts.listContactItem.size > 0) {
                    isEmptyStatus.postValue(false)
                    val newListContacItemView = ArrayList<ContactItemView>()
                    for (item in contacts.listContactItem) {
                        newListContacItemView.add(ContactItemView("", "", item, false))
                    }
                    mListContact = getHeaderListLatter(newListContacItemView)
                    listContactItem.postValue(mListContact)
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
                    .toString(), Utils.covertToString(item.contactItem.name)
                    .toString(), item.contactItem, false))
            }

            Collections.sort(newListContact, Comparator<ContactItemView> { user1, user2 ->  // sap xep list theo headerContact
                java.lang.String.valueOf(user1.contactHeader.get(0)).toUpperCase(Locale.ROOT)
                    .compareTo(java.lang.String.valueOf(user2.contactHeader.get(0))
                        .toUpperCase(Locale.UK))
            })

            val firstContact = newListContact.get(0).contactHeader  // neu co cac ky tu dac biet thi them 1 header = "#"
            if (!firstContact[0].isLetter()) {
                listContact.add(ContactItemView("#", "", ContactItem("", "", null, null, false, ""), true))
            }
            var lastHeader: String? = ""
            for (contact in newListContact) {           // gom cac contact vao chung 1 header
                val header: String = contact.contactHeader.get(0).toString()
                    .toUpperCase(Locale.ROOT)
                if (header[0].isLetter()) {
                    if (!TextUtils.equals(lastHeader, header)) {
                        lastHeader = header
                        listContact.add(ContactItemView(header, contact.contactHeader.toUpperCase(Locale.ROOT), contact.contactItem, true))
                    }
                }
                listContact.add(contact)
            }
        }
        return listContact
    }

    fun refesherData() {
        var index = 0
        for (item in mListContact) {
            val newItem = item.copy()
            newItem.isSearch = true

            mListContact.set(index, newItem)
            index++
        }
        listContactItem.postValue(mListContact)
    }

    fun searchContact(data: String) {
        if (data.equals("")) {
            var index = 0
            for (item in mListContact) {
                val contact = item.copy()
                contact.isSearch = true
                mListContact.set(index, contact)
                index++
            }
        } else {
            var index = 0
            for (item in mListContact) {
                val contact = item.copy()
                if (item.searchHeader.toUpperCase(Locale.ROOT)
                        .contains(data.toUpperCase(Locale.ROOT))) {
                    contact.isSearch = true
                } else {
                    contact.isSearch = false
                }
                mListContact.set(index, contact)
                index++
            }
        }

        isEmptyStatus.postValue(true)
        for (item in mListContact) {
            if (item.isSearch) {
                isEmptyStatus.postValue(false)
            }
        }
        listContactItem.postValue(mListContact)
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