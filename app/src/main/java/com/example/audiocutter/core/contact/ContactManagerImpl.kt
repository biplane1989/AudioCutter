package com.example.audiocutter.core.contact

import android.content.Context
import android.database.ContentObserver
import android.database.Cursor
import android.net.Uri
import android.os.Handler
import android.provider.ContactsContract
import android.text.TextUtils
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.audiocutter.R
import com.example.audiocutter.core.manager.ContactManager
import com.example.audiocutter.functions.contacts.objects.GetContactResult
import com.example.audiocutter.objects.ContactItem
import com.example.audiocutter.util.Utils
import kotlinx.coroutines.*

class ContactManagerImpl(val appContext: Context) : ContactManager {
    enum class ScanningState {                  // cac trang thai de xu ly khi dang loading ma nguoi dung nhan back
        IDLE, RUNNING, WAITING_FOR_CANCELING
    }

    private val contactLiveData = MutableLiveData<GetContactResult>()

    private val TAG = "giangtd"

    private var oldRingtoneDefault = ""
    private val contactObserver = ContactObserver(Handler())
    private val backgroundScope = CoroutineScope(Dispatchers.Default)
    private var scanningState = ScanningState.IDLE





    override fun scanContact() {
        if (scanningState == ScanningState.WAITING_FOR_CANCELING) {
            return
        }
        backgroundScope.launch {
            if (scanningState == ScanningState.RUNNING) {
                scanningState = ScanningState.WAITING_FOR_CANCELING
                while (scanningState == ScanningState.WAITING_FOR_CANCELING) {
                    delay(100)
                }
            }
            scanningState = ScanningState.RUNNING

            contactLiveData.postValue(GetContactResult(false))

            val newListContact: ArrayList<ContactItem> = ArrayList()
            val projection = arrayOf(ContactsContract.CommonDataKinds.Phone.CONTACT_ID, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.CommonDataKinds.Phone.PHOTO_URI, ContactsContract.CommonDataKinds.Phone.CUSTOM_RINGTONE)
            val cursor: Cursor? = appContext.contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, projection, null, null, null)

            cursor?.let {
                val idIndex = cursor.getColumnIndex(projection[0])
                val nameIndex = cursor.getColumnIndex(projection[1])
                val numberIndex = cursor.getColumnIndex(projection[2])
                val photoIndex = cursor.getColumnIndex(projection[3])
                val ringtoneIndex = cursor.getColumnIndex(projection[4])
                try {
                    val defaultRingtone = Utils.getUriRingtoneDefault(appContext) ?: ""

                    if (cursor.moveToFirst()) {
                        do {
//                            delay(1000)
                            if (!isActive || scanningState == ScanningState.WAITING_FOR_CANCELING) {
                                break
                            }
                            val id = cursor.getString(idIndex)
                            val name = cursor.getString(nameIndex)
                            val number = cursor.getString(numberIndex)
                            val photoUri = cursor.getString(photoIndex)
                            val ringtone = cursor.getString(ringtoneIndex)


                            var isRingtoneDefault = true
                            var ringtoneFilePath = ""
                            var filename = ""
                            if (Utils.checkUriIsExits(appContext, defaultRingtone)) {           // check co ton tai hay khong
                                if (TextUtils.equals(oldRingtoneDefault, defaultRingtone)) {
                                    if (ringtone != null) {
                                        if (Utils.checkUriIsExits(appContext, ringtone)) {
                                            if (TextUtils.equals(ringtone, defaultRingtone)) {
                                                isRingtoneDefault = true
                                                ringtoneFilePath = defaultRingtone
                                            } else {
                                                isRingtoneDefault = false
                                                ringtoneFilePath = ringtone
                                            }
                                        } else {
                                            isRingtoneDefault = true
                                            ringtoneFilePath = defaultRingtone
                                        }
                                    } else {
                                        isRingtoneDefault = true
                                        ringtoneFilePath = defaultRingtone
                                    }
                                } else {
                                    if (ringtone != null) {
                                        if (Utils.checkUriIsExits(appContext, ringtone)) {
                                            if (TextUtils.equals(ringtone, oldRingtoneDefault)) {
                                                isRingtoneDefault = true
                                                ringtoneFilePath = defaultRingtone
                                            } else {
                                                if (TextUtils.equals(ringtone, defaultRingtone)) {
                                                    isRingtoneDefault = true
                                                    ringtoneFilePath = defaultRingtone
                                                } else {
                                                    isRingtoneDefault = false
                                                    ringtoneFilePath = ringtone
                                                }
                                            }
                                        } else {
                                            isRingtoneDefault = true
                                            ringtoneFilePath = defaultRingtone
                                        }
                                    } else {
                                        isRingtoneDefault = true
                                        ringtoneFilePath = defaultRingtone
                                    }
                                }
                                filename = Utils.getNameByUri(appContext, ringtoneFilePath)
                            } else {
                                if (ringtone != null) {
                                    if (Utils.checkUriIsExits(appContext, ringtone)) {
                                        isRingtoneDefault = false
                                        ringtoneFilePath = ringtone
                                        filename = Utils.getNameByUri(appContext, ringtoneFilePath)
                                    } else {
                                        filename = appContext.getString(R.string.contact_managerimpl_default_ringtone)
                                        ringtoneFilePath = ""
                                        isRingtoneDefault = true
                                    }
                                } else {
                                    filename = appContext.getString(R.string.contact_managerimpl_default_ringtone)
                                    ringtoneFilePath = ""
                                    isRingtoneDefault = true
                                }
                            }

                            val contactItem = ContactItem(id, name, number, photoUri, ringtoneFilePath, isRingtoneDefault, filename)
                            newListContact.add(contactItem)

                            oldRingtoneDefault = defaultRingtone
                        } while (cursor.moveToNext())

                    }
                    if (scanningState == ScanningState.RUNNING) {
                        contactLiveData.postValue(GetContactResult(true, newListContact))
                    }

                } finally {
                    if (!cursor.isClosed) cursor.close()
                    scanningState = ScanningState.IDLE
                }
            }
        }
    }

    override fun getListContact(): LiveData<GetContactResult> {
        return contactLiveData
    }

    inner class ContactObserver(handler: Handler?) : ContentObserver(handler) {       // nhan event khi thay doi data tu bo nho
        override fun onChange(selfChange: Boolean, uri: Uri?) {
            scanContact()
        }
    }

    fun registerContentObserVerDeleted() {
        appContext.contentResolver.registerContentObserver(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, true, contactObserver)
        appContext.contentResolver.registerContentObserver(android.provider.Settings.System.CONTENT_URI, true, contactObserver)
    }

    fun unRegisterContentObserve() {
        appContext.contentResolver.unregisterContentObserver(contactObserver)
    }

    override fun setup() {
        registerContentObserVerDeleted()
    }

    override fun release() {
        unRegisterContentObserve()
        backgroundScope.cancel()
    }
}