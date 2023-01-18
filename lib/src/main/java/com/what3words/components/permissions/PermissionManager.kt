package com.what3words.components.permissions

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager

/**
 * A class that can be used to request multiple runtime permissions at a time.
 *
 * - Get an instance of this class by calling [PermissionManager.getInstance]
 * - Check and Request permissions simultaneously by calling [PermissionManager.checkPermissions]
 * **/
internal class PermissionManager private constructor(private val context: Context) {

    private var permissionRequestListener: PermissionRequestListener? = null

    internal fun onRequestPermissionsResult(
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        // check if some permissions where denied by the user
        val deniedPermissions = hashSetOf<DeniedPermission>()
        for (index in permissions.indices) {
            if (grantResults[index] == PackageManager.PERMISSION_DENIED) {
                deniedPermissions.add(DeniedPermission(permission = permissions[index]))
            }
        }
        if (deniedPermissions.isEmpty()) {
            permissionRequestListener?.onPermissionGranted()
        } else {
            permissionRequestListener?.onPermissionDenied(deniedPermissions = deniedPermissions)
        }
    }

    fun checkPermissions(
        permissions: Array<String>,
        listener: PermissionRequestListener
    ) {
        this.permissionRequestListener = listener
        if (isPermissionsGranted(permissions = permissions)) {
            permissionRequestListener?.onPermissionGranted()
        } else {
            val deniedPermissions = getDeniedPermissions(permissions = permissions)
            requestPermission(permissions = deniedPermissions)
        }
    }

    private fun requestPermission(permissions: Array<String>) {
        val permissionsActivityIntent = Intent(context, PermissionsActivity::class.java).apply {
            putExtra(PermissionsActivity.PERMISSIONS_KEY, permissions)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(permissionsActivityIntent)
    }

    private fun isPermissionsGranted(permissions: Array<String>): Boolean {
        for (permission in permissions) {
            if (context.checkSelfPermission(permission) == PackageManager.PERMISSION_DENIED) return false
        }
        return true
    }

    private fun getDeniedPermissions(permissions: Array<String>): Array<String> {
        val deniedPermissions = hashSetOf<String>()
        for (permission in permissions) {
            if (context.checkSelfPermission(permission) == PackageManager.PERMISSION_DENIED) {
                deniedPermissions.add(permission)
            }
        }
        return deniedPermissions.toTypedArray()
    }


    companion object {
        @SuppressLint("StaticFieldLeak")
        private var permissionManager: PermissionManager? = null

        @JvmStatic
        fun getInstance(context: Context): PermissionManager {
            if (permissionManager == null) {
                // instantiate PermissionManager with the applicationContext to avoid memory leak
                permissionManager = PermissionManager(context = context.applicationContext)
            }
            return permissionManager!!
        }
    }

    interface PermissionRequestListener {
        fun onPermissionGranted()

        fun onPermissionDenied(deniedPermissions: HashSet<DeniedPermission>)
    }
}
