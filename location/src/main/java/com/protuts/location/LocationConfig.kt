package com.protuts.location

import android.Manifest
import android.content.Context
import android.os.Build
import android.os.Parcelable
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.Priority
import com.protuts.location.LocationConstants.INTERVAL_MS
import com.protuts.location.utils.arePermissionsGranted
import kotlinx.parcelize.Parcelize

object LocationConstants {
    const val LOCATION_CONFIG = "location_config"
    const val INTERVAL_MS = 1000L
}

@Parcelize
data class LocationConfig(
    val locationRequest: LocationRequest = defaultLocationRequest(),
    val isPreciseLocation: Boolean = false,
    val isBackgroundLocation: Boolean = false,
    val showForegroundService: Boolean = false,
    val minAccuracyFilter: Float? = null
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

fun LocationConfig.isAllPermissionsGranted(context: Context) =
    this.listOfPermissions.arePermissionsGranted(
        context
    )


fun defaultLocationRequest() = LocationRequest.Builder(INTERVAL_MS)
    .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
    .setMinUpdateIntervalMillis(INTERVAL_MS)
    .setMaxUpdateDelayMillis(INTERVAL_MS)
    .setWaitForAccurateLocation(true)
    .build()