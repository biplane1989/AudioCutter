package com.example.audiocutter.permissions

import android.Manifest
import android.app.AppOpsManager
import android.app.role.RoleManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Process
import android.provider.Settings
import android.text.TextUtils
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.audiocutter.base.BaseActivity
import com.example.audiocutter.util.PreferencesHelper


object PermissionUtil {
    private const val ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners"

    fun hasCameraPermission(context: Context): Boolean {
        return hasPermission(context, Manifest.permission.CAMERA);
    }

    fun hasPermission(context: Context, permission: String): Boolean {
        if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
            return false
        }
        return true
    }

    fun hasWriteSetting(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.System.canWrite(context)
        } else {
            return true
        }
    }
    fun hasNotificationListenerPermission(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            val pkgName: String = context.packageName
            val flat = Settings.Secure.getString(
                context.contentResolver,
                ENABLED_NOTIFICATION_LISTENERS
            )
            if (!TextUtils.isEmpty(flat)) {
                val names = flat.split(":".toRegex()).toTypedArray()
                for (i in names.indices) {
                    val cn = ComponentName.unflattenFromString(names[i])
                    if (cn != null) {
                        if (TextUtils.equals(pkgName, cn.packageName)) {
                            return true
                        }
                    }
                }
            }
        }
        return false
    }

    fun hasQueryAppInfoPermission(context: Context): Boolean {
        var appOps: AppOpsManager? = null
        var mode = -1
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
            if (appOps != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), context.packageName)
                }
            }
            mode == AppOpsManager.MODE_ALLOWED
        } else true
    }


    fun hasPermissions(context: Context, permissions: Array<String>): Boolean {
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

    fun clickedOnNeverAskAgain(activity: AppCompatActivity, permissions: Array<String>): Boolean {

        var neverAskAgain = false
        for (permission in permissions) {
            if (PreferencesHelper.getBoolean(permission, false)) {
                if (ActivityCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_DENIED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                        //denied
                    } else {
                        neverAskAgain = true
                        break
                    }
                }
            }
        }
        return neverAskAgain
    }

    fun requestPermission(activity: AppCompatActivity, permissions: Array<String>, REQUEST_PERMISSION: Int) {
        if (!hasPermissions(activity, permissions)) {
            permissions.forEach {
                PreferencesHelper.putBoolean(it, true);
            }

            ActivityCompat.requestPermissions(activity, permissions, REQUEST_PERMISSION)
        }
    }

    fun goToPermissionSettingScreen(activity: BaseActivity, requestCode: Int) {

        val intent = Intent()
        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        val uri: Uri = Uri.fromParts("package", activity.packageName, null)
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.data = uri
        activity.startActivityForResult(intent, requestCode)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun hasCallRolePermission(appContext: Context): Boolean {
        val roleManager = appContext.getSystemService(RoleManager::class.java)
        return roleManager.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)
    }


}