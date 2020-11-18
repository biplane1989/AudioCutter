package com.example.audiocutter.functions.mystudio.screens

import android.app.Application
import android.content.Context
import android.text.TextUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.example.audiocutter.base.BaseAndroidViewModel
import com.example.audiocutter.core.manager.ManagerFactory
import com.example.audiocutter.functions.mystudio.objects.SetContactItemView
import com.example.audiocutter.objects.ContactItem
import com.example.audiocutter.util.Utils
import java.util.*
import kotlin.Comparator
import kotlin.collections.ArrayList

class SetContactViewModel(application: Application) : BaseAndroidViewModel(application) {

    private val mContext = getApplication<Application>().applicationContext
    private val contactManager = ManagerFactory.createNewContactManager(mContext)

    private var loadingStatus: MutableLiveData<Boolean> = MutableLiveData()
    private var isEmptyStatus: MutableLiveData<Boolean> = MutableLiveData()
    private val listContactItem = MediatorLiveData<List<SetContactItemView>>()
    private val isSelectItem = MutableLiveData<Boolean>()

    private var mListContact = ArrayList<SetContactItemView>()

    init {
        isSelectItem.postValue(false)
        contactManager.setup()
    }

    fun scan() {
        runOnBackground {
            contactManager.scanContact()
        }
    }

    fun getData(): LiveData<List<SetContactItemView>> {
        listContactItem.addSource(contactManager.getListContact()) { contacts ->
            if (contacts.completed) {
                loadingStatus.postValue(false)
                if (contacts.listContactItem.size > 0) {
                    isEmptyStatus.postValue(false)
                    val newListContacItemView = ArrayList<SetContactItemView>()
                    for (item in contacts.listContactItem) {
                        newListContacItemView.add(SetContactItemView("", "", item, false))
                    }
                    val listContact = getHeaderListLatter(newListContacItemView)
                    listContactItem.postValue(listContact)
                    for (item in listContact) {
                        mListContact.add(item)
                    }
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

    fun getIsSelectItem(): LiveData<Boolean> {
        return isSelectItem
    }

    // tao header cho list contact
    private fun getHeaderListLatter(contactList: ArrayList<SetContactItemView>): ArrayList<SetContactItemView> {
        val listContact = ArrayList<SetContactItemView>()
        val newListContact = ArrayList<SetContactItemView>()

        if (contactList.size > 0) {
            for (item in contactList) {     // add them 1 truong headerContact -> conver contactItem.name co dau thanh khong dau
                newListContact.add(SetContactItemView(Utils.covertToString(item.contactItem.name)
                    .toString(), Utils.covertToString(item.contactItem.name)
                    .toString(), item.contactItem, false))
            }

            Collections.sort(newListContact, Comparator<SetContactItemView> { user1, user2 ->  // sap xep list theo headerContact
                java.lang.String.valueOf(user1.contactHeader.get(0)).toUpperCase(Locale.ROOT)
                    .compareTo(java.lang.String.valueOf(user2.contactHeader.get(0))
                        .toUpperCase(Locale.UK))
            })

            val firstContact = newListContact.get(0).contactHeader  // neu co cac ky tu dac biet thi them 1 header = "#"
            if (!firstContact[0].isLetter()) {
                listContact.add(SetContactItemView("#", "", ContactItem("", "", null, null, false, ""), true))
            }
            var lastHeader: String? = ""
            for (contact in newListContact) {           // gom cac contact vao chung 1 header
                val header: String = contact.contactHeader.get(0).toString()
                    .toUpperCase(Locale.ROOT)
                if (header[0].isLetter()) {
                    if (!TextUtils.equals(lastHeader, header)) {
                        lastHeader = header
                        listContact.add(SetContactItemView(header, contact.contactHeader.toUpperCase(Locale.ROOT), contact.contactItem, true))
                    }
                }
                listContact.add(contact)
            }
        }
        return listContact
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

    fun updateIsSelectItem(phoneNumber: String) {

        var index = 0
        for (item in mListContact) {
            if (TextUtils.equals(item.contactItem.phoneNumber, phoneNumber)) {
                val newContact = item.copy()
                newContact.isSelect = true
                mListContact.set(index, newContact)
                isSelectItem.postValue(true)

            } else {
                val newContact = item.copy()
                newContact.isSelect = false
                mListContact.set(index, newContact)
            }
            index++
        }

        listContactItem.postValue(mListContact)
    }

    fun setRingtoneForContact(filePath: String): Boolean {
        for (item in mListContact) {
            if (item.isSelect) {
                return ManagerFactory.getRingtoneManager()
                    .setRingToneWithContactNumberandFilePath(filePath, item.contactItem.phoneNumber)
            }
        }
        return false
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