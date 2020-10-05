package com.example.audiocutter

import android.app.Application
import com.example.a0025antivirusapplockclean.permissions.PermissionManager
import com.example.audiocutter.core.ManagerFactory
import com.example.audiocutter.util.PreferencesHelper

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        PreferencesHelper.start(applicationContext)
        ManagerFactory.init(applicationContext)
        PermissionManager.start(applicationContext)
    }
}