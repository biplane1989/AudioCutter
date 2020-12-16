package com.example.audiocutter.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.*
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.text.TextUtils
import android.util.TypedValue
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.core.content.res.ResourcesCompat
import com.example.audiocutter.R
import com.example.audiocutter.core.audiomanager.AudioFileManagerImpl
import com.example.audiocutter.core.audiomanager.Folder
import com.example.audiocutter.core.manager.ManagerFactory
import com.example.audiocutter.functions.audiochooser.objects.ItemAppShare
import com.example.audiocutter.objects.AudioFile
import com.example.core.core.AudioInfor
import java.io.ByteArrayInputStream
import java.io.File
import java.io.InputStream
import java.text.Normalizer
import java.text.SimpleDateFormat
import java.util.regex.Pattern


class Utils {
    companion object {
        val KEY_SEND_PATH = "key_send_path"
        val FIVE_SECOND = 5000
        val TIME_CHANGE = 100
        val generatedNameHashMap = HashMap<Folder, HashSet<String>>()

        var TIME_FORMAT_INCLUDED_HOUR_TWO_ZERO = 1
        var TIME_FORMAT_INCLUDED_MINUTE_TWO_ZERO = 2
        var TIME_FORMAT_INCLUDED_SECOND_TWO_ZERO = 3


        fun addGeneratedName(folder: Folder, file: File) {
            if (!generatedNameHashMap.containsKey(folder)) {
                generatedNameHashMap.put(folder, HashSet())
            }
            generatedNameHashMap.get(folder)!!.add(getBaseName(file))
        }

        fun removeGeneratedName(folder: Folder, file: File) {
            if (generatedNameHashMap.containsKey(folder)) {
                generatedNameHashMap.get(folder)!!.remove(getBaseName(file))
            }
        }

        fun contains(folder: Folder, baseName: String): Boolean {
            if (generatedNameHashMap.containsKey(folder)) {
                return generatedNameHashMap.get(folder)!!.contains(baseName)
            }
            return false
        }

        fun dpToPx(context: Context, dp: Float): Float {
            return dp * context.resources.displayMetrics.density + 0.5f
        }


        fun pxToDp(context: Context, px: Int): Int {
            return (px / context.resources.displayMetrics.density).toInt()
        }

        fun spToPx(context: Context, sp: Float): Float {
            return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, context.resources.displayMetrics)
        }

