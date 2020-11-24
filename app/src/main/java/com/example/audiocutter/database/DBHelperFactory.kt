package com.example.audiocutter.database

object DBHelperFactory {
    private val dbHelper = DBHelperImpl()
    fun getDBHelper(): DBHelper {
        return dbHelper
    }


}