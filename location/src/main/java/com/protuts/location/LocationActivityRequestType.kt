package com.protuts.location

enum class LocationActivityRequestType {
    PERMISSION,
    LOCATION_REQUEST;

    companion object {
        fun fromInt(type: Int) = LocationActivityRequestType.entries.first { it.ordinal == type }

        const val LOCATION_ACTIVITY_TYPE = "location_activity_type"
    }
}