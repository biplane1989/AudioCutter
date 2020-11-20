package com.example.audiocutter.core.rington

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.media.RingtoneManager
import android.net.Uri
import android.provider.ContactsContract
import android.provider.MediaStore
import android.util.Log
import com.example.audiocutter.core.manager.RingtonManager
import com.example.audiocutter.objects.AudioFile
import java.io.File

object RingtonManagerImpl : RingtonManager {

    private val IS_ALARM = 1
    private val IS_NOTIFICATION = 2
    private val IS_RINGTONE = 3
    lateinit var mContext: Context

    fun init(context: Context) {
        mContext = context
    }

    override fun setAlarmManager(audioFile: AudioFile): Boolean {
        val uri = getOrNew(audioFile.file.absolutePath, IS_ALARM)
        if (uri != null) {
            RingtoneManager.setActualDefaultRingtoneUri(mContext, RingtoneManager.TYPE_ALARM, uri)
            return true
        }
        return false
    }

    override fun setNotificationSound(audioFile: AudioFile): Boolean {
        val uri = getOrNew(audioFile.file.absolutePath, IS_NOTIFICATION)
        if (uri != null) {
            RingtoneManager.setActualDefaultRingtoneUri(mContext, RingtoneManager.TYPE_NOTIFICATION, uri)
            return true
        }
        return false
    }

    override fun setRingTone(audioFile: AudioFile): Boolean {
        val uri = getOrNew(audioFile.file.absolutePath, IS_RINGTONE)
        if (uri != null) {
            RingtoneManager.setActualDefaultRingtoneUri(mContext, RingtoneManager.TYPE_RINGTONE, uri)
            return true
        }
        return false
    }

    override fun setRingToneWithContactNumberandFilePath(filePath: String, contactNumber: String): Boolean {
        val values = ContentValues()
        val resolver: ContentResolver = mContext.getContentResolver()
        val uri = getOrNew(filePath, IS_RINGTONE)
        if (uri != null) {
            val lookupUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, contactNumber)
            val projection = arrayOf(ContactsContract.Contacts._ID, ContactsContract.Contacts.LOOKUP_KEY)
            val cursor: Cursor? = mContext.getContentResolver()
                .query(lookupUri, projection, null, null, null)

            if (cursor != null) {
                try {
                    if (cursor.moveToFirst()) {
                        do {
                            // Get the contact lookup Uri
                            val contactId = cursor.getLong(0)
                            val lookupKey = cursor.getString(1)
                            val contactUri = ContactsContract.Contacts.getLookupUri(contactId, lookupKey)
                            val uriString = uri.toString()
                            values.put(ContactsContract.Contacts.CUSTOM_RINGTONE, uriString)
                            resolver.update(contactUri, values, null, null).toLong()
                        } while (cursor.moveToNext())

                    } else {
                        return false
                    }
                } finally {
                    if (!cursor.isClosed) cursor.close()
                }
                return true
            }
        }
        return false
    }

    override fun setRingToneWithContactNumberAndUri(pathUri: String, contactNumber: String): Boolean {

        val values = ContentValues()
        val resolver: ContentResolver = mContext.getContentResolver()
        val uri = getOrNew(pathUri, IS_RINGTONE)
        if (uri != null) {

            Log.d("giangtd", "uri ringtone: " + uri)
            val lookupUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, contactNumber)
            val projection = arrayOf(ContactsContract.Contacts._ID, ContactsContract.Contacts.LOOKUP_KEY)
            val cursor: Cursor? = mContext.getContentResolver()
                .query(lookupUri, projection, null, null, null)

            if (cursor != null) {
                try {
                    if (cursor.moveToFirst()) {
                        do {
                            // Get the contact lookup Uri
                            val contactId = cursor.getLong(0)
                            val lookupKey = cursor.getString(1)
                            val contactUri = ContactsContract.Contacts.getLookupUri(contactId, lookupKey)
                            val uriString = uri.toString()
                            values.put(ContactsContract.Contacts.CUSTOM_RINGTONE, uriString)
                            resolver.update(contactUri, values, null, null).toLong()
                        } while (cursor.moveToNext())
                    } else {
                        return false
                    }
                } finally {
                    if (!cursor.isClosed) cursor.close()
                }
                return true
            }
        }
        return false
    }

    override fun setRingtoneDefault(uri: String, contactNumber: String): Boolean {
        val values = ContentValues()
        val resolver: ContentResolver = mContext.getContentResolver()

        Log.d("giangtd", "uri ringtone: " + uri)
        val lookupUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, contactNumber)
        val projection = arrayOf(ContactsContract.Contacts._ID, ContactsContract.Contacts.LOOKUP_KEY)
        val cursor: Cursor? = mContext.getContentResolver()
            .query(lookupUri, projection, null, null, null)

        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    do {
                        // Get the contact lookup Uri
                        val contactId = cursor.getLong(0)
                        val lookupKey = cursor.getString(1)
                        val contactUri = ContactsContract.Contacts.getLookupUri(contactId, lookupKey)
                        val uriString = uri.toString()
                        values.put(ContactsContract.Contacts.CUSTOM_RINGTONE, uriString)
                        resolver.update(contactUri, values, null, null).toLong()
                    } while (cursor.moveToNext())

                }
            } finally {
                if (!cursor.isClosed) cursor.close()
            }
            return true
        }
        return false
    }

    fun getUriFromFile(filePath: String): Uri? {
        val folder = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(MediaStore.Audio.Media.DISPLAY_NAME, MediaStore.Audio.Media._ID, MediaStore.Audio.Media.DATA)
        val cursor: Cursor? = mContext.getContentResolver()
            .query(folder, projection, MediaStore.Audio.Media.DATA + "=?", arrayOf(filePath), null)
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    return Uri.parse(folder.toString() + File.separator + cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media._ID)))
                }
            } finally {
                cursor.close()
            }
        }
        return null
    }

    fun getOrNew(filePath: String, typeRing: Int): Uri? {
        val resolver: ContentResolver = mContext.getContentResolver()
        val file = File(filePath)
        if (file.exists()) {
            val oldUri = getUriFromFile(filePath)
            if (oldUri != null) {
                return oldUri
            } else {
                val values = ContentValues()
                values.put(MediaStore.Audio.AudioColumns.DISPLAY_NAME, file.name)
                values.put(MediaStore.Audio.AudioColumns.DATA, file.absolutePath)
                values.put(MediaStore.Audio.AudioColumns.TITLE, file.name)
                values.put(MediaStore.Audio.AudioColumns.SIZE, file.length())
                values.put(MediaStore.Audio.AudioColumns.MIME_TYPE, "audio/*")              // sua lai .mp3 -> *
                when (typeRing) {
                    IS_ALARM -> values.put(MediaStore.Audio.AudioColumns.IS_ALARM, true)
                    IS_NOTIFICATION -> values.put(MediaStore.Audio.AudioColumns.IS_NOTIFICATION, true)
                    IS_RINGTONE -> values.put(MediaStore.Audio.AudioColumns.IS_RINGTONE, true)
                }
                return resolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values)
            }
        }
        return null
    }
}