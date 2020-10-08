package com.example.audiocutter.util

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.util.TypedValue
import android.view.View
import java.io.File
import kotlin.math.max

object Utils {
    val KEY_SEND_PATH = "key_send_path"
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
        return minutes.toString() + ":" + ((if (oddSeconds > 9) oddSeconds else "0$oddSeconds").toString() + if (oddMsTrimmed != 0L) ".$oddMsTrimmed" else "")
    }

    fun getWidthText(str: String = "00:00:00", context: Context): Float {
        val paint = Paint()
        paint.textSize =
            spToPx(context, 12f)
        val result = Rect()
        paint.getTextBounds(str, 0, str.length, result)
        return result.width().toFloat()
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

}