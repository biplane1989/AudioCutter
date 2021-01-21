package com.example.audiocutter.permissions

import android.util.Log
import android.widget.Toast
import androidx.annotation.CallSuper
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.Observer
import androidx.lifecycle.OnLifecycleEvent
import com.example.audiocutter.base.BaseActivity
import kotlinx.coroutines.*

typealias PendingPermissionAction = () -> Unit

interface PermissionRequest : Observer<AppPermission> {
    val STORAGE_REQUEST_CODE: Int get() = 1
    val CONTACT_REQUEST_CODE: Int get() = 2
    val WRITE_SETTINGS_REQUEST_CODE: Int get() = 3
    val CALL_PHONE_REQUEST_CODE: Int get() = 4
    val NOTIFICATION_LISTENER_REQUEST_CODE: Int get() = 5

    fun getPermissionActivity(): BaseActivity?
    fun getLifeCycle(): Lifecycle

    @CallSuper
    fun requestPermission() {
        RequestingPermissionObject.isRequestingPermission = true
    }

    fun init() {
        getLifeCycle().addObserver(object : LifecycleObserver {
            @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
            private fun onCreated() {
                PermissionManager.getAppPermission()
                    .observeForever(this@PermissionRequest)
            }

            @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
            private fun onResume() {
                PermissionManager.checkChangingPermission()
                onViewResume()
                RequestingPermissionObject.isRequestingPermission = false
                RequestingPermissionObject.stopCheckingPermission()
            }

            @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
            private fun onPaused() {
                if (RequestingPermissionObject.isRequestingPermission) {
                    RequestingPermissionObject.startCheckingPermission()
                }

            }

            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            private fun onDestroy() {
                PermissionManager.getAppPermission()
                    .removeObserver(this@PermissionRequest)
            }
        })
    }

    fun onViewResume() {
        RequestingPermissionObject.guidePermissionToast?.cancel()
        RequestingPermissionObject.guidePermissionToast = null
    }
}

object RequestingPermissionObject {
    var isRequestingPermission = false
    var guidePermissionToast: Toast? = null
    private var checkPermissionJob: Job? = null
    private val permissionLifecycleScope = MainScope()
    fun startCheckingPermission() {
        checkPermissionJob?.cancel()
        checkPermissionJob = permissionLifecycleScope.launch {
            while (isActive) {
                delay(1000)
                PermissionManager.checkChangingPermission()
            }
        }
    }

    fun stopCheckingPermission() {
        checkPermissionJob?.cancel()
    }
}

