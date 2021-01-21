package com.example.audiocutter.permissions

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.Toast

import androidx.lifecycle.Observer
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseActivity

interface WriteSettingPermissionRequest : PermissionRequest, Observer<AppPermission> {
    fun isPermissionGranted(): Boolean {

        return PermissionManager.getAppPermissionData().hasWriteSettingPermission()
    }

    override fun requestPermission() {
        super.requestPermission()
        if (!isPermissionGranted()) {
            val baseActivity = getPermissionActivity()!!
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                RequestingPermissionObject.guidePermissionToast?.cancel()
                RequestingPermissionObject.guidePermissionToast = Toast.makeText(getPermissionActivity(), "", Toast.LENGTH_SHORT)
                RequestingPermissionObject.guidePermissionToast!!.setGravity(Gravity.FILL_HORIZONTAL or Gravity.BOTTOM, 0, 0)
                val inflater: LayoutInflater = baseActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                RequestingPermissionObject.guidePermissionToast!!.view = inflater.inflate(R.layout.common_guide_turn_on_write_setting_permission, null)
                RequestingPermissionObject.guidePermissionToast!!.duration = Toast.LENGTH_LONG
                RequestingPermissionObject.guidePermissionToast!!.show()
                goToSetting(baseActivity)
            } else {
                PermissionUtil.requestPermission(baseActivity, arrayOf(Manifest.permission.WRITE_SETTINGS), WRITE_SETTINGS_REQUEST_CODE)
            }
        }
    }

    override fun onChanged(appPermission: AppPermission) {
        if (isPermissionGranted()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                getPermissionActivity()?.let {
                    goToSetting(it)
                    it.finishActivity(WRITE_SETTINGS_REQUEST_CODE)
                }
            }
        }
    }

    private fun goToSetting(baseActivity: BaseActivity) {
        val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
        intent.data = Uri.parse("package:" + baseActivity.getPackageName())
        baseActivity.startActivityForResult(intent, WRITE_SETTINGS_REQUEST_CODE)
    }
}