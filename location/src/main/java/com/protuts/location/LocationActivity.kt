package com.protuts.location

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.protuts.location.Coordinates.locationRequestState
import com.protuts.location.Coordinates.permissionResult
import com.protuts.location.LocationActivityRequestType.Companion.LOCATION_ACTIVITY_TYPE
import com.protuts.location.LocationConstants.LOCATION_INTENT_SENDER_REQUEST
import com.protuts.location.utils.checkIfPermissionsGranted
import com.protuts.location.utils.getParcelableCompact
import kotlinx.coroutines.launch

class LocationActivity : FragmentActivity() {

    private lateinit var config: LocationConfig

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_location)
        config = intent?.getParcelableCompact(
            LocationConstants.LOCATION_CONFIG, LocationConfig::class.java
        ) ?: LocationConfig()

        val type =
            LocationActivityRequestType.fromInt(intent.getIntExtra(LOCATION_ACTIVITY_TYPE, 0))
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        if (type == LocationActivityRequestType.PERMISSION) launchPermissionResult(config.getPermissionListWithoutBackground())
        else intent?.getParcelableCompact(
            LOCATION_INTENT_SENDER_REQUEST, IntentSenderRequest::class.java
        )?.let {
            resolutionForResult.launch(it)
        }
    }

    private fun launchPermissionResult(permissions: Array<String>) {
        locationPermissionRequest.launch(permissions)
    }

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val permissionResult = permissions.checkIfPermissionsGranted(this)
        if (!config.shouldAskBackgroundLocation || permissions.containsKey(config.backgroundPermission))
            postPermissionResult(
                if (permissionResult.permissionsDeniedPermanently.isNotEmpty()) LocationPermissionResult.PERMANENTLY_DENIED
                else if (permissionResult.permissionsDenied.isNotEmpty()) LocationPermissionResult.DENIED
                else LocationPermissionResult.GRANTED
            )
        else
            launchPermissionResult(config.listOfPermissions())
    }

    private fun postPermissionResult(result: LocationPermissionResult) {
        lifecycleScope.launch {
            permissionResult.value = result
        }
        finish()
    }

    private val resolutionForResult =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { activityResult ->
            if (activityResult.resultCode == RESULT_OK) locationRequestState.value =
                ResolutionResult.Success
            else locationRequestState.value =
                ResolutionResult.Failure(message = "we can't determine your location")

            finish()
        }
}