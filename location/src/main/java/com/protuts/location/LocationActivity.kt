package com.protuts.location

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.protuts.location.Coordinates.permissionResult
import com.protuts.location.utils.checkIfPermissionsGranted
import com.protuts.location.utils.getParcelableCompact
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LocationActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_location)
        val config = intent?.getParcelableCompact(
            LocationConstants.LOCATION_CONFIG,
            LocationConfig::class.java
        ) ?: LocationConfig()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        locationPermissionRequest.launch(config.listOfPermissions)
    }

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val permissionResult = permissions.checkIfPermissionsGranted(this)
        postResult(
            if (permissionResult.permissionsDeniedPermanently.isNotEmpty())
                LocationPermissionResult.PERMANENTLY_DENIED
            else if (permissionResult.permissionsDenied.isNotEmpty())
                LocationPermissionResult.DENIED
            else
                LocationPermissionResult.GRANTED
        )
    }

    private fun postResult(result: LocationPermissionResult) {
        lifecycleScope.launch {
            if (result == LocationPermissionResult.GRANTED) {
                permissionResult.update {
                    true
                }
            }
        }
        finish()
    }

    private val resolutionForResult =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { activityResult ->
            if (activityResult.resultCode == RESULT_OK)
            //startLocationUpdates() or do whatever you want
            else {
//                showMessage("we can't determine your location")
            }
        }
}