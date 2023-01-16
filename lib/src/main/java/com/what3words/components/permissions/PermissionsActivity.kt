package com.what3words.components.permissions

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

/**
 * An activity with a transparent theme used solely to request permissions and receive the [AppCompatActivity.onRequestPermissionsResult] callback
 * **/
internal class PermissionsActivity : AppCompatActivity() {
    private val permissionRequestCode = 1020
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val permissionsToRequest = intent.getStringArrayExtra(PERMISSIONS_KEY)
        permissionsToRequest?.let {
            requestPermissions(permissionsToRequest, permissionRequestCode)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == permissionRequestCode) {
            PermissionManager.getInstance(this).onRequestPermissionsResult(
                permissions = permissions,
                grantResults = grantResults
            )
        }
        finish()
    }

    companion object {
        const val PERMISSIONS_KEY = "permission_key"
    }
}