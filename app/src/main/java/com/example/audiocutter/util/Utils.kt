package com.example.audiocutter.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
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
import java.util.*
import java.util.regex.Pattern
import kotlin.collections.ArrayList
import kotlin.collections.HashSet

object Utils {
    val KEY_SEND_PATH = "key_send_path"
    val FIVE_SECOND = 5000
    val TIME_CHANGE = 100
    private const val APP_FOLDER_NAME = "AudioCutter"
    private const val CUTTING_FOLDER_NAME = "cutter"
    private const val MERGING_FOLDER_NAME = "merger"
    private const val MIXING_FOLDER_NAME = "mixer"

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
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            sp,
            context.resources.displayMetrics
        )
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
        val audioCursor: Cursor? =
            context.contentResolver.query(Uri.parse(uri), proj, null, null, null)
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
            if (RingtoneManager.getActualDefaultRingtoneUri(
                    context,
                    RingtoneManager.TYPE_RINGTONE
                ) != null
            ) {
                return RingtoneManager.getActualDefaultRingtoneUri(
                    context.applicationContext,
                    RingtoneManager.TYPE_RINGTONE
                )
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
                val bitmap =
                    MediaStore.Images.Media.getBitmap(context.contentResolver, Uri.parse(path))
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

    fun checkUriIsExits(
        context: Context,
        uri: String
    ): Boolean {       // kiem tra uri co ton tai khong

        val projecttion = arrayOf(MediaStore.MediaColumns.DATA)
        val cursor: Cursor? =
            context.contentResolver.query(Uri.parse(uri), projecttion, null, null, null)
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    val filePath = cursor.getString(0);
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

    fun convertValue(
        min1: Double,
        max1: Double,
        min2: Double,
        max2: Double,
        value: Double
    ): Double {
        return ((value - min1) * ((max2 - min2) / (max1 - min1)) + min2)
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
        val typeface: Typeface? = ResourcesCompat.getFont(context, R.font.sanfrancisco_medium)
        typeface?.let {
            paint.typeface = typeface
        }
        val result = Rect()
        paint.getTextBounds(str, 0, str.length, result)
        return result.width().toFloat()
    }


    fun convertDp2Px(dip: Int, context: Context): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dip.toFloat(),
            context.resources.displayMetrics
        )
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
        val AlphaNumericString =
            ("ABCDEFGHIJKLMNOPQRSTUVWXYZ" + "0123456789" + "abcdefghijklmnopqrstuvxyz")

        // create StringBuffer size of AlphaNumericString
        val sb = StringBuilder(n)
        for (i in 0 until n) {

            // generate a random number between
            // 0 to AlphaNumericString variable length
            val index = (AlphaNumericString.length * Math.random()).toInt()

            // add Character one by one in end of sb
            sb.append(AlphaNumericString[index])
        }
        return sb.toString()
    }

    private fun getAllFileName(folder: Folder): HashSet<String> {
        val folderPath = ManagerFactory.getAudioFileManager().getFolderPath(folder)
        val folder = File(folderPath)
        val fileNameHash = HashSet<String>()
        if (folder.exists()) {
            folder.listFiles()?.forEach {
                if (it.name.contains(".")) {
                    fileNameHash.add(it.name.substring(0, (it.name).lastIndexOf(".")))
                } else {
                    fileNameHash.add(it.name)
                }
            }
        }
        return fileNameHash

    }

    @SuppressLint("SimpleDateFormat")
    fun genNewAudioFileName(typeFile: Folder): String {
        val random = Random()
        val fileNameHash = getAllFileName(typeFile)
        var fileName = ""
        val day = SimpleDateFormat("dd_MM_YYYY").format(Date())
        var textName = ""
        fileName = when (typeFile) {
            Folder.TYPE_CUTTER -> {
                "${CUTTING_FOLDER_NAME}_${APP_FOLDER_NAME}_$day"
            }
            Folder.TYPE_MERGER -> {
                "${MERGING_FOLDER_NAME}_${APP_FOLDER_NAME}_$day"
            }
            Folder.TYPE_MIXER -> {
                "${MIXING_FOLDER_NAME}_${APP_FOLDER_NAME}_$day"
            }
        }

//        fileName = "AudioCutter_AudioCutter_lonely(2)"

        fileNameHash.forEach {
            textName += "$it,"
        }
        if (textName.contains(fileName)) {
            fileName = "$fileName(${getAlphaNumericString(random.nextInt(10))})"
        }
        return fileName
    }


    fun createValidFileName(name: String, typeFile: Folder): String {
        val random = Random()
        val fileNameHash = getAllFileName(typeFile)
        var fileName = ""
        var textName = ""

        fileNameHash.forEach {
            textName += "$it,"
        }
        fileName = if (textName.contains(name)) {
            "$name(${getAlphaNumericString(random.nextInt(10))})"
        } else {
            name
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
            val item = ItemAppShare(
                info.loadLabel(AudioFileManagerImpl.mContext.packageManager).toString(),
                info.loadIcon(AudioFileManagerImpl.mContext.packageManager),
                info.activityInfo.packageName
            )
            listAppShares.add(item)
        }
        return listAppShares
    }

    fun shareFileAudio(context: Context, audioFile: AudioFile): Boolean {
        return try {
            val intent = Intent()
            intent.action = Intent.ACTION_SEND
            intent.putExtra(Intent.EXTRA_STREAM, audioFile.uri)
            intent.type = "audio/*"
            context.startActivity(
                Intent.createChooser(
                    intent,
                    context.resources.getString(R.string.Choose_in_app_inten)
                )
            )
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
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

    fun convertToAudioFile(audioInfor: AudioInfor, modified: Long, uri:Uri): AudioFile {
        val file = File(audioInfor.filePath)
        return AudioFile(
            file,
            getName(file),
            audioInfor.size,
            audioInfor.bitRate,
            audioInfor.duration,
            uri ,
            getBitmapByPath(file.absolutePath),
            audioInfor.title,
            audioInfor.alBum,
            audioInfor.artist,
            modified,
            audioInfor.genre,
            audioInfor.format
        )
    }

}