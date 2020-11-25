package com.example.audiocutter.core.flashcall

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.audiocutter.core.audiomanager.AudioFileManagerImpl
import com.example.audiocutter.core.manager.ManagerFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.reflect.Method
import java.util.concurrent.locks.ReentrantLock

data class ListAppInfoResult(val isLoading: Boolean, val data: List<AppInfo>)

class AppManager {
    private val listAppInfoData = ArrayList<AppInfo>()
    private val listAppInfo = MutableLiveData<ListAppInfoResult>()
    private val pkgHashSet = HashSet<String>()
    private val appManagerScope = CoroutineScope(Dispatchers.Default)
    private val lock = ReentrantLock()


    private val installOrUninstallAppReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {

            var pkgName: String? = null
            var actionName: String? = null
            pkgName = intent?.data?.encodedSchemeSpecificPart
            actionName = intent?.action
            if (pkgName != null) {
                if (Intent.ACTION_PACKAGE_ADDED.equals(actionName)) {
                    runBackgroundAndLock {
                        if (!pkgHashSet.contains(pkgName)) {
                            addAppInfo(pkgName)
                            postDoneGetListData()
                        }
                    }
                }
                if (Intent.ACTION_PACKAGE_REMOVED.equals(actionName)) {
                    runBackgroundAndLock {
                        if (pkgHashSet.contains(pkgName)) {
                            removeAppInfo(pkgName)
                            postDoneGetListData()
                        }
                    }

                }
            }
        }
    }

    init {

        runBackgroundAndLock {
            postLoadingListData()
            queryAllInstalledApps()
            registerInstallOrUninstallAppEvent()

        }
    }

    fun getListAppInfo(): LiveData<ListAppInfoResult> {
        return listAppInfo
    }

    fun getListAppInfoData(): List<AppInfo> {
        val tmp = ArrayList<AppInfo>()
        withLock {
            tmp.addAll(listAppInfoData)
        }
        return tmp

    }

    private fun postDoneGetListData() {
        listAppInfo.postValue(ListAppInfoResult(false, listAppInfoData))
    }

    private fun postLoadingListData() {
        val listAppInfoResult = listAppInfo.value
        if (listAppInfoResult == null || !listAppInfoResult.isLoading) {
            listAppInfo.postValue(ListAppInfoResult(true, ArrayList()))
        }
    }


    private fun registerInstallOrUninstallAppEvent() {
        val intentFilter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addDataScheme("package")
        }
        ManagerFactory.getAppContext().registerReceiver(installOrUninstallAppReceiver, intentFilter)
    }

    private fun queryAllInstalledApps() {

        val packageManager = ManagerFactory.getAppContext().packageManager
        val intent = Intent("android.intent.action.MAIN", null)
        intent.addCategory("android.intent.category.LAUNCHER")
        val listResolveInfos = packageManager.queryIntentActivities(intent, 0)
        withLock {
            listAppInfoData.clear()
            pkgHashSet.clear()
            for (resolveInfo in listResolveInfos) {
                if (!pkgHashSet.contains(resolveInfo.activityInfo.packageName)) {
                    addAppInfo(resolveInfo.activityInfo.packageName)
                }
            }
        }

        postDoneGetListData()
    }


    private fun addAppInfo(pkgName: String) {
        val appInfo = buildAppInfo(pkgName)
        synchronized(listAppInfoData) {
            listAppInfoData.add(appInfo)
            pkgHashSet.add(appInfo.pkgName)

        }
    }

    private fun removeAppInfo(pkgName: String) {
        val listAppMatched = ArrayList<AppInfo>()
        synchronized(listAppInfoData) {
            val listIterator = listAppInfoData.iterator()
            while (listIterator.hasNext()) {
                val appInfo = listIterator.next()
                if (appInfo.pkgName == pkgName) {
                    listAppMatched.add(appInfo)
                }
            }
            if (listAppMatched.size > 0) {

                for (appInfo in listAppMatched) {
                    listAppInfoData.remove(appInfo)
                    pkgHashSet.remove(appInfo.pkgName)
                }

            }
        }
    }

    private fun getAppName(pkgName: String): String {
        val packageManager = ManagerFactory.getAppContext().packageManager
        var appInfo: ApplicationInfo? = null
        try {
            appInfo = packageManager.getApplicationInfo(pkgName, 0)
        } catch (e: PackageManager.NameNotFoundException) {

        }
        try {
            if (appInfo != null) {
                return packageManager.getApplicationLabel(appInfo).toString()
            }
        } catch (e: SecurityException) {

        }
        return "Unknown"
    }

    private fun buildAppInfo(pkgName: String): AppInfo {
        val appInfo = AppInfo(pkgName, getAppName(pkgName))
        try {
            appInfo.icon =
                ManagerFactory.getAppContext().packageManager.getApplicationIcon(appInfo.pkgName)

            if (appInfo.icon != null) {
                appInfo.bmIcon = appInfo.icon!!.toBitmap()
            }
        } catch (e: PackageManager.NameNotFoundException) {

        }
        appInfo.isSystem = isSystemApp(ManagerFactory.getAppContext(), appInfo.pkgName)
        return appInfo
    }

    private fun isSystemApp(context: Context, pkgName: String): Boolean {
        val pm = context.packageManager
        return try {
            val applicationInfo = pm.getApplicationInfo(pkgName, PackageManager.GET_META_DATA)
            return isSystemApp(applicationInfo)
        } catch (e: PackageManager.NameNotFoundException) {
            true
        }
    }

    private fun isSystemApp(applicationInfo: ApplicationInfo?): Boolean {
        if (Build.DEVICE.contains("huawei") || Build.DEVICE.contains("HUAWEI")) {
            if (applicationInfo == null) {
                return true
            }
            var isSystem = false
            try {
                applicationInfo.javaClass.methods.forEach { method ->
                    method.isAccessible = true
                    if (method.returnType == String::class) {
                        try {
                            Log.d(
                                "libDevice",
                                method.name.toString() + ":" + method.invoke(
                                    applicationInfo,
                                    arrayOfNulls<Any>(0)
                                ) as String
                            )
                        } catch (e: java.lang.Exception) {
                            e.printStackTrace()
                            Log.d("libDevice", "e:" + e.message)
                        }
                    }
                }

                val method2: Method = applicationInfo.javaClass.getMethod(
                    "getResourcePath",
                    *arrayOfNulls<Class<*>>(0)
                )

                try {
                    method2.isAccessible = true
                    val str = method2.invoke(applicationInfo, arrayOfNulls<Any>(0)) as String
                    if (str.startsWith("/system")) {
                        isSystem = true
                    } else {
                        if (str.startsWith("/data")) {
                            isSystem = false
                        }
                        isSystem = true
                    }
                } catch (e2: java.lang.Exception) {
                    e2.printStackTrace()
                    Log.d("libDevice", "err:" + e2.message + " cause:" + e2.cause)
                }
                return isSystem

            } catch (e: NoSuchMethodException) {
                e.printStackTrace()
                return true
            }
        } else {
            if (applicationInfo == null) {
                return true
            }
            return (applicationInfo.flags and 1) > 0
        }
    }

    private inline fun withLock(func: () -> Unit) {
        lock.lock()
        try {
            func()
        } finally {
            lock.unlock()
        }
    }

    private inline fun runBackgroundAndLock(crossinline func: () -> Unit) {
        appManagerScope.launch {
            lock.lock()
            try {
                func()
            } finally {
                lock.unlock()
            }
        }

    }

}