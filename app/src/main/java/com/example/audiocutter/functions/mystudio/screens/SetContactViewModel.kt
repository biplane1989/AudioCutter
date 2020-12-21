package com.example.audiocutter.functions.mystudio.screens

import android.app.Application
import android.content.Context
import android.text.TextUtils
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.example.audiocutter.base.BaseAndroidViewModel
import com.example.audiocutter.core.manager.ManagerFactory
import com.example.audiocutter.functions.contacts.objects.SelectItemView
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

    private val mContactMediatorLivedata = MediatorLiveData<List<SetContactItemView>>()

    private var mListContact = ArrayList<SetContactItemView>()
    private var mListSearch = ArrayList<SetContactItemView>()

    private val isSelectItem = MutableLiveData<Boolean>()

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
        mContactMediatorLivedata.addSource(contactManager.getListContact()) { contacts ->
            if (contacts.completed) {
                loadingStatus.postValue(false)
                if (contacts.listContactItem.size > 0) {
                    isEmptyStatus.postValue(false)
                    val newListContacItemView = ArrayList<SetContactItemView>()
                    for (item in contacts.listContactItem) {
                        newListContacItemView.add(SetContactItemView("", "", item, false))
                    }
                    val listContact = getHeaderListLatter(newListContacItemView)
                    mContactMediatorLivedata.postValue(listContact)
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
        return mContactMediatorLivedata
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
                listContact.add(SetContactItemView("#", "", ContactItem("", "", "", null, null, false, ""), true))
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
        var index = 0                   // dong bo hoa mListSearch va mListContact
        if (mListSearch.size > 0) {
            for (item in mListContact) {
                for (searchItem in mListSearch) {
                    if (TextUtils.equals(item.contactItem.phoneNumber, searchItem.contactItem.phoneNumber) && TextUtils.equals(item.searchHeader, searchItem.searchHeader) && item.isHeader == searchItem.isHeader) {
                        mListContact.set(index, searchItem)
                        break
                    }
                }
                index++
            }
        }
        mListSearch.clear()
        if (data.equals("")) {
            if (mListContact.size > 0) {
                mListSearch.add(mListContact.get(0))
            }
            mContactMediatorLivedata.postValue(mListContact)
        } else {
            for (item in mListContact) {
                if (item.searchHeader.toUpperCase(Locale.ROOT)
                        .contains(data.toUpperCase(Locale.ROOT))) {
                    mListSearch.add(item)
                }
            }
            mContactMediatorLivedata.postValue(mListSearch)
        }

        if (mListContact.size > 0) {
            if (mListSearch.size > 0) {
                isEmptyStatus.postValue(false)
            } else {
                isEmptyStatus.postValue(true)
            }
        } else {
            isEmptyStatus.postValue(true)
        }
    }

    fun updateIsSelectItem(phoneNumber: String) {
        var newContactList = ArrayList<SetContactItemView>()
        if (mListSearch.size > 0) {
            newContactList = mListSearch

            for (item in mListContact) {    // khi la listSearch thi reset lai cho mListContact
                item.isSelect = false
            }

        } else {
            newContactList = mListContact
        }

        var index = 0
        for (item in newContactList) {
            if (TextUtils.equals(item.contactItem.phoneNumber, phoneNumber)) {
                val newContact = item.copy()
                newContact.isSelect = true
                newContactList.set(index, newContact)
                isSelectItem.postValue(true)

            } else {
                val newContact = item.copy()
                newContact.isSelect = false
                newContactList.set(index, newContact)
            }
            index++
        }

        mContactMediatorLivedata.postValue(newContactList)
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

    /* // check ringtone contact co phai la ringtone default khong?
     private fun checkRingtoneDefault(context: Context, uri: String): Boolean {
         if (TextUtils.equals(uri, Utils.getUriRingtoneDefault(context).toString())) return true
         return false
     }

      // set lai ringtone mac dinh cho list contact
      private fun setListDefaultRingtone(context: Context, listContact: List<ContactItem>): List<ContactItem> {
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
      }*/

    override fun onCleared() {
        super.onCleared()
        contactManager.release()
    }
}