package com.example.audiocutter.util

import java.text.Normalizer

object Utils {

    // loai bo ky tu chuyen ve dang aphalbet
    fun stripAccents(str: String): String {
        var newStr = Normalizer.normalize(str, Normalizer.Form.NFD)
        newStr = newStr.replace("[\\p{InCombiningDiacriticalMarks}]".toRegex(), "")
        return newStr
    }
}