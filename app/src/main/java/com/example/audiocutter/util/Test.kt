package com.example.audiocutter.util

import androidx.fragment.app.Fragment
import com.example.audiocutter.base.BaseFragment
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.requestPermission
import java.util.jar.Manifest

const val REQUEST_CODE = 123
const val REQUEST_CODE_2 = 124

val permissions = listOf(android.Manifest.permission.READ_CONTACTS, android.Manifest.permission.READ_EXTERNAL_STORAGE)


class Test : BaseFragment(){


    init {
        requestPermission(
            REQUEST_CODE, *permissions.toTypedArray()
        )

        requestPermission(
            REQUEST_CODE_2, android.Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    @AfterPermissionGranted(REQUEST_CODE)
    fun openFunction(){
        // do some thing
    }

    @AfterPermissionGranted(REQUEST_CODE)
    fun querySDCard(){

    }

}