package com.example.audiocutter.core.manager

import android.content.Context
import android.database.ContentObserver
import android.database.Cursor
import android.net.Uri
import android.os.Handler
import android.provider.ContactsContract
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.audiocutter.objects.ContactItem
import kotlinx.coroutines.*

object ContactManagerImpl : ContactManager {

    private val contactLiveData = MutableLiveData<List<ContactItem>>()
    var _listContact: ArrayList<ContactItem> = ArrayList()
    val TAG = "giangtd"
    lateinit var mContext: Context
    private var initialized = false
    val contactObserver = ContactObserver(Handler())
    val mainScope = MainScope()
    fun init(context: Context) {
        mContext = context
    }

    suspend fun scanContact(): List<ContactItem> = withContext(Dispatchers.IO) {
        val newListContact: ArrayList<ContactItem> = ArrayList()
//
        val projecttion = arrayOf(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.CommonDataKinds.Phone.PHOTO_URI, ContactsContract.CommonDataKinds.Phone.CUSTOM_RINGTONE)
        val cursor: Cursor = mContext.contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, projecttion, null, null, null)!!
        val nameIndex = cursor.getColumnIndex(projecttion[0])
        val numberIndex = cursor.getColumnIndex(projecttion[1])
        val photoIndex = cursor.getColumnIndex(projecttion[2])
        val ringtoneIndex = cursor.getColumnIndex(projecttion[3])
        try {
            if (cursor.moveToFirst()) {

                do {
                    val name = cursor.getString(nameIndex)
                    val number = cursor.getString(numberIndex)
                    val photoUri = cursor.getString(photoIndex)
                    val ringtone = cursor.getString(ringtoneIndex)
//                    Log.d(TAG, "getListData: $name - $number - $photoUri - $ringtone")

                    if (ringtone != null) {
                        newListContact.add(ContactItem(name, number, photoUri, ringtone))

                    } else {
                        newListContact.add(ContactItem(name, number, photoUri, null))

                    }
                } while (cursor.moveToNext())

            }
        } finally {
            if (!cursor.isClosed) cursor.close()
        }
        newListContact
    }

    override suspend fun getListContact(): LiveData<List<ContactItem>> = withContext(Dispatchers.IO) {

        if (!initialized) {
            contactLiveData.postValue(scanContact())
        }
        contactLiveData
    }

    class ContactObserver(handler: Handler?) : ContentObserver(handler) {
        override fun onChange(selfChange: Boolean, uri: Uri?) {
            mainScope.launch {
                contactLiveData.postValue(scanContact())
            }

        }
    }

    fun registerContentObserVerDeleted() {
        mContext.contentResolver.registerContentObserver(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, true, contactObserver)
    }

    fun unRegisterContentObserve() {
        mContext.contentResolver.unregisterContentObserver(contactObserver)
    }


}