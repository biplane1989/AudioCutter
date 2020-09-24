package com.example.audiocutter.core.manager

import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.ContactsContract
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.audiocutter.objects.ContactItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ContactManagerImpl : ContactManager {

    override suspend fun getListContact(context: Context): LiveData<List<ContactItem>> = withContext(Dispatchers.IO) {

        val listContact: MutableLiveData<List<ContactItem>> = MutableLiveData()

        val _listContact: ArrayList<ContactItem> = ArrayList()

        val projecttion = arrayOf(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.CommonDataKinds.Phone.PHOTO_URI)
        val phones: Cursor = context.contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, projecttion, null, null, null)!!
        val nameIndex = phones.getColumnIndex(projecttion[0])
        val numberIndex = phones.getColumnIndex(projecttion[1])
        val photoIndex = phones.getColumnIndex(projecttion[2])
        phones.moveToFirst()
        if (phones != null) {
            try {
                while (phones.moveToNext()) {
                    val name = phones.getString(nameIndex)
                    val number = phones.getString(numberIndex)
                    val photoUri = phones.getString(photoIndex)
                    Log.d("TAG", "getListData: $name - $number - $photoUri")

                    _listContact.add(ContactItem("0", name, number, photoUri))
                }
            } finally {
                phones.close()
            }
        }

        listContact.postValue(_listContact)
        listContact
    }

}