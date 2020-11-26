package com.example.audiocutter

import org.junit.Assert
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        val major = 1
        val minor = 1

        val longVersion = major.toLong() shl 32 or (minor.toLong() and 0xffffffffL)
        print(longVersion)
        val decodeMajor = (longVersion shr 32)
        val decodeMinor = longVersion.toInt()

        Assert.assertEquals(1, decodeMajor)
        Assert.assertEquals(1, decodeMinor)

    }
}