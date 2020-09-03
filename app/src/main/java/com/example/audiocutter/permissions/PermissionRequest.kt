package com.example.a0025antivirusapplockclean.permissions
import com.example.audiocutter.base.BaseActivity

typealias PendingPermissionAction = () -> Unit

interface PermissionRequest {
    val QUERY_APP_INFO_REQUEST_CODE: Int get() = 1
    val CAMERA_REQUEST_CODE: Int get() = 2
    val CALL_ASSISTANT_REQUEST_CODE: Int get() = 200
    fun getPermissionActivity(): BaseActivity
    fun requestPermission(pendingAction: () -> Unit)
}

object PendingActionManager {
    private val permissionRequestMap = HashMap<PermissionRequest, PendingPermissionAction>()
    fun put(permissionRequest: PermissionRequest, pendingAction: PendingPermissionAction) {
        permissionRequestMap.put(permissionRequest, pendingAction)
    }
    fun remove(permissionRequest: PermissionRequest) {
        permissionRequestMap.remove(permissionRequest)
    }
    fun get(permissionRequest: PermissionRequest) : PendingPermissionAction?{
        return permissionRequestMap.get(permissionRequest)
    }

}
