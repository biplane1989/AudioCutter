package com.example.audiocutter.database

import com.example.audiocutter.core.manager.AppFlashItem

interface DBHelper {
    suspend fun saveAppEnabledFlash(appFlashItem: AppFlashItem)
    suspend fun saveAppEnabledFlash(listPkgNames:List<String>)
    suspend fun deleteAppEnabledFlash(listAppFlashItems: List<AppFlashItem>)
    suspend fun findAllAppEnabledFlash(): HashSet<String>
    suspend fun clearAllAppEnabledFlash()
}