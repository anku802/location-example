package com.protuts.location

import android.content.Context
import android.content.Intent
import android.location.Location
import androidx.activity.result.IntentSenderRequest
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.protuts.location.LocationActivityRequestType.Companion.LOCATION_ACTIVITY_TYPE
import com.protuts.location.LocationConstants.LOCATION_CONFIG
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext

object Coordinates {

    val permissionResult = MutableStateFlow(false)

    val locationRequestState = MutableStateFlow<ResolutionResult?>(null)

    val location = MutableStateFlow<Location?>(null)

    private var config = LocationConfig()

    fun configure(config: LocationConfig) {
        this.config = config
    }

    suspend fun startLocationUpdates(context: Context) {
        withContext(Dispatchers.Main) {
            permissionResult.collectLatest {
                if (it) {
                    checkIfLocationRequestSettingsAreSatisfied(context)
                }
            }

            locationRequestState.collectLatest {
                
            }

            //check if all the permissions are granted
            if (config.isAllPermissionsGranted(context)) {
                permissionResult.value = true
            } else {
                context.startLocationActivity(
                    config,
                    LocationActivityRequestType.PERMISSION
                )
            }
        }
    }

    private fun checkIfLocationRequestSettingsAreSatisfied(
        context: Context
    ) {
        val builder = LocationSettingsRequest.Builder().addLocationRequest(config.locationRequest)
        val client = LocationServices.getSettingsClient(context)
        val task = client.checkLocationSettings(builder.build())
        task.addOnSuccessListener {
            locationRequestState.update { ResolutionResult.Success }
        }
        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                try {
                    context.startLocationActivity(
                        config,
                        LocationActivityRequestType.LOCATION_REQUEST
                    )
                } catch (e: Exception) {
                    // Ignore the error.
                }

            }
        }
    }

    fun stopLocationUpdates() {

    }

}

fun Context.startLocationActivity(
    config: LocationConfig,
    requestType: LocationActivityRequestType
) {
    this.applicationContext.startActivity(
        Intent(
            this, LocationActivity::class.java
        ).apply {
            putExtra(LOCATION_CONFIG, config)
            putExtra(LOCATION_ACTIVITY_TYPE, requestType.ordinal)
        }
    )
}

fun Coordinates.configureLocationRequest(locationConfig: LocationConfig) = this.apply {
    this.configure(locationConfig)
}

interface LocationResult {
    data class Success(val location: Location) : LocationResult
    data class Failure(val error: Exception?, val message: String) : LocationResult
}

interface ResolutionResult {
    object Success : ResolutionResult
    data class Failure(val message: String) : ResolutionResult
}