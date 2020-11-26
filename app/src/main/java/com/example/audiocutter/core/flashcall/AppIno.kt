package com.example.audiocutter.core.flashcall

import android.graphics.Bitmap
import android.graphics.drawable.Drawable

data class AppInfo(
    val pkgName: String,
    val name: String,
    var icon: Drawable? = null,
    var bmIcon: Bitmap? = null,
    var isSystem: Boolean = false
)