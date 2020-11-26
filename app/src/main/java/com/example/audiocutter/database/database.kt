package com.example.audiocutter.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.audiocutter.database.dao.AppFlashDao
import com.example.audiocutter.database.entities.AppFlashEntity

const val DATABASE_NAME = "audio_cutter_db"


@Database(
    entities = [AppFlashEntity::class],
    exportSchema = false,
    version = 1
)
abstract class DatabaseHelper : RoomDatabase() {
    companion object {
        @Volatile
        private var instance: DatabaseHelper? = null
        fun create(context: Context) {
            instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also {
                    instance = it
                }
            }
        }

        fun get(): DatabaseHelper {
            return instance!!
        }

        private fun buildDatabase(context: Context): DatabaseHelper {
            return Room.databaseBuilder(context, DatabaseHelper::class.java, DATABASE_NAME).build()
        }
    }

    abstract val appFlashDao: AppFlashDao

}