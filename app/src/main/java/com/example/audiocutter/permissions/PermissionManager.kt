package com.example.audiocutter.permissions

import android.Manifest
import android.content.Context
import android.os.Build
import androidx.lifecycle.*
import com.example.audiocutter.util.PreferencesHelper
import com.google.gson.GsonBuilder
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList

data class PermissionInfo(val permissionName: String, var granted: Boolean)
class AppPermission {
    private val listPermissionInfos = ArrayList<PermissionInfo>()
    private val listPermissionNames = ArrayList<String>()
    private val NOTIFICATION_LISTENER_PERMISSION = "NOTIFICATION_LISTENER_PERMISSION"

    init {
        listPermissionNames.addAll(listOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS, Manifest.permission.WRITE_SETTINGS, Manifest.permission.READ_PHONE_STATE, Manifest.permission.PROCESS_OUTGOING_CALLS,
            NOTIFICATION_LISTENER_PERMISSION))

        listPermissionNames.forEach({ permissionName ->
            listPermissionInfos.add(PermissionInfo(permissionName, false))
        })
    }

    fun encode(): String {
        return GsonBuilder().create().toJson(this)
    }

    companion object {
        fun decode(encode: String): AppPermission {
            return GsonBuilder().create().fromJson(encode, AppPermission::class.java)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (other is AppPermission) {
            return other.listPermissionInfos == listPermissionInfos
        }
        return super.equals(other)
    }

    override fun hashCode(): Int {
        if (listPermissionInfos.size > 0) {
            val listHashCode = IntArray(listPermissionInfos.size)
            for (i in 0..listPermissionInfos.size) {
                listHashCode[i] = listPermissionInfos.get(i).hashCode()
            }
            return Arrays.hashCode(listHashCode)
        }
        return super.hashCode()
    }

    private fun getPermissionInfo(permissionName: String): PermissionInfo? {

        for (permissionInfo in listPermissionInfos) {
            if (permissionInfo.permissionName.equals(permissionName)) {
                return permissionInfo
            }
        }
        return null
    }

    fun hasStoragePermission(): Boolean {
        return getPermissionInfo(Manifest.permission.WRITE_EXTERNAL_STORAGE)?.granted ?: false
    }
    fun hasNotificationListenerPermission(): Boolean {
        return getPermissionInfo(NOTIFICATION_LISTENER_PERMISSION)?.granted ?: false
    }

    fun hasReadContactPermission(): Boolean {
        return getPermissionInfo(Manifest.permission.READ_CONTACTS)?.granted ?: false
    }

    fun hasWriteContactPermission(): Boolean {
        return getPermissionInfo(Manifest.permission.WRITE_CONTACTS)?.granted ?: false
    }

    fun hasWriteSettingPermission(): Boolean {
        return getPermissionInfo(Manifest.permission.WRITE_SETTINGS)?.granted ?: false
    }

    fun hasProcessOutGoingCallsPermission():Boolean{
        return getPermissionInfo(Manifest.permission.PROCESS_OUTGOING_CALLS)?.granted ?: false
    }
    fun hasReadPhoneStatePermission():Boolean{
        return getPermissionInfo(Manifest.permission.READ_PHONE_STATE)?.granted ?: false
    }
    fun isPermissionChanged(context: Context): Boolean {
        for (permissionInfo in listPermissionInfos) {
            when (permissionInfo.permissionName) {
                Manifest.permission.WRITE_SETTINGS -> {
                    if (permissionInfo.granted != PermissionUtil.hasWriteSetting(context)) {
                        return true
                    }
                }
                NOTIFICATION_LISTENER_PERMISSION  -> {
                    if (permissionInfo.granted != PermissionUtil.hasNotificationListenerPermission(
                            context
                        )
                    ) {
                        return true
                    }
                }
                else -> {
                    if (permissionInfo.granted != PermissionUtil.hasPermission(context, permissionInfo.permissionName)) {
                        return true
                    }
                }
            }
        }
        return false;
    }

    fun updatePermission(context: Context) {
        for (permissionInfo in listPermissionInfos) {
            when (permissionInfo.permissionName) {
                Manifest.permission.WRITE_SETTINGS -> {
                    permissionInfo.granted = PermissionUtil.hasWriteSetting(context)
                }
                NOTIFICATION_LISTENER_PERMISSION -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                        permissionInfo.granted =
                            PermissionUtil.hasNotificationListenerPermission(context)
                    }
                }
                else -> {
                    permissionInfo.granted = PermissionUtil.hasPermission(context, permissionInfo.permissionName)
                }
            }
        }
    }


}

object PermissionManager {
    private val APP_PERMISSION_PREFERENCE_KEY = "APP_PERMISSION_PREFERENCE_KEY"
    private lateinit var appContext: Context
    private val appPermissionLiveData = MutableLiveData<AppPermission>()
    private lateinit var appPermission: AppPermission

    fun start(appContext: Context) {
        this.appContext = appContext
        init()
        /*mainScope.launch {
            while (true) {
                delay(1000)
                if (appPermission.isPermissionChanged(PermissionManager.appContext)) {
                    appPermission.updatePermission(PermissionManager.appContext)
                    PreferencesHelper.putString(APP_PERMISSION_PREFERENCE_KEY, appPermission.encode())
                    appPermissionLiveData.postValue(appPermission)
                }
            }
        }*/
    }
    fun checkChangingPermission(){
        if (appPermission.isPermissionChanged(PermissionManager.appContext)) {
            appPermission.updatePermission(PermissionManager.appContext)
            PreferencesHelper.putString(APP_PERMISSION_PREFERENCE_KEY, appPermission.encode())
            appPermissionLiveData.postValue(appPermission)
        }
    }

    fun getAppPermission(): LiveData<AppPermission> {
        return appPermissionLiveData
    }

    fun getAppPermissionData(): AppPermission {
        return appPermission
    }

    private fun init() {
        val appPermissionEncode = PreferencesHelper.getString(APP_PERMISSION_PREFERENCE_KEY, "")
        if (!appPermissionEncode.isEmpty()) {
            appPermission = AppPermission.decode(appPermissionEncode)
        } else {
            appPermission = AppPermission()
            appPermission.updatePermission(appContext)
        }
        if (appPermission.isPermissionChanged(appContext)) {
            appPermission.updatePermission(appContext)
            PreferencesHelper.putString(APP_PERMISSION_PREFERENCE_KEY, appPermission.encode())

        }
        appPermissionLiveData.postValue(appPermission)
    }


    fun hasStoragePermission(): Boolean {
        return appPermission.hasStoragePermission()
    }
    fun hasFlashCallPermission(): Boolean {
        return appPermission.hasProcessOutGoingCallsPermission() && appPermission.hasReadPhoneStatePermission()
    }



}