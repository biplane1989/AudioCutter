package com.example.audiocutter.permissions

import android.Manifest
import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.Toast
import androidx.lifecycle.Observer
import com.example.audiocutter.R
import com.example.audiocutter.util.PreferencesHelper

interface ContactPermissionRequest : PermissionRequest, Observer<AppPermission> {
    fun isPermissionGranted(): Boolean {
        return PermissionManager.getAppPermissionData()
            .hasReadContactPermission() && PermissionManager.getAppPermissionData()
            .hasWriteContactPermission()
    }

    override fun requestPermission() {

        if (!isPermissionGranted()) {
            val baseActivity = getPermissionActivity()!!
            if (PermissionUtil.clickedOnNeverAskAgain(
                    baseActivity,
                    arrayOf(
                        Manifest.permission.READ_CONTACTS,
                        Manifest.permission.WRITE_CONTACTS
                    )
                )
            ) {
                PreferencesHelper.putBoolean(
                    this@ContactPermissionRequest::class.java.name + "goToPermissionSettingScreen",
                    true
                )
                PendingCallFunction.guidePermissionToast?.cancel()
                PendingCallFunction.guidePermissionToast = Toast.makeText(
                    getPermissionActivity(),
                    "",
                    Toast.LENGTH_SHORT
                )
                PendingCallFunction.guidePermissionToast!!.setGravity(
                    Gravity.FILL_HORIZONTAL or Gravity.BOTTOM,
                    0,
                    0
                )
                val inflater: LayoutInflater =
                    baseActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                PendingCallFunction.guidePermissionToast!!.view = inflater.inflate(
                    R.layout.contact_guide_turn_on_permission_layout,
                    null
                )
                PendingCallFunction.guidePermissionToast!!.duration = Toast.LENGTH_LONG
                PendingCallFunction.guidePermissionToast!!.show()
                PermissionUtil.goToPermissionSettingScreen(
                    baseActivity,
                    CONTACT_REQUEST_CODE
                )

            } else {
                PermissionUtil.requestPermission(
                    baseActivity, arrayOf(
                        Manifest.permission.READ_CONTACTS,
                        Manifest.permission.WRITE_CONTACTS
                    ), CONTACT_REQUEST_CODE
                )
            }
        }
    }

    override fun onChanged(appPermission: AppPermission) {
        if (isPermissionGranted()) {

            if (PreferencesHelper.getBoolean(
                    this@ContactPermissionRequest::class.java.name + "goToPermissionSettingScreen",
                    false
                )
            ) {
                PreferencesHelper.putBoolean(
                    this@ContactPermissionRequest::class.java.name + "goToPermissionSettingScreen",
                    false
                )
                getPermissionActivity()?.let {
                    PermissionUtil.goToPermissionSettingScreen(
                        it,
                        CONTACT_REQUEST_CODE
                    )
                    it.finishActivity(CONTACT_REQUEST_CODE)
                }
            }
        }
    }
}