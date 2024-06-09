package com.protuts.location

data class PermissionResponseModel(
    val permissionsGranted: List<String>,
    val permissionsDenied: List<String>,
    val permissionsDeniedPermanently: List<String>
) {
    val allPermissionGranted get() = permissionsDenied.isEmpty() && permissionsDeniedPermanently.isEmpty()
}
