package com.example.audiocutter.core.contact

import android.content.Context
import android.database.ContentObserver
import android.database.Cursor
import android.net.Uri
import android.os.Handler
import android.provider.ContactsContract
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.audiocutter.core.audiomanager.AudioFileManagerImpl
import com.example.audiocutter.core.manager.ContactManager
import com.example.audiocutter.core.manager.ManagerFactory
import com.example.audiocutter.functions.contacts.objects.GetContactResult
import com.example.audiocutter.objects.ContactItem
import com.example.audiocutter.util.Utils
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel

class ContactManagerImpl(val appContext: Context) : ContactManager {
    private val contactLiveData = MutableLiveData<GetContactResult>()
    private var oldRingtoneDefault = ""
    private val contactObserver = ContactObserver(Handler())
    private val backgroundScope = CoroutineScope(Dispatchers.Default)

    private suspend fun queryContacts(filterFunc: (String, String, String, String?, String?) -> Unit) =
        coroutineScope {
            val projection = arrayOf(
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Phone.PHOTO_URI,
                ContactsContract.CommonDataKinds.Phone.CUSTOM_RINGTONE
            )
            val cursor: Cursor? = appContext.contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                projection,
                null,
                null,
                null
            )
            try {
                cursor?.let {
                    val idIndex = it.getColumnIndex(projection[0])
                    val nameIndex = it.getColumnIndex(projection[1])
                    val numberIndex = it.getColumnIndex(projection[2])
                    val photoIndex = it.getColumnIndex(projection[3])
                    val ringtoneIndex = it.getColumnIndex(projection[4])
                    var hasRow = it.moveToFirst()
                    while (isActive && hasRow) {
                        val id = it.getString(idIndex)
                        val name = it.getString(nameIndex)
                        val number = it.getString(numberIndex)
                        val photoUri = it.getString(photoIndex)
                        val ringtone = it.getString(ringtoneIndex)
                        filterFunc(id, name, number, photoUri, ringtone)
                        hasRow = it.moveToNext()
                    }
                }
            } finally {
                cursor?.close()
            }
        }


    private fun checkAudioUri(uri: String?, cacheUriMap: HashMap<String, Boolean>): Boolean {
        if (uri == null) {
            return false
        }
        if (cacheUriMap.containsKey(uri)) {
            return cacheUriMap.get(uri)!!
        }

        if (ManagerFactory.getAudioFileManager().hasUri(uri)) {
            cacheUriMap.put(uri, true)
            return true
        }
        val result = Utils.checkUriIsExits(appContext, uri)
        cacheUriMap.put(uri, result)
        return result
    }

    private fun getNameByUri(uri: String, uriNameMap: HashMap<String, String>): String {
        if (uriNameMap.containsKey(uri)) {
            return uriNameMap.get(uri)!!
        }
        val result = Utils.getNameByUri(appContext, uri)
        uriNameMap.put(uri, result)
        return result
    }
    private suspend fun scan()= coroutineScope{
        contactLiveData.postValue(GetContactResult(false))

        val newListContact: ArrayList<ContactItem> = ArrayList()


        val defaultRingtone = Utils.getUriRingtoneDefault(appContext) ?: ""
        val hasDefault = !defaultRingtone.isEmpty()
        val cacheUriMap = HashMap<String, Boolean>()
        val uriNameMap = HashMap<String, String>()
        queryContacts { id, name, number, photoUri, ringtone ->
            val hasRingtone = ringtone != null
            val isUriRingtoneExisted = checkAudioUri(ringtone, cacheUriMap)
            val isDefaultEqualRingtone = defaultRingtone == ringtone
            var isRingtoneDefault = true
            var ringtoneFilePath = ""
            var filename = ""
            if (hasDefault && hasRingtone && isUriRingtoneExisted && isDefaultEqualRingtone) {
                //1
                filename = getNameByUri(ringtone!!, uriNameMap)
                ringtoneFilePath = defaultRingtone
                isRingtoneDefault = true
            }
            if (hasDefault && hasRingtone && !isUriRingtoneExisted) {
                //1
                filename = getNameByUri(defaultRingtone, uriNameMap)
                ringtoneFilePath = defaultRingtone
                isRingtoneDefault = true
            }

            if (hasDefault && !hasRingtone) {
                //1
                filename = getNameByUri(defaultRingtone, uriNameMap)
                ringtoneFilePath = defaultRingtone
                isRingtoneDefault = true
            }
            if (hasDefault && hasRingtone && isUriRingtoneExisted && !isDefaultEqualRingtone) {
                //3
                filename = getNameByUri(ringtone!!, uriNameMap)
                isRingtoneDefault = false
            }
            if (!hasDefault && hasRingtone && isUriRingtoneExisted) {
                //3
                filename = getNameByUri(ringtone!!, uriNameMap)
                ringtoneFilePath = ringtone
                isRingtoneDefault = false
            }

            if (!hasDefault && hasRingtone && isUriRingtoneExisted) {
                //2
                isRingtoneDefault = true
                ringtoneFilePath = ringtone!!
            }
            if (!hasDefault && !hasRingtone) {
                //2
                isRingtoneDefault = true
            }

            val contactItem = ContactItem(
                id,
                name,
                number,
                photoUri,
                ringtoneFilePath,
                isRingtoneDefault,
                filename
            )
            newListContact.add(contactItem)

            oldRingtoneDefault = defaultRingtone

        }
        if(isActive){
            contactLiveData.postValue(GetContactResult(true, newListContact))
        }


    }
    override suspend fun scanContact() {
       notifyDiskChanged()
    }

    inner class ContactObserver(handler: Handler?) :
        ContentObserver(handler) {       // nhan event khi thay doi data tu bo nho
        override fun onChange(selfChange: Boolean, uri: Uri?) {
            notifyDiskChanged()
        }
    }

    fun registerContentObserVerDeleted() {
        appContext.contentResolver.registerContentObserver(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            true,
            contactObserver
        )
        appContext.contentResolver.registerContentObserver(
            android.provider.Settings.System.CONTENT_URI,
            true,
            contactObserver
        )
    }

    fun unRegisterContentObserve() {
        appContext.contentResolver.unregisterContentObserver(contactObserver)
    }


    override fun getListContact(): LiveData<GetContactResult> {
        return contactLiveData
    }


    override fun setup() {
        registerContentObserVerDeleted()
    }

    override fun release() {
        unRegisterContentObserve()
        backgroundScope.cancel()
    }

    private val scanFileChannel = Channel<Any>(Channel.CONFLATED)
    private var scanFileJob: Job? = null
    private fun notifyDiskChanged() {
        backgroundScope.launch {
            scanFileChannel.send(true)

        }
    }

    init {

        backgroundScope.launch {
            while (true) {
                val signal = scanFileChannel.receive()
                scanFileJob?.let {
                    if (!it.isCompleted) {
                        it.cancelAndJoin()
                    }
                }
                scanFileJob = backgroundScope.launch {
                    scan()
                }
            }
        }
    }


}