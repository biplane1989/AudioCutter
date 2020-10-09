package com.example.audiocutter.permissions
import android.Manifest
import android.app.role.RoleManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
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


    init {
        listPermissionNames.addAll(
            listOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.WRITE_CONTACTS

            )
        )

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


    fun hasReadContactPermission(): Boolean {
        return getPermissionInfo(Manifest.permission.READ_CONTACTS)?.granted ?: false
    }

    fun hasWriteContactPermission(): Boolean {
        return getPermissionInfo(Manifest.permission.WRITE_CONTACTS)?.granted ?: false
    }
    fun isPermissionChanged(context: Context): Boolean {
        for (permissionInfo in listPermissionInfos) {
            when (permissionInfo.permissionName) {
                else -> {
                    if (permissionInfo.granted != PermissionUtil.hasPermission(
                            context,
                            permissionInfo.permissionName
                        )
                    ) {
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
                else -> {
                    permissionInfo.granted =
                        PermissionUtil.hasPermission(context, permissionInfo.permissionName)
                }
            }
        }
    }


}

object PermissionManager {
    private val APP_PERMISSION_PREFERENCE_KEY = "APP_PERMISSION_PREFERENCE_KEY"
    private lateinit var appContext: Context
    private val mainScope = MainScope()
    private val appPermissionLiveData = MutableLiveData<AppPermission>()
    private lateinit var appPermission: AppPermission

    fun start(appContext: Context) {
        this.appContext = appContext
        init()
        mainScope.launch {
            while (true) {
                delay(1000)
                if (appPermission.isPermissionChanged(PermissionManager.appContext)) {
                    appPermission.updatePermission(PermissionManager.appContext)
                    PreferencesHelper.putString(
                        APP_PERMISSION_PREFERENCE_KEY,
                        appPermission.encode()
                    )
                    appPermissionLiveData.postValue(appPermission)
                }
            }
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


}