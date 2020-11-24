package com.example.audiocutter.core.flashcall

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.example.audiocutter.core.manager.*
import com.example.audiocutter.database.DBHelperFactory
import com.example.audiocutter.util.PreferencesHelper
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel

class FlashCallSettingImpl : FlashCallSetting {
    companion object {
        private const val INCOMING_CALL_KEY = "INCOMING_CALL_KEY"
        private const val NOTIFICATIONS_FOR_APPS_KEY = "NOTIFICATIONS_FOR_APPS_KEY"
        private const val FLASH_IS_NOT_FIRED_WHEN_IN_USE_KEY = "FLASH_IS_NOT_FIRED_WHEN_IN_USE_KEY"
        private const val LIGHTNING_SPEED_KEY = "LIGHTNING_SPEED_KEY"
        private const val NUMBER_OF_FLASHES_WHEN_NOTIFIED_KEY =
            "NUMBER_OF_FLASHES_WHEN_NOTIFIED_KEY"

        private const val FLASH_MODE_BELL_KEY = "FLASH_MODE_BELL_KEY"
        private const val FLASH_MODE_VIBRATE_KEY = "FLASH_MODE_VIBRATE_KEY"
        private const val FLASH_MODE_SILENT_KEY = "FLASH_MODE_SILENT_KEY"
        private const val FLASH_TIMER_ENABLE_KEY = "FLASH_TIMER_ENABLE_KEY"
        private const val FLASH_TIMER_START_TIME_KEY = "FLASH_TIMER_START_TIME_KEY"
        private const val FLASH_TIMER_END_TIME_KEY = "FLASH_TIMER_END_TIME_KEY"
        private const val FLASH_CALL_SETTING_ENABLE_KEY = "FLASH_CALL_SETTING_ENABLE_KEY"


    }

    private val appManager = AppManager()
    private val flashCallSettingScope = CoroutineScope(Dispatchers.Default)
    private val listAppFlashItems = MediatorLiveData<ListAppFlashItemsResult>()
    private val syncListItemsChannel = Channel<Any>(Channel.CONFLATED)
    private var syncListItemsJob: Job? = null
    private val flashCallConfig = MutableLiveData<FlashCallConfig>(restoreFlashCallConfig())
    private val flashPlayer = FlashPlayer()

    init {
        listAppFlashItems.addSource(appManager.getListAppInfo()) {
            if (it.isLoading) {
                postLoadingListData()
            } else {
                notifySyncListItem()
            }

        }
        registerSyncListItemsChannel()

    }

    private fun restoreFlashCallConfig(): FlashCallConfig {
        val flashCallSettingEnable =
            PreferencesHelper.getBoolean(FLASH_CALL_SETTING_ENABLE_KEY, false)
        val incomingCallEnable = PreferencesHelper.getBoolean(INCOMING_CALL_KEY, true)
        val notificationEnable = PreferencesHelper.getBoolean(NOTIFICATIONS_FOR_APPS_KEY, false)
        val notFiredWhenInUsed =
            PreferencesHelper.getBoolean(FLASH_IS_NOT_FIRED_WHEN_IN_USE_KEY, true)
        val lightningSpeed = PreferencesHelper.getLong(LIGHTNING_SPEED_KEY, LIGHTING_SPEED_DEFAULT)
        val numberOfLightning =
            PreferencesHelper.getInt(NUMBER_OF_FLASHES_WHEN_NOTIFIED_KEY, NUMBER_OF_FLASHES_DEFAULT)
        val bellEnable = PreferencesHelper.getBoolean(FLASH_MODE_BELL_KEY, true)
        val vibrateEnable = PreferencesHelper.getBoolean(FLASH_MODE_VIBRATE_KEY, false)
        val silentEnable = PreferencesHelper.getBoolean(FLASH_MODE_SILENT_KEY, true)
        val flashTimerEnable = PreferencesHelper.getBoolean(FLASH_TIMER_ENABLE_KEY, false)
        val startTime = PreferencesHelper.getLong(FLASH_TIMER_START_TIME_KEY, -1)
        val endTime = PreferencesHelper.getLong(FLASH_TIMER_END_TIME_KEY, -1)
        return FlashCallConfig(
            flashCallSettingEnable,
            incomingCallEnable,
            notificationEnable,
            notFiredWhenInUsed,
            lightningSpeed,
            numberOfLightning,
            FlashMode(bellEnable, vibrateEnable, silentEnable),
            FlashTimer(flashTimerEnable, startTime, endTime)
        )
    }

