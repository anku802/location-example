package com.protuts.location.utils

import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentActivity
import com.protuts.location.PermissionResponseModel

fun Map<String, Boolean>.checkIfPermissionsGranted(activity: FragmentActivity): PermissionResponseModel {
    var permissionsDeniedPermanently = emptyList<String>()
    val permissionsGranted = this.filter { it.value }.map { it.key }
    val permissionsDenied: List<String> = this.filter {
        !it.value
    }.map {
        it.key
    }

    when {
        permissionsDenied.isNotEmpty() -> {
            val map = permissionsDenied.groupBy { permission ->
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        activity,
                        permission
                    )
                ) 1 else 2
            }
            map[2]?.let {
                //request denied ,send to settings
                permissionsDeniedPermanently = it
            }

        }
    }
    return PermissionResponseModel(
        permissionsGranted = permissionsGranted,
        permissionsDenied = permissionsDenied,
        permissionsDeniedPermanently = permissionsDeniedPermanently
    )

}