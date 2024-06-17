package com.protuts.location

import android.content.Context
import android.location.Location
import android.os.Looper
import androidx.activity.result.IntentSenderRequest
import androidx.annotation.MainThread
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.protuts.location.utils.startLocationActivity

object Coordinates {

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    val permissionResult = MutableLiveData(LocationPermissionResult.NONE)

    val locationRequestState = MutableLiveData<ResolutionResult>(ResolutionResult.None)

    private val location = MutableLiveData<CoordinatesResult?>(null)

    private var config = LocationConfig()

    fun configure(config: LocationConfig) {
        this.config = config
    }

    private fun initProviders(context: Context) {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
    }

    @MainThread
    private fun initObservers(
        lifecycleScope: LifecycleOwner,
        context: Context,
        onLocationResult: (CoordinatesResult) -> Unit
    ) {
        permissionResult.observe(lifecycleScope) {
            if (it == LocationPermissionResult.GRANTED) {
                checkIfLocationRequestSettingsAreSatisfied(context)
            } else {
                location.value = CoordinatesResult.Failure(LocationFailureCode.PERMISSIONS_DENIED)
            }
        }
        locationRequestState.observe(lifecycleScope) {
            if (it is ResolutionResult.Success) startUpdates()
        }

        location.observe(lifecycleScope) { result ->
            result?.let { onLocationResult.invoke(result) }
        }
    }

    fun startLocationUpdates(
        context: Context,
        lifecycleScope: LifecycleOwner,
        onLocationResult: (CoordinatesResult) -> Unit
    ) {
        //check if all the permissions are granted
        initProviders(context)
        initObservers(lifecycleScope, context, onLocationResult)
        if (config.isAllPermissionsGranted(context)) {
            permissionResult.value = LocationPermissionResult.GRANTED
        } else {
            context.startLocationActivity(
                config = config, requestType = LocationActivityRequestType.PERMISSION
            ) {
                location.value = CoordinatesResult.Failure(LocationFailureCode.PERMISSIONS_DENIED)
            }
        }
    }

    private fun checkIfLocationRequestSettingsAreSatisfied(context: Context) {
        val builder = LocationSettingsRequest.Builder().addLocationRequest(config.locationRequest)
        val client = LocationServices.getSettingsClient(context)
        val task = client.checkLocationSettings(builder.build())
        task.addOnSuccessListener {
            locationRequestState.value = ResolutionResult.Success
        }
        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                try {
                    context.startLocationActivity(
                        config = config,
                        requestType = LocationActivityRequestType.LOCATION_REQUEST,
                        IntentSenderRequest.Builder(exception.resolution.intentSender).build()
                    ) {
                        location.value =
                            CoordinatesResult.Failure(LocationFailureCode.LOCATION_REQUEST_FAILED)
                    }
                } catch (e: Exception) {
                    // Ignore the error.
                    location.value =
                        CoordinatesResult.Failure(LocationFailureCode.LOCATION_REQUEST_FAILED)
                }

            }
        }
    }


    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(coordinatesResult: LocationResult) {
            super.onLocationResult(coordinatesResult)
            location.value = CoordinatesResult.Success(coordinatesResult.locations.minBy {
                it.accuracy
            })
        }
    }

    private fun startUpdates() {
        fusedLocationProviderClient.requestLocationUpdates(
            config.locationRequest, locationCallback, Looper.getMainLooper()
        )
    }

    fun stopLocationUpdates(lifecycleScope: LifecycleOwner) {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        location.removeObservers(lifecycleScope)
        permissionResult.removeObservers(lifecycleScope)
        locationRequestState.removeObservers(lifecycleScope)
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

    object None : ResolutionResult
    object Success : ResolutionResult
    data class Failure(val message: String) : ResolutionResult
}

enum class LocationFailureCode {
    PERMISSIONS_DENIED, LOCATION_REQUEST_FAILED
}