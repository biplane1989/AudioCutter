package com.example.audiocutter.util

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
}