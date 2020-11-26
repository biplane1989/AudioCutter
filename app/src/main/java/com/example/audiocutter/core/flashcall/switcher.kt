package com.example.audiocutter.core.flashcall

import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Camera
import android.hardware.camera2.CameraManager
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.audiocutter.core.manager.FlashType
import com.example.audiocutter.core.manager.LIGHTING_SPEED_DEFAULT
import com.example.audiocutter.core.manager.ManagerFactory
import com.example.audiocutter.core.manager.NUMBER_OF_FLASHES_DEFAULT
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel


private interface FlashSwitcher {
    fun turnOn()
    fun turnOff()
}

private class BelowAndroidMFlashSwitcher : FlashSwitcher {
    private var mCamera: Camera? = null

    override fun turnOn() {
        val isFlashAvailable: Boolean =
            ManagerFactory.getAppContext().getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)
        if (isFlashAvailable) {
            try {
                mCamera = Camera.open()
                val parameters = mCamera!!.getParameters()
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH)
                mCamera!!.parameters = parameters
                mCamera!!.startPreview()
            } catch (e: Exception) {
                mCamera?.stopPreview()
                mCamera?.release();
                mCamera = null

            }
        }

    }

    override fun turnOff() {
        try {
            mCamera?.stopPreview();
            mCamera?.release();
        } catch (e: Exception) {
            mCamera = null
        }
    }
}

@RequiresApi(Build.VERSION_CODES.M)
private class AndroidMFlashSwitcher : FlashSwitcher {
    private var cameraId: String = ""
    private val cameraManager: CameraManager

    init {
        val isFlashAvailable: Boolean =
            ManagerFactory.getAppContext().getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)
        cameraManager =
            ManagerFactory.getAppContext().getSystemService(Context.CAMERA_SERVICE) as CameraManager
        if (isFlashAvailable && cameraManager.cameraIdList.size > 0) {
            cameraId = cameraManager.cameraIdList[0]
        }
    }

    override fun turnOn() {
        if (!cameraId.isEmpty()) {
            cameraManager.setTorchMode(cameraId, true)
        }
    }

    override fun turnOff() {
        if (!cameraId.isEmpty()) {
            cameraManager.setTorchMode(cameraId, false)
        }
    }
}


class FlashPlayer {
    private var flashSwitcher: FlashSwitcher
    private var blinkingFlashJob: Job? = null
    private val flashPlayerScope = CoroutineScope(Dispatchers.Default)
    private val flashPlayerChannel = Channel<Boolean>(Channel.CONFLATED)
    private var flashTime: Int = NUMBER_OF_FLASHES_DEFAULT
    private var flashSpeed: Long = LIGHTING_SPEED_DEFAULT
    private var flashType = FlashType.BEAT
    private var isTestMode = false
    private var timeCounter = 0
    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flashSwitcher = AndroidMFlashSwitcher()
        } else {
            flashSwitcher = BelowAndroidMFlashSwitcher()
        }
        registerFlashPlayerChannel()
    }

    private fun registerFlashPlayerChannel() {
        flashPlayerScope.launch {
            while (isActive && true) {
                val isBlinkingFlash = flashPlayerChannel.receive()
                blinkingFlashJob?.cancelAndJoin()
                if (isBlinkingFlash) {
                    timeCounter = 0
                    while ((timeCounter < flashTime) || isTestMode) {
                        if (flashType == FlashType.CONTINUITY) {
                            flashSwitcher.turnOn()
                            delay(flashSpeed / 2)
                            flashSwitcher.turnOff()
                            delay(flashSpeed / 2)
                        } else {
                            for (i in 0..2) {
                                if (!isActive) {
                                    break
                                }
                                flashSwitcher.turnOn()
                                delay(50)
                                flashSwitcher.turnOff()
                                delay(50)
                            }
                            delay(flashSpeed)
                        }
                        if (timeCounter != Int.MAX_VALUE) {
                            timeCounter += 1
                        }

                    }
                } else {
                    blinkingFlashJob = flashPlayerScope.launch {
                        flashSwitcher.turnOff()
                    }
                }
            }
        }
    }

    suspend fun startBlinkingFlash(
        time: Int,
        speed: Long,
        flashType: FlashType,
        isTestMode: Boolean = false
    ) {
        flashTime = time
        flashSpeed = speed
        this.flashType = flashType
        this.isTestMode = isTestMode
        flashPlayerChannel.send(true)
    }

    suspend fun stopBlink() {
        isTestMode = false
        timeCounter = Int.MAX_VALUE
        flashPlayerChannel.send(false)
    }


}