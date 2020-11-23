package com.example.audiocutter.activities.acttest.testnm

import android.content.Context
import android.hardware.camera2.CameraManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object UtilsTest {


    private var mCameraId: String? = ""
    private lateinit var mCameraManager: CameraManager

    @RequiresApi(Build.VERSION_CODES.M)
    fun startFlash(mContext: Context, text: String) {
        Toast.makeText(mContext, "this is button", Toast.LENGTH_SHORT).show()

        Log.i("start runtime", "start")
        mCameraManager = mContext.getSystemService(AppCompatActivity.CAMERA_SERVICE) as CameraManager
        mCameraId = mCameraManager.cameraIdList[0]

        GlobalScope.launch {
            while (true) {
                for (element in text) {

                    if (element == '0') {
                        mCameraManager.setTorchMode(mCameraId!!, true)
                    } else {
                        mCameraManager.setTorchMode(mCameraId!!, false)
                    }
                    try {
                        delay(100)
                    } catch (e: Exception) {
                        Log.i("exceptionException", "This is an blinking exception")
                        e.printStackTrace()
                    }
                }

            }

        }


    }


    fun turnOff() {
        mCameraManager.setTorchMode(mCameraId!!, false)
    }
}