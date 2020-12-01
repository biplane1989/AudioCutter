package com.example.audiocutter.core.manager

import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import java.util.*

const val LIGHTING_SPEED_DEFAULT = 350L
const val NUMBER_OF_FLASHES_DEFAULT = 2

data class FlashMode(
    var bellEnable: Boolean = true,
    var vibrateEnable: Boolean = false,
    var silentEnable: Boolean = true
)

data class FlashTimer(
    var enable: Boolean = false,
    var startHour: Int = 23,
    var startMinute: Int = 0,
    var endHour: Int = 7,
    var endMinute: Int = 0
) {
    fun isNowInRange(): Boolean {
        val now = Calendar.getInstance()
        now.time = Date(System.currentTimeMillis())
        val currHour = now.get(Calendar.HOUR_OF_DAY)
        val currMinute = now.get(Calendar.MINUTE)
        return startHour <= currHour && currHour <= endHour
                && startMinute <= currMinute && currMinute <= endMinute
    }
}

enum class FlashType {
    BEAT,
    CONTINUITY
}

data class FlashCallConfig(

    var enable: Boolean = false, var incomingCallEnable: Boolean = true, var notificationEnable: Boolean = false, var notFiredWhenInUsed: Boolean = true, var lightningSpeed: Long = LIGHTING_SPEED_DEFAULT, var numberOfLightning: Int = NUMBER_OF_FLASHES_DEFAULT, val flashMode: FlashMode = FlashMode(), val flashTimer: FlashTimer = FlashTimer(), var flashType: FlashType = FlashType.BEAT) {
    fun isLightingSpeedDefault(): Boolean {
        return lightningSpeed == LIGHTING_SPEED_DEFAULT
    }

    fun isNumberOfLightningDefault(): Boolean {
        return numberOfLightning == NUMBER_OF_FLASHES_DEFAULT
    }
}

data class AppFlashItem(val name: String, val pkgName: String, val icon: Bitmap?, var selected: Boolean)
data class ListAppFlashItemsResult(val isLoading: Boolean, val data: List<AppFlashItem>?)
interface FlashCallSetting {
    fun setup(appContext: Context)
    fun getListNotificationApps(): LiveData<ListAppFlashItemsResult>
    fun getFlashCallConfig(): LiveData<FlashCallConfig>
    fun startTestingLightningSpeed()
    fun stopTestingLightningSpeed()
    fun changeFlashCallConfig(flashCallConfig: FlashCallConfig)
    suspend fun enableNotificationFlash(appFlashItem: AppFlashItem)
    suspend fun disableNotificationFlash(appFlashItem: AppFlashItem)
    fun release()
}