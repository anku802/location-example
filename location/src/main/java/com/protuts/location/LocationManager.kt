package com.protuts.location

import android.content.Context
import android.location.Location
import android.os.Looper
import androidx.activity.result.IntentSenderRequest
import androidx.lifecycle.LifecycleCoroutineScope
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.protuts.location.utils.startLocationActivity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

object Coordinates {

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    val permissionResult = MutableStateFlow(LocationPermissionResult.NONE)

    val locationRequestState = MutableStateFlow<ResolutionResult?>(null)

    val location = MutableStateFlow<CoordinatesResult?>(null)

    private var config = LocationConfig()

    fun configure(config: LocationConfig) {
        this.config = config
    }

    private fun initProviders(context: Context) {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
    }

    private fun initObservers(lifecycleScope: LifecycleCoroutineScope, context: Context) {
        lifecycleScope.launch {
            permissionResult.collectLatest {
                if (it == LocationPermissionResult.GRANTED) {
                    checkIfLocationRequestSettingsAreSatisfied(context)
                } else {
                    location.update {
                        CoordinatesResult.Failure(LocationFailureCode.PERMISSIONS_DENIED)
                    }
                }
            }
        }

        lifecycleScope.launch {
            locationRequestState.collectLatest {
                if (it is ResolutionResult.Success)
                    startUpdates()
            }
        }
    }

    fun startLocationUpdates(context: Context, lifecycleScope: LifecycleCoroutineScope) {
        //check if all the permissions are granted
        initProviders(context)
        initObservers(lifecycleScope, context)
        if (config.isAllPermissionsGranted(context)) {
            permissionResult.value = LocationPermissionResult.GRANTED
        } else {
            context.startLocationActivity(
                config = config, requestType = LocationActivityRequestType.PERMISSION
            ) {
                location.update {
                    CoordinatesResult.Failure(LocationFailureCode.PERMISSIONS_DENIED)
                }
            }
        }
    }

    private fun checkIfLocationRequestSettingsAreSatisfied(context: Context) {
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
                        config = config, requestType = LocationActivityRequestType.LOCATION_REQUEST,
                        IntentSenderRequest.Builder(exception.resolution.intentSender).build()
                    ) {
                        location.update {
                            CoordinatesResult.Failure(LocationFailureCode.LOCATION_REQUEST_FAILED)
                        }
                    }
                } catch (e: Exception) {
                    // Ignore the error.
                    location.update {
                        CoordinatesResult.Failure(LocationFailureCode.LOCATION_REQUEST_FAILED)
                    }
                }

            }
        }
    }


    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(coordinatesResult: LocationResult) {
            super.onLocationResult(coordinatesResult)
            location.update {
                CoordinatesResult.Success(coordinatesResult.locations.minBy {
                    it.accuracy
                })
            }
        }
    }

    private fun startUpdates() {
        fusedLocationProviderClient.requestLocationUpdates(
            config.locationRequest, locationCallback, Looper.getMainLooper()
        )

    }

    fun stopLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }

}

fun Coordinates.configureLocationRequest(locationConfig: LocationConfig) = this.apply {
    this.configure(locationConfig)
}

interface CoordinatesResult {
    data class Success(val location: Location) : CoordinatesResult
    data class Failure(val locationFailure: LocationFailureCode) : CoordinatesResult
}

interface ResolutionResult {
    object Success : ResolutionResult
    data class Failure(val message: String) : ResolutionResult
}

enum class LocationFailureCode {
    PERMISSIONS_DENIED, LOCATION_REQUEST_FAILED
}