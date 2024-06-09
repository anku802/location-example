package com.protuts.location

import android.Manifest
import android.os.Build
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

object LocationConstants {
    const val LOCATION_CONFIG = "location_config"
}

@Parcelize
data class LocationConfig(
    val isPreciseLocation: Boolean = false,
    val isBackgroundLocation: Boolean = false,
    val showForegroundService: Boolean = false
) : Parcelable {
    val listOfPermissions
        get() = arrayOf<String>().apply {
            Manifest.permission.ACCESS_COARSE_LOCATION
            if (isPreciseLocation)
                Manifest.permission.ACCESS_FINE_LOCATION
            if (isBackgroundLocation && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            }
        }
}
