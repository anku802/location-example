package com.protuts.location.utils

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Parcelable
import androidx.activity.result.IntentSenderRequest
import com.protuts.location.LocationActivity
import com.protuts.location.LocationActivityRequestType
import com.protuts.location.LocationConfig
import com.protuts.location.LocationConstants
import com.protuts.location.LocationConstants.LOCATION_INTENT_SENDER_REQUEST

fun <T : Parcelable?> Intent.getParcelableCompact(key: String, type: Class<T>): T? = when {
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> getParcelableExtra(key, type)
    else -> @Suppress("DEPRECATION") getParcelableExtra<T>(key)
}

fun Context.startLocationActivity(
    config: LocationConfig,
    requestType: LocationActivityRequestType,
    intentSenderRequest: IntentSenderRequest? = null,
    onFailure: (LocationActivityRequestType) -> Unit
) {
    if (appIsInForeground())
        this.startActivity(
            Intent(
                this, LocationActivity::class.java
            ).apply {
                setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                putExtra(LocationConstants.LOCATION_CONFIG, config)
                putExtra(LOCATION_INTENT_SENDER_REQUEST, intentSenderRequest)
                putExtra(LocationActivityRequestType.LOCATION_ACTIVITY_TYPE, requestType.ordinal)
            }
        )
    else
        onFailure.invoke(requestType)
}

fun Context.appIsInForeground(): Boolean {
    return (this.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager)?.runningAppProcesses?.filter {
        it.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
    }?.any {
        it.pkgList.any { pkg -> pkg == this.packageName }
    } ?: false
}