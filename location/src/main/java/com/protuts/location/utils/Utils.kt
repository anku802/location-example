package com.protuts.location.utils

import android.content.Intent
import android.os.Build
import android.os.Parcelable

fun <T : Parcelable?> Intent.getParcelableCompact(key: String, type: Class<T>): T? = when {
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> getParcelableExtra(key, type)
    else -> @Suppress("DEPRECATION") getParcelableExtra<T>(key)
}