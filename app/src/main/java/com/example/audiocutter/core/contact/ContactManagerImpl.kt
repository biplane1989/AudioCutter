package com.example.audiocutter.core.contact

import android.content.Context
import android.database.ContentObserver
import android.database.Cursor
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.provider.ContactsContract
import android.text.TextUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.audiocutter.core.manager.ContactManager
import com.example.audiocutter.functions.contacts.objects.GetContactResult
import com.example.audiocutter.objects.ContactItem
import com.example.audiocutter.util.Utils
import kotlinx.coroutines.*

object ContactManagerImpl : ContactManager {

    private val contactLiveData = MutableLiveData<GetContactResult>()

    val TAG = "giangtd"
    lateinit var mContext: Context
    private var initialized = false
    var oldRingtoneDefault = ""
    val contactObserver = ContactObserver(Handler())
    val mainScope = MainScope()

    fun init(context: Context) {
        mContext = context
    }

    suspend fun scanContact(): List<ContactItem> = withContext(Dispatchers.IO) {
        val newListContact: ArrayList<ContactItem> = ArrayList()

        val projecttion = arrayOf(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.CommonDataKinds.Phone.PHOTO_URI, ContactsContract.CommonDataKinds.Phone.CUSTOM_RINGTONE)
        val cursor: Cursor = mContext.contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, projecttion, null, null, null)!!
        val nameIndex = cursor.getColumnIndex(projecttion[0])
        val numberIndex = cursor.getColumnIndex(projecttion[1])
        val photoIndex = cursor.getColumnIndex(projecttion[2])
        val ringtoneIndex = cursor.getColumnIndex(projecttion[3])
        try {
            val defaultRingtone = getUriRingtoneDefault(mContext)
            defaultRingtone?.let {
                if (cursor.moveToFirst()) {
                    do {
                        val name = cursor.getString(nameIndex)
                        val number = cursor.getString(numberIndex)
                        val photoUri = cursor.getString(photoIndex)
                        val ringtone = cursor.getString(ringtoneIndex)


                        if (TextUtils.equals(oldRingtoneDefault, defaultRingtone)) {
                            if (ringtone != null) {
                                if (TextUtils.equals(ringtone, defaultRingtone)) {
                                    newListContact.add(ContactItem(name, number, photoUri, defaultRingtone, true, Utils.getNameByUri(mContext, defaultRingtone)))
                                } else {
                                    newListContact.add(ContactItem(name, number, photoUri, ringtone, false,Utils.getNameByUri(mContext, ringtone)))
                                }
                            } else {
                                newListContact.add(ContactItem(name, number, photoUri, defaultRingtone, true, Utils.getNameByUri(mContext, defaultRingtone)))
                            }
                        } else {
                            if (ringtone != null) {
                                if (TextUtils.equals(ringtone, oldRingtoneDefault)) {
                                    newListContact.add(ContactItem(name, number, photoUri, defaultRingtone, true, Utils.getNameByUri(mContext, defaultRingtone)))
                                    // set nhac chuong
                                } else {
                                    if (TextUtils.equals(ringtone, defaultRingtone)) {
                                        newListContact.add(ContactItem(name, number, photoUri, defaultRingtone, true,Utils.getNameByUri(mContext, defaultRingtone)))
                                    } else {
                                        newListContact.add(ContactItem(name, number, photoUri, ringtone, false,Utils.getNameByUri(mContext, ringtone)))
                                    }
                                }
                            } else {
                                newListContact.add(ContactItem(name, number, photoUri, defaultRingtone, true,Utils.getNameByUri(mContext, defaultRingtone)))
                            }
                        }
                    } while (cursor.moveToNext())
                }
                oldRingtoneDefault = defaultRingtone
            }

        } finally {
            if (!cursor.isClosed) cursor.close()
        }
        newListContact
    }

    // lay uri cua ringtone mac dinh
    fun getUriRingtoneDefault(context: Context): String? {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
            return RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_RINGTONE)
                .toString()
        } else {
            return RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE).toString()
        }
    }

    override fun getListContact(): LiveData<GetContactResult> {
        CoroutineScope(Dispatchers.Default).launch {
            val listContact = scanContact()
            contactLiveData.postValue(GetContactResult(true, listContact))
        }

        return contactLiveData
    }

    class ContactObserver(handler: Handler?) : ContentObserver(handler) {       // nhan event khi thay doi data tu bo nho
        override fun onChange(selfChange: Boolean, uri: Uri?) {
            CoroutineScope(Dispatchers.Default).launch {
                contactLiveData.postValue(GetContactResult(true, scanContact()))
            }
        }
    }

    fun registerContentObserVerDeleted() {
        mContext.contentResolver.registerContentObserver(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, true, contactObserver)
        mContext.contentResolver.registerContentObserver(android.provider.Settings.System.CONTENT_URI, true, contactObserver)
    }

    fun unRegisterContentObserve() {
        mContext.contentResolver.unregisterContentObserver(contactObserver)
    }


}