package com.protuts.location

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.protuts.location.LocationPermissionConstants.LOCATION_PERMISSION_RESULT
import com.protuts.location.LocationPermissionConstants.LOCATION_PERMISSION_RESULT_CODE
import com.protuts.location.utils.checkIfPermissionsGranted
import com.protuts.location.utils.getParcelableCompact

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

    val locationPermissionRequest = registerForActivityResult(
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

    private fun postResult(granted: LocationPermissionResult) {
        setResult(LOCATION_PERMISSION_RESULT_CODE, Intent().apply {
            putExtra(LOCATION_PERMISSION_RESULT, granted.ordinal)
        })
        finish()
    }
}