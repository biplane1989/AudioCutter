package com.example.audiocutter.permissions

import android.Manifest
import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.Toast
import androidx.lifecycle.Observer
import com.example.audiocutter.R
import com.example.audiocutter.util.PreferencesHelper

interface ContactItemPermissionRequest : PermissionRequest, Observer<AppPermission> {

    fun isPermissionGranted(): Boolean {
        val appPermission = PermissionManager.getAppPermissionData()
        return appPermission.hasReadContactPermission() &&
                appPermission.hasWriteContactPermission() &&
                appPermission.hasStoragePermission()
    }

    override fun requestPermission() {
        super.requestPermission()
        if (!isPermissionGranted()) {
            val baseActivity = getPermissionActivity()!!
            if (PermissionUtil.clickedOnNeverAskAgain(
                    baseActivity,
                    arrayOf(
                        Manifest.permission.READ_CONTACTS,
                        Manifest.permission.WRITE_CONTACTS,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                )
            ) {
                PreferencesHelper.putBoolean(
                    this@ContactItemPermissionRequest::class.java.name + "goToPermissionSettingScreen",
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
                    R.layout.contact_guide_turn_on_permission_layout,
                    null
                )
                RequestingPermissionObject.guidePermissionToast!!.duration = Toast.LENGTH_LONG
                RequestingPermissionObject.guidePermissionToast!!.show()
                PermissionUtil.goToPermissionSettingScreen(
                    baseActivity,
                    CONTACT_REQUEST_CODE
                )

            } else {
                PermissionUtil.requestPermission(
                    baseActivity, arrayOf(
                        Manifest.permission.READ_CONTACTS,
                        Manifest.permission.WRITE_CONTACTS,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ), CONTACT_REQUEST_CODE
                )
            }
        }
    }

    override fun onChanged(appPermission: AppPermission) {
        if (isPermissionGranted()) {

            if (PreferencesHelper.getBoolean(
                    this@ContactItemPermissionRequest::class.java.name + "goToPermissionSettingScreen",
                    false
                )
            ) {
                PreferencesHelper.putBoolean(
                    this@ContactItemPermissionRequest::class.java.name + "goToPermissionSettingScreen",
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