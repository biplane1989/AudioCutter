package com.example.audiocutter.util

import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.text.TextUtils
import android.util.TypedValue
import com.example.audiocutter.functions.contactscreen.contacts.ContactInfomation
import java.text.Normalizer
import java.util.regex.Pattern

object Utils {

    // loai bo ky tu chuyen ve dang aphalbet
    fun stripAccents(str: String): String {
        var newStr = Normalizer.normalize(str, Normalizer.Form.NFD)
        newStr = newStr.replace("[\\p{InCombiningDiacriticalMarks}]".toRegex(), "")
        return newStr
    }

    // lay ten bai hat theo uri
    fun getNameByUri(context: Context, uri: String): ContactInfomation {
        var contactInfomation = ContactInfomation("", "")
        var result: String? = null
        val newUri = Uri.parse(uri)

        if (newUri.getScheme().equals("content")) {
            val cursor: Cursor? = context.contentResolver.query(newUri, null, null, null, null)
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                }
            } finally {
                cursor!!.close()
            }
        }
        if (result == null) {
            result = newUri.getPath()
            val cut: Int? = result?.lastIndexOf('/')
            if (cut != -1) {
                result = cut?.plus(1)?.let { result!!.substring(it) }
            }
        }
        contactInfomation = result?.let { ContactInfomation(result, it) }!!
        return contactInfomation
    }

    // lay path bai hat theo uri
    fun getPathByUri(context: Context, uri: String): String? {
        var audioTitle = ""
        val proj = arrayOf(MediaStore.Audio.Media.DATA)
        var audioCursor: Cursor? = context.contentResolver.query(Uri.parse(uri), proj, null, null, null)
        try {
            if (audioCursor != null) {
                if (audioCursor.moveToFirst()) {
                    audioTitle = audioCursor.getString(0)
                }
            }
        } finally {
            audioCursor?.close()
        }
        return audioTitle
    }

    // lay uri cua ringtone mac dinh
    fun getUriRingtoneDefault(context: Context): String? {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
            if (RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_RINGTONE) != null) {
                return RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_RINGTONE)
                    .toString()
            }
        } else {
            if (RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE) != null) {
                return RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE).toString()
            }
        }
        return null
    }

    // lay bitmap theo path
    fun getImageCover(context: Context, path: String?): Bitmap? {
        try {
            if (path != null) {
                val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, Uri.parse(path))
                return bitmap
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    // check ringtone contact co phai la ringtone default khong?
    fun checkRingtoneDefault(context: Context, uri: String): Boolean {
        if (TextUtils.equals(uri, getUriRingtoneDefault(context).toString())) return true
        return false
    }

    // convert ky tu co dau sang khong dau
    fun covertToString(value: String?): String? {
        try {
            val temp = Normalizer.normalize(value, Normalizer.Form.NFD)
            val pattern: Pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+")
            return pattern.matcher(temp).replaceAll("")
        } catch (ex: java.lang.Exception) {
            ex.printStackTrace()
        }
        return null
    }

    fun convertValue(min1: Double, max1: Double, min2: Double, max2: Double, value: Double): Double {
        return ((value - min1) * ((max2 - min2) / (max1 - min1)) + min2)
    }


    fun convertDp2Px(dip: Int, context: Context): Float {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip.toFloat(), context.resources.displayMetrics)
    }


}