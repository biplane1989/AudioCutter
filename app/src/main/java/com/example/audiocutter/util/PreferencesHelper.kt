package com.example.audiocutter.util

import android.content.Context
import android.content.SharedPreferences
import java.util.*

object PreferencesHelper {
    private val SHARED_PREFERENCES_NAME = "rington_cutter_pref"
    val FADE_IN_TIME = "fade_in_time"
    val FADE_OUT_TIME = "fade_out_time"
    val CONVERT_FORMAT = "convert_format"
    val CONVERT_VOLUME = "convert_volume"
    val IS_FIRST_TIME_TO_USED_APP = "IS_FIRST_TIME_TO_USED_APP"
    val APP_LANGUAGE = "APP_LANGUAGE"

    private lateinit var sharedPreferences: SharedPreferences
    fun start(appContext: Context) {
        sharedPreferences =
            appContext.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
    }


    fun putInt(key: String, value: Int) {
        sharedPreferences.edit().putInt(key, value).apply()
    }

    fun putLong(key: String, value: Long) {
        sharedPreferences.edit().putLong(key, value).apply()
    }

    fun putFloat(key: String, value: Float) {
        sharedPreferences.edit().putFloat(key, value).apply()
    }

    fun putString(key: String, value: String) {
        sharedPreferences.edit().putString(key, value).apply()
    }

    fun putBoolean(key: String, value: Boolean) {
        sharedPreferences.edit().putBoolean(key, value).apply()
    }

    fun getInt(key: String, defaultValue: Int): Int {
        return sharedPreferences.getInt(key, defaultValue)
    }

    fun getLong(key: String, defaultValue: Long): Long {
        return sharedPreferences.getLong(key, defaultValue)
    }

    fun getFloat(key: String, defaultValue: Float): Float {
        return sharedPreferences.getFloat(key, defaultValue)
    }

    fun getString(key: String, defaultValue: String): String {
        return sharedPreferences.getString(key, defaultValue).toString()
    }

    fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return sharedPreferences.getBoolean(key, defaultValue)
    }

    fun getStringSet(key: String): Set<String> {
        return sharedPreferences.getStringSet(key, HashSet())!!
    }

    fun putStringSet(key: String, value: Set<String>) {
        sharedPreferences.edit().putStringSet(key, value).apply()
    }

    fun setFirstTimeToUsedApp(value:Boolean){
        putBoolean(IS_FIRST_TIME_TO_USED_APP, value)
    }

    fun isFirstTimeToUsedApp():Boolean{
        return getBoolean(IS_FIRST_TIME_TO_USED_APP, false)
    }
}