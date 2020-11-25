package com.example.audiocutter.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.audiocutter.database.entities.AppFlashEntity

@Dao
interface AppFlashDao {
    @Query("SELECT COUNT(*) > 0 FROM app_enabled_flash_table WHERE package_name=:pkgName")
    suspend fun exist(pkgName: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(appEnabledFlash: AppFlashEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(listAppEnabledFlash: List<AppFlashEntity>)

    @Query("SELECT * FROM app_enabled_flash_table")
    fun findAll(): List<AppFlashEntity>

    @Query("DELETE FROM app_enabled_flash_table WHERE package_name IN(:listPkgName)")
    suspend fun delete(listPkgName: List<String>)

    @Query("DELETE FROM app_enabled_flash_table")
    suspend fun clear()
}