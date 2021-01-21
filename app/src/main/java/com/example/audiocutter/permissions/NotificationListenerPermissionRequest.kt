package com.example.audiocutter.permissions

import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.Toast
import com.example.audiocutter.R

interface NotificationListenerPermissionRequest : PermissionRequest {
    private val ACTION_NOTIFICATION_LISTENER_SETTINGS: String
        get() = "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"

    fun isPermissionGranted(): Boolean {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            if (!PermissionManager.getAppPermissionData().hasNotificationListenerPermission()) {
                return false
            }
        }
        return true
    }

    override fun requestPermission() {
        super.requestPermission()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {

            if (!PermissionManager.getAppPermissionData().hasNotificationListenerPermission()) {
                val baseActivity = getPermissionActivity()!!
                baseActivity.startActivityForResult(
                    Intent(ACTION_NOTIFICATION_LISTENER_SETTINGS),
                    NOTIFICATION_LISTENER_REQUEST_CODE
                )
                RequestingPermissionObject.guidePermissionToast?.cancel()
                RequestingPermissionObject.guidePermissionToast = Toast.makeText(
                    getPermissionActivity(),
                    "",
                    Toast.LENGTH_SHORT
                )
                RequestingPermissionObject.guidePermissionToast!!.setGravity(
                    Gravity.FILL_HORIZONTAL or Gravity.BOTTOM,
                    0,
                    0
                )
                val inflater: LayoutInflater =
                    baseActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                RequestingPermissionObject.guidePermissionToast!!.view = inflater.inflate(
                    R.layout.locknotification_guide_turn_on_permission_layout,
                    null
                )
                RequestingPermissionObject.guidePermissionToast!!.duration = Toast.LENGTH_LONG
                RequestingPermissionObject.guidePermissionToast!!.show()


            }
        }

    }

    override fun onChanged(appPermission: AppPermission?) {
        appPermission?.apply {
            if (hasNotificationListenerPermission()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                    getPermissionActivity()?.finishActivity(NOTIFICATION_LISTENER_REQUEST_CODE)
                }

            }
        }
    }
}