    private fun saveFlashCallConfig(flashCallConfig: FlashCallConfig) {
        PreferencesHelper.putBoolean(FLASH_CALL_SETTING_ENABLE_KEY, flashCallConfig.enable)
        PreferencesHelper.putBoolean(INCOMING_CALL_KEY, flashCallConfig.incomingCallEnable)
        PreferencesHelper.putBoolean(NOTIFICATIONS_FOR_APPS_KEY, flashCallConfig.notificationEnable)
        PreferencesHelper.putBoolean(
            FLASH_IS_NOT_FIRED_WHEN_IN_USE_KEY,
            flashCallConfig.notFiredWhenInUsed
        )
        PreferencesHelper.putLong(LIGHTNING_SPEED_KEY, flashCallConfig.lightningSpeed)
        PreferencesHelper.putInt(
            NUMBER_OF_FLASHES_WHEN_NOTIFIED_KEY,
            flashCallConfig.numberOfLightning
        )
        PreferencesHelper.putBoolean(FLASH_MODE_BELL_KEY, flashCallConfig.flashMode.bellEnable)
        PreferencesHelper.putBoolean(
            FLASH_MODE_VIBRATE_KEY,
            flashCallConfig.flashMode.vibrateEnable
        )
        PreferencesHelper.putBoolean(FLASH_MODE_SILENT_KEY, flashCallConfig.flashMode.silentEnable)
        PreferencesHelper.putBoolean(FLASH_TIMER_ENABLE_KEY, flashCallConfig.flashTimer.enable)
        PreferencesHelper.putLong(FLASH_TIMER_START_TIME_KEY, flashCallConfig.flashTimer.startTime)
        PreferencesHelper.putLong(FLASH_TIMER_END_TIME_KEY, flashCallConfig.flashTimer.endTime)
    }

    private fun postLoadingListData() {
        val listAppInfoResult = listAppFlashItems.value
        if (listAppInfoResult == null || !listAppInfoResult.isLoading) {
            listAppFlashItems.postValue(ListAppFlashItemsResult(true, ArrayList()))
        }
    }

    private fun registerSyncListItemsChannel() {
        flashCallSettingScope.launch {
            while (true) {
                val signal = syncListItemsChannel.receive()
                syncListItemsJob?.cancelAndJoin()
                syncListItemsJob = flashCallSettingScope.launch {
                    syncListItem()
                }
            }
        }
    }

    private fun newAppFlashItem(appInfo: AppInfo, selected: Boolean): AppFlashItem {
        return AppFlashItem(appInfo.name, appInfo.pkgName, appInfo.bmIcon, selected)
    }

    private suspend fun syncListItem() = coroutineScope {
        val oldPkgNameSet = DBHelperFactory.getDBHelper().findAllAppEnabledFlash()
        val listInstalledApps = appManager.getListAppInfoData()
        val newPkgNameSet = HashSet<String>()
        val data = ArrayList<AppFlashItem>()
        listInstalledApps.forEach {
            val appFlashItem = newAppFlashItem(it, oldPkgNameSet.contains(it.pkgName))
            if (appFlashItem.selected) {
                newPkgNameSet.add(it.pkgName)
            }
            data.add(appFlashItem)
        }
        if (oldPkgNameSet.intersect(newPkgNameSet).size != oldPkgNameSet.size) {
            DBHelperFactory.getDBHelper().clearAllAppEnabledFlash()
            DBHelperFactory.getDBHelper().saveAppEnabledFlash(newPkgNameSet.toList())
        }
        listAppFlashItems.postValue(ListAppFlashItemsResult(true, data))
    }

    override suspend fun enableNotificationFlash(appFlashItem: AppFlashItem) {
        DBHelperFactory.getDBHelper().saveAppEnabledFlash(appFlashItem)
        notifySyncListItem()
    }

    override suspend fun disableNotificationFlash(appFlashItem: AppFlashItem) {
        DBHelperFactory.getDBHelper().deleteAppEnabledFlash(arrayListOf(appFlashItem))
        notifySyncListItem()
    }

    private fun notifySyncListItem() {
        flashCallSettingScope.launch {
            syncListItemsChannel.send(true)
        }
    }

    override fun getListNotificationApps(): LiveData<ListAppFlashItemsResult> {
        return listAppFlashItems
    }

    override fun getFlashCallConfig(): LiveData<FlashCallConfig> {
        return flashCallConfig
    }

    override fun testLightningSpeed() {
        this.flashCallConfig.value?.let {
            flashCallSettingScope.launch {
                flashPlayer.startBlinkingFlash(it.numberOfLightning, it.lightningSpeed)
            }

        }

    }

    override fun stopTestingLightningSpeed() {
        flashCallSettingScope.launch {
            flashPlayer.stopBlink()
        }
    }

    override fun changeFlashCallConfig(flashCallConfig: FlashCallConfig) {
        if (this.flashCallConfig.value != flashCallConfig) {
            saveFlashCallConfig(flashCallConfig)
            this.flashCallConfig.postValue(restoreFlashCallConfig())
        }
    }


}