        fun longDurationMsToStringMs(time: Long): String {
            val seconds = time / 1000
            val minutes = seconds / 60
            val oddSeconds = seconds - minutes * 60
            val oddMSeconds = (time - (minutes * 60 + oddSeconds) * 1000) / 100
            return minutes.toString() + ":" + (if (oddSeconds > 9) oddSeconds else "0$oddSeconds") + "." + oddMSeconds
        }

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
            return minutes.toString() + ":" + ((if (oddSeconds > 9) oddSeconds.toString() else "0$oddSeconds").toString() + if (oddMsTrimmed != 0L) ".$oddMsTrimmed" else "")
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
            val r = RingtoneManager.getRingtone(context, Uri.parse(uri))
            return r.getTitle(context)
        }

        // lay path bai hat theo uri
        fun getPathByUri(context: Context, uri: String): String? {
            var audioTitle = ""
            val proj = arrayOf(MediaStore.Audio.Media.DATA)
            val audioCursor: Cursor? = context.contentResolver.query(Uri.parse(uri), proj, null, null, null)
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

        fun checkUriIsExits(context: Context, uri: String): Boolean {       // kiem tra uri co ton tai khong

            val projecttion = arrayOf(MediaStore.MediaColumns.DATA)
            val cursor: Cursor? = context.contentResolver.query(Uri.parse(uri), projecttion, null, null, null)
            if (cursor != null) {
                try {
                    if (cursor.moveToFirst()) {
                        val filePath = cursor.getString(0)
                        return File(filePath).exists()
                    } else {
                        return false        // Uri was ok but no entry found.
                    }
                } finally {
                    cursor.close()
                }
            } else {
                return false    // content Uri was invalid or some other error occurred
            }
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

        fun convertValue(min1: Int, max1: Int, min2: Int, max2: Int, value: Int): Int {
            return ((value - min1) * ((max2 - min2) * 1f / (max1 - min1)) + min2).toInt()
        }


        @SuppressLint("SimpleDateFormat")
        fun convertTime(time: Int): String {
            if (time < 0) return "00:00"
            val df = SimpleDateFormat("mm:ss")
            return df.format(time)
        }

        fun getWidthTextPlayController(str: String, context: Context, textSize: Int): Float {
            val paint = Paint()
            paint.textSize = convertDp2Px(textSize, context)
            val typeface: Typeface? = ResourcesCompat.getFont(context, R.font.opensans_regular)
            typeface?.let {
                paint.typeface = typeface
            }
            val result = Rect()
            paint.getTextBounds(str, 0, str.length, result)
            return result.width().toFloat()
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

        fun getBaseName(file: File): String {
            val fileName = file.name
            val dotPos = fileName.indexOf(".")
            if (dotPos != -1) {
                return fileName.substring(0, dotPos)
            }
            return fileName
        }

        fun getAllFileNameFromFolder(folderPath: String): HashSet<String> {
            val hashSet = HashSet<String>()
            val folder = File(folderPath)
            if (folder.exists()) {
                folder.listFiles()?.forEach {
                    hashSet.add(getBaseName(it))
                }
            }
            return hashSet
        }

        @SuppressLint("SimpleDateFormat")
        fun genAudioFileName(typeFile: Folder, newName: String = "", prefixName: String = "file"): String {
            var baseFileName = ""
            //val dateStr = SimpleDateFormat("dd_MM_yyyy").format(Date())
            val folderPath = ManagerFactory.getAudioFileManager().getFolderPath(typeFile)
            baseFileName = if (newName.isEmpty()) {
                when (typeFile) {
                    Folder.TYPE_CUTTER -> {
                        "cutting_${prefixName}"
                    }
                    Folder.TYPE_MERGER -> {
                        "merging_${prefixName}"
                    }
                    Folder.TYPE_MIXER -> {
                        "mixing_${prefixName}"
                    }
                }
            } else {
                newName
            }
            val listBaseName = getAllFileNameFromFolder(folderPath)
            var fileName = baseFileName
            var index = 0
            while (listBaseName.contains(fileName) || contains(typeFile, fileName)) {
                index += 1
                fileName = "%s(%d)".format(baseFileName, index)
            }
            return fileName
        }

        fun openWithApp(context: Context, uri: Uri) {
            val intent = Intent()
            intent.action = Intent.ACTION_VIEW
            intent.setDataAndType(uri, "audio/*")
            context.startActivity(intent)
        }

        @SuppressLint("QueryPermissionsNeeded")
        fun getListAppQueryReceiveData(context: Context): List<ItemAppShare> {
            val listAppShares = ArrayList<ItemAppShare>()
            val intent = Intent()
            intent.action = Intent.ACTION_SEND
            intent.type = "audio/*"
            val listResolver = context.packageManager.queryIntentActivities(intent, 0)

            for (info in listResolver) {
                val item = ItemAppShare(info.loadLabel(AudioFileManagerImpl.mContext.packageManager)
                    .toString(), info.loadIcon(AudioFileManagerImpl.mContext.packageManager), info.activityInfo.packageName)
                listAppShares.add(item)
            }
            return listAppShares
        }

        @SuppressLint("QueryPermissionsNeeded")
        fun getListAppQueryReceiveMutilData(context: Context): List<ItemAppShare> {
            val listAppShares = ArrayList<ItemAppShare>()
            val intent = Intent()
            intent.action = Intent.ACTION_SEND_MULTIPLE
            intent.type = "audio/*"
            val listResolver = context.packageManager.queryIntentActivities(intent, 0)

            for (info in listResolver) {
                val item = ItemAppShare(info.loadLabel(AudioFileManagerImpl.mContext.packageManager)
                    .toString(), info.loadIcon(AudioFileManagerImpl.mContext.packageManager), info.activityInfo.packageName)
                listAppShares.add(item)
            }
            return listAppShares
        }


        fun shareFileAudio(context: Context, uriAudioFile: Uri, pkgName: String?): Boolean {
            return try {
                val intent = Intent()
                if (pkgName != null) {
                    intent.`package` = pkgName
                }
                intent.action = Intent.ACTION_SEND
                intent.putExtra(Intent.EXTRA_STREAM, uriAudioFile)
                intent.type = "audio/*"
                context.startActivity(Intent.createChooser(intent, context.resources.getString(R.string.Choose_in_app_inten)))
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }


        fun shareMutilpleFile(context: Context, listUri: ArrayList<Uri>, pkgName: String?) {
            val shareIntent = Intent()
            if (pkgName != null) {
                shareIntent.`package` = pkgName
            }
            shareIntent.action = Intent.ACTION_SEND_MULTIPLE
            shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, listUri)
            shareIntent.type = "audio/*"
            context.startActivity(Intent.createChooser(shareIntent, context.resources.getString(R.string.Choose_in_app_inten)))
        }

        private fun getName(file: File): String {
            val fileName = file.name
            val index = fileName.lastIndexOf(".")

            if (index != -1) {
                return fileName.substring(0, index)
            }
            return fileName
        }

        private fun getBitmapByPath(path: String?): Bitmap? {
            try {
                val mmr = MediaMetadataRetriever()
                mmr.setDataSource(path)
                var inputStream: InputStream? = null
                if (mmr.embeddedPicture != null) {
                    inputStream = ByteArrayInputStream(mmr.embeddedPicture)
                }
                mmr.release()

                return BitmapFactory.decodeStream(inputStream)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return null
        }

        fun convertToAudioFile(audioInfor: AudioInfor, modified: Long, uri: Uri): AudioFile {
            val file = File(audioInfor.filePath)
            return AudioFile(file, getName(file), audioInfor.size, audioInfor.bitRate, audioInfor.duration, uri, getBitmapByPath(file.absolutePath), audioInfor.title, audioInfor.alBum, audioInfor.artist, modified, audioInfor.genre, audioInfor.format)
        }

        fun hideKeyboard(context: Context, editText: EditText) {
            val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(editText.windowToken, 0)
        }

        fun showKeyboard(context: Context, editText: EditText) {
            val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
        }

        fun checkFlashOnDeviceAvailable(context: Context): Boolean? {
            return context.packageManager?.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)
        }

        fun toTimeStr(timeInMs: Long, timeFormat: Int): String? {
            val hours = (timeInMs / 36e5).toInt()
            var remainingTime = (timeInMs - hours * 36e5).toLong()
            val minutes = (remainingTime / 6e4).toInt()
            remainingTime = (remainingTime - minutes * 6e4).toLong()
            val seconds = (remainingTime / 1e3).toInt()
            when (timeFormat) {
                TIME_FORMAT_INCLUDED_HOUR_TWO_ZERO -> return String.format("%02d:%02d:%02d", hours, minutes, seconds)
                TIME_FORMAT_INCLUDED_MINUTE_TWO_ZERO -> return String.format("%02d:%02d", minutes, seconds)
                TIME_FORMAT_INCLUDED_SECOND_TWO_ZERO -> return String.format("00:%02d", seconds)
            }
            return String.format("%02d:%02d:%02d", hours, minutes, seconds)
        }

        fun chooseTimeFormat(duration: Long): Int {
            val hours = (duration / 36e5).toInt()
            var remainingTime = (duration - hours * 36e5).toLong()
            val minutes = (remainingTime / 6e4).toInt()
            return if (hours > 0) {
                TIME_FORMAT_INCLUDED_HOUR_TWO_ZERO
            } else {
                if (minutes > 0) {
                    TIME_FORMAT_INCLUDED_MINUTE_TWO_ZERO
                } else {
                    TIME_FORMAT_INCLUDED_SECOND_TWO_ZERO
                }
            }
        }

    }
}