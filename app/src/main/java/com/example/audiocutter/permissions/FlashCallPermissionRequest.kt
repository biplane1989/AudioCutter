package com.example.audiocutter.permissions

import android.Manifest
import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.Toast
import androidx.lifecycle.Observer
import com.example.audiocutter.R
import com.example.audiocutter.util.PreferencesHelper


interface FlashCallPermissionRequest : PermissionRequest, Observer<AppPermission> {
    fun isPermissionGranted(): Boolean {
        return PermissionManager.hasFlashCallPermission()
    }

    override fun requestPermission() {
        super.requestPermission()
        if (!isPermissionGranted()) {
            val baseActivity = getPermissionActivity()
            baseActivity?.let {
                if (PermissionUtil.clickedOnNeverAskAgain(
                        baseActivity,
                        arrayOf(Manifest.permission.CALL_PHONE)
                    )
                ) {
                    PreferencesHelper.putBoolean(
                        this@FlashCallPermissionRequest::class.java.name + "goToPermissionSettingScreen",
                        true
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
                        R.layout.call_phone_guide_turn_on_permission_layout,
                        null
                    )
                    RequestingPermissionObject.guidePermissionToast!!.duration = Toast.LENGTH_LONG
                    RequestingPermissionObject.guidePermissionToast!!.show()
                    PermissionUtil.goToPermissionSettingScreen(
                        baseActivity,
                        CALL_PHONE_REQUEST_CODE
                    )
                } else {
                    PermissionUtil.requestPermission(
                        baseActivity, arrayOf(
                            Manifest.permission.READ_PHONE_STATE,
                            Manifest.permission.PROCESS_OUTGOING_CALLS
                        ), CALL_PHONE_REQUEST_CODE
                    )
                }

            }
        }
    }

    override fun onChanged(appPermission: AppPermission) {
        if (isPermissionGranted()) {
            RequestingPermissionObject.guidePermissionToast?.cancel()
            if (PreferencesHelper.getBoolean(
                    this@FlashCallPermissionRequest::class.java.name + "goToPermissionSettingScreen",
                    false
                )
            ) {
                PreferencesHelper.putBoolean(
                    this@FlashCallPermissionRequest::class.java.name + "goToPermissionSettingScreen",
                    false
                )
                getPermissionActivity()?.let {
                    PermissionUtil.goToPermissionSettingScreen(
                        it,
                        CALL_PHONE_REQUEST_CODE
                    )
                    it.finishActivity(CALL_PHONE_REQUEST_CODE)
                }
            }
        }
    }
}