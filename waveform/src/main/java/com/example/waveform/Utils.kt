package com.example.waveform

import android.content.Context
import android.graphics.Paint
import android.graphics.Rect
import android.util.TypedValue

class Utils {
    companion object{
        fun dpToPx(context: Context, dp: Float): Float {
            return dp * context.resources.displayMetrics.density + 0.5f
        }


        fun pxToDp(context: Context, px: Int): Int {
            return (px / context.resources.displayMetrics.density).toInt()
        }

        fun spToPx(context: Context, sp: Float): Float {
            return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP,
                sp,
                context.resources.displayMetrics
            )
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
    }
}