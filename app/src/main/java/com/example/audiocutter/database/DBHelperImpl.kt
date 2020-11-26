package com.example.audiocutter.database

import com.example.audiocutter.core.flashcall.AppInfo
import com.example.audiocutter.core.manager.AppFlashItem
import com.example.audiocutter.database.entities.AppFlashEntity

fun List<AppFlashEntity>.toPkgNameSet(): HashSet<String> {
    val pkgNameSet = HashSet<String>()
    for (item in this) {
        pkgNameSet.add(item.packageName)
    }
    return pkgNameSet
}

fun List<AppFlashItem>.toListPkgName(): List<String> {
    val listPkgName = ArrayList<String>()
    for (item in this) {
        listPkgName.add(item.pkgName)
    }
    return listPkgName
}

fun AppFlashItem.toAppFlashEntity(): AppFlashEntity {
    return AppFlashEntity(this.pkgName)
}

class DBHelperImpl : DBHelper {

    override suspend fun saveAppEnabledFlash(listPkgNames: List<String>) {
        DatabaseHelper.get().appFlashDao.save(listPkgNames.map { AppFlashEntity(it) })
    }

    override suspend fun saveAppEnabledFlash(appFlashItem: AppFlashItem) {
        DatabaseHelper.get().appFlashDao.save(appFlashItem.toAppFlashEntity())
    }

    override suspend fun deleteAppEnabledFlash(listAppFlashItems: List<AppFlashItem>) {
        DatabaseHelper.get().appFlashDao.delete(listAppFlashItems.toListPkgName())
    }

    override suspend fun findAllAppEnabledFlash(): HashSet<String> {
        return DatabaseHelper.get().appFlashDao.findAll().toPkgNameSet()
    }

    override suspend fun clearAllAppEnabledFlash() {
        DatabaseHelper.get().appFlashDao.clear()
    }


}