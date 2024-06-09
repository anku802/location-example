package com.protuts.location

import android.content.Context
import android.content.Intent
import android.location.Location
import androidx.activity.result.IntentSenderRequest
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.withContext

object Coordinates {

    val permissionResult = MutableStateFlow(false)

    val location = MutableStateFlow<Location?>(null)

    private var config = LocationConfig()

    fun configure(config: LocationConfig) {
        this.config = config
    }

    suspend fun startLocationUpdates(context: Context) {
        withContext(Dispatchers.Main) {
            permissionResult.collectLatest {
                if (it) {
                    startUpdates(context)
                }
            }
            //check if all the permissions are granted
            if (config.isAllPermissionsGranted(context)) {
                permissionResult.value = true
            } else {
                context.applicationContext.startActivity(
                    Intent(
                        context, LocationActivity::class.java
                    )
                )
            }
        }
    }

    private fun startUpdates(context: Context) {
        checkIfLocationRequestSettingsAreSatisfied(context) {

        }
    }

    private fun checkIfLocationRequestSettingsAreSatisfied(
        context: Context,
        onSuccess: () -> Unit,
    ) {
        val builder = LocationSettingsRequest.Builder().addLocationRequest(config.locationRequest)
        val client = LocationServices.getSettingsClient(context)
        val task = client.checkLocationSettings(builder.build())
        task.addOnSuccessListener {
            onSuccess.invoke()
        }
        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                try {
                    if (this.isVisible) gpsLauncher.launch(
                        IntentSenderRequest.Builder(e.resolution.intentSender).build()
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

fun Coordinates.configureLocationRequest(locationConfig: LocationConfig) = this.apply {
    this.configure(locationConfig)
}

interface LocationResult {
    data class Success(val location: Location) : LocationResult
    data class Failure(val error: Exception?, val message: String) : LocationResult
}