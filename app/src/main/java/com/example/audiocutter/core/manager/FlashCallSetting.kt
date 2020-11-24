package com.example.audiocutter.core.manager

import android.graphics.Bitmap
import androidx.lifecycle.LiveData

const val LIGHTING_SPEED_DEFAULT = 350L
const val NUMBER_OF_FLASHES_DEFAULT = 2

data class FlashMode(val bellEnable: Boolean, val vibrateEnable: Boolean, val silentEnable: Boolean)
data class FlashTimer(val enable: Boolean, val startTime: Long, val endTime: Long)
data class FlashCallConfig(

    val enable: Boolean,
    val incomingCallEnable: Boolean,
    val notificationEnable: Boolean,
    val notFiredWhenInUsed: Boolean,
    val lightningSpeed: Long,
    val numberOfLightning: Int,
    val flashMode: FlashMode,
    val flashTimer: FlashTimer
) {
    fun isLightingSpeedDefault(): Boolean {
        return lightningSpeed == LIGHTING_SPEED_DEFAULT
    }

    fun isNumberOfLightningDefault(): Boolean {
        return numberOfLightning == NUMBER_OF_FLASHES_DEFAULT
    }
}

class AppFlashItem(val name: String, val pkgName: String, val icon: Bitmap?, val selected: Boolean)
data class ListAppFlashItemsResult(val isLoading: Boolean, val data: List<AppFlashItem>)
interface FlashCallSetting {
    fun getListNotificationApps(): LiveData<ListAppFlashItemsResult>
    fun getFlashCallConfig(): LiveData<FlashCallConfig>
    fun testLightningSpeed()
    fun stopTestingLightningSpeed()
    fun changeFlashCallConfig(flashCallConfig: FlashCallConfig)
    suspend fun enableNotificationFlash(appFlashItem: AppFlashItem)
    suspend fun disableNotificationFlash(appFlashItem: AppFlashItem)
}