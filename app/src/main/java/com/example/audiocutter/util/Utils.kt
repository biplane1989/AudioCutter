package com.example.audiocutter.util

import android.app.Activity
import android.content.Context
import android.util.TypedValue
import android.view.View
import android.view.inputmethod.InputMethodManager

object Utils {

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


}