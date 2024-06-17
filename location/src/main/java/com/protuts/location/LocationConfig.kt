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
    const val LOCATION_INTENT_SENDER_REQUEST = "location_intent_sender_request"
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

    fun getPermissionListWithoutBackground(): Array<String> {
        val items = mutableListOf<String>()
        items.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        if (isPreciseLocation) items.add(Manifest.permission.ACCESS_FINE_LOCATION)
        return items.toTypedArray()
    }

    val backgroundPermission get() = Manifest.permission.ACCESS_BACKGROUND_LOCATION

    val shouldAskBackgroundLocation get() = isBackgroundLocation && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

    fun listOfPermissions(): Array<String> {
        val items = mutableListOf<String>()
        items.addAll(getPermissionListWithoutBackground())
        if (shouldAskBackgroundLocation) items.add(
            Manifest.permission.ACCESS_BACKGROUND_LOCATION
        )
        return items.toTypedArray()
    }
}

fun LocationConfig.isAllPermissionsGranted(context: Context) =
    this.listOfPermissions().arePermissionsGranted(
        context
    )


fun defaultLocationRequest(isSingleRequest: Boolean = false): LocationRequest {
    val builder = LocationRequest.Builder(INTERVAL_MS).setPriority(Priority.PRIORITY_HIGH_ACCURACY)
        .setMinUpdateIntervalMillis(INTERVAL_MS).setMaxUpdateDelayMillis(INTERVAL_MS)
        .setWaitForAccurateLocation(true)
    if (isSingleRequest)
        builder.setMaxUpdates(1)
    return builder.build()
}