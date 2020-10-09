package com.example.audiocutter.permissions
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.Observer
import androidx.lifecycle.OnLifecycleEvent
import com.example.audiocutter.base.BaseActivity

typealias PendingPermissionAction = () -> Unit

interface PermissionRequest : Observer<AppPermission> {
    val STORAGE_REQUEST_CODE: Int get() = 1
    fun getPermissionActivity(): BaseActivity?
    fun getLifeCycle(): Lifecycle
    fun requestPermission()
    fun init() {

        getLifeCycle().addObserver(object : LifecycleObserver {
            @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
            private fun onCreated() {
                PermissionManager.getAppPermission()
                    .observeForever(this@PermissionRequest)
            }

            @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
            private fun onResume() {
                onViewResume()
            }

            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            private fun onDestroy() {
                PermissionManager.getAppPermission()
                    .removeObserver(this@PermissionRequest)
            }
        })
    }

    fun onViewResume() {
        PendingCallFunction.guidePermissionToast?.cancel()
        PendingCallFunction.guidePermissionToast = null
    }
}

object PendingCallFunction {
    var guidePermissionToast: Toast? = null
}

