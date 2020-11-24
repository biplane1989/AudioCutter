package com.example.audiocutter.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity(tableName = "app_enabled_flash_table")
data class AppFlashEntity(@PrimaryKey @ColumnInfo(name = "package_name") val packageName: String)