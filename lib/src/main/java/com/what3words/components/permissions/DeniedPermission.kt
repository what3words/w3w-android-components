package com.what3words.components.permissions

/**
 * A class representing a denied permission
 *
 * @property permission an android permission that was denied by the user
 * **/
internal data class DeniedPermission(
    val permission: String
)

