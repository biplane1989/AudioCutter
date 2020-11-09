package com.example.audiocutter.util

import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.Rect
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.text.TextUtils
import android.util.Log
import android.util.TypedValue
import java.io.File
import java.text.Normalizer
import java.text.SimpleDateFormat
import java.util.regex.Pattern

object Utils {
    val KEY_SEND_PATH = "key_send_path"
    val KEY_SEND_AUDIO = "key_send_audio"
    val FIVE_SECOND = 5000
    val TIME_CHANGE = 100

    @JvmStatic
    fun dpToPx(context: Context, dp: Float): Float {
        return dp * context.resources.displayMetrics.density + 0.5f
    }

    @JvmStatic
    fun pxToDp(context: Context, px: Int): Int {
        return (px / context.resources.displayMetrics.density).toInt()
    }

    @JvmStatic
    fun spToPx(context: Context, sp: Float): Float {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, context.resources.displayMetrics)
    }

    @JvmStatic
    fun longDurationMsToStringMs(time: Long): String {
        val seconds = time / 1000
        val minutes = seconds / 60
        val oddSeconds = seconds - minutes * 60
        val oddMSeconds = (time - (minutes * 60 + oddSeconds) * 1000) / 100
        return minutes.toString() + ":" + (if (oddSeconds > 9) oddSeconds else "0$oddSeconds") + "." + oddMSeconds
    }

    @JvmStatic
    fun longMsToString(ms: Long): String {
        val seconds = ms / 1000
        val minutes = seconds / 60
        val oddSeconds = seconds - minutes * 60
        var oddMs = ms - seconds * 1000
        oddMs = if (oddMs < 250 || oddMs > 750) {
            0
        } else if (oddMs <= 500) {
            500
        } else {
            750
        }
        val oddMsTrimmed = oddMs / 10
        return minutes.toString() + ":" + ((if (oddSeconds > 9) oddSeconds else "0$oddSeconds").toString() + if (oddMsTrimmed != 0L) ".$oddMsTrimmed" else "")
    }

    fun getWidthText(str: String = "00:00:00", context: Context): Float {
        val paint = Paint()
        paint.textSize = spToPx(context, 12f)
        val result = Rect()
        paint.getTextBounds(str, 0, str.length, result)
        return result.width().toFloat()
    }

    // loai bo ky tu chuyen ve dang aphalbet
    fun stripAccents(str: String): String {
        var newStr = Normalizer.normalize(str, Normalizer.Form.NFD)
        newStr = newStr.replace("[\\p{InCombiningDiacriticalMarks}]".toRegex(), "")
        return newStr
    }

    // lay ten bai hat theo uri
    fun getNameByUri(context: Context, uri: String): String {
        Log.d("giangtd", "getNameByUri: uri: " + uri)
        var fileName = ""
        try {
            val newUri = Uri.parse(uri)
            if (newUri.getScheme().equals("content")) {
                val cursor: Cursor? = context.contentResolver.query(newUri, null, null, null, null)
                try {
                    if (cursor != null && cursor.moveToFirst()) {
                        fileName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                    }
                } finally {
                    cursor!!.close()
                }
            }
        } catch (e: Exception) {

        }
        if (fileName.isEmpty()) {
            fileName = File(uri).name
        }
        return fileName
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
                return RingtoneManager.getActualDefaultRingtoneUri(context.applicationContext, RingtoneManager.TYPE_RINGTONE)
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


    fun convertTime(time: Int): String {
        if (time < 0) return "00:00"
        val df = SimpleDateFormat("mm:ss")
        return df.format(time)
    }


    fun convertDp2Px(dip: Int, context: Context): Float {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip.toFloat(), context.resources.displayMetrics)
    }

    //test
    fun getTimeAudio(file: File, context: Context): Long {
        val mp: MediaPlayer = MediaPlayer.create(context, Uri.parse(file.absolutePath))
        val duration = mp.duration
        mp.release()
        return duration.toLong()
    }

     fun getAlphaNumericString(n: Int): String? {

        // chose a Character random from this String
        val AlphaNumericString = ("ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                + "0123456789"
                + "abcdefghijklmnopqrstuvxyz")

        // create StringBuffer size of AlphaNumericString
        val sb = StringBuilder(n)
        for (i in 0 until n) {

            // generate a random number between
            // 0 to AlphaNumericString variable length
            val index = (AlphaNumericString.length
                    * Math.random()).toInt()

            // add Character one by one in end of sb
            sb.append(AlphaNumericString[index])
        }
        return sb.toString()
    }

}