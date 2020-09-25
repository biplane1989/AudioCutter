package com.example.audiocutter.util

import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.media.RingtoneManager
import android.net.Uri
import android.provider.MediaStore
import java.text.Normalizer

object Utils {

    // loai bo ky tu chuyen ve dang aphalbet
    fun stripAccents(str: String): String {
        var newStr = Normalizer.normalize(str, Normalizer.Form.NFD)
        newStr = newStr.replace("[\\p{InCombiningDiacriticalMarks}]".toRegex(), "")
        return newStr
    }

    // lay ten bai hat theo uri
    fun getPlayList(context: Context, uri: String): String? {
        var audioTitle = ""
        val proj = arrayOf(MediaStore.Audio.Media.TITLE)
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
    fun getCurrentSound(context: Context): Uri? {
        var ringtone_uri = RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_RINGTONE)
        if (ringtone_uri == null) {
            // if ringtone_uri is null get Default Ringtone
            ringtone_uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
        }
        return ringtone_uri
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
}