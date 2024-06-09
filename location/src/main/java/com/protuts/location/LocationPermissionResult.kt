package com.protuts.location

object LocationPermissionConstants {
    const val LOCATION_PERMISSION_RESULT_CODE = 1
    const val LOCATION_PERMISSION_RESULT = "location_permission_result"
}

enum class LocationPermissionResult {
    GRANTED,
    DENIED,
    PERMANENTLY_DENIED;

    companion object {
        fun fromInt(type: Int) = entries.first { it.ordinal == type }
    }
}

