@file:Suppress("DEPRECATION")

package com.protuts.location.utils

import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.annotation.DrawableRes
import com.protuts.location.notification.NotificationManager

fun Service.startForegroundCompact(
    notificationId: Int,
    notificationManager: NotificationManager,
    title: String,
    description: String,
    notificationIntent: Intent? = null,
    @DrawableRes notificationIcon: Int
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        startForeground(
            notificationId,
            notificationManager.getNotification(
                notificationTitle = title,
                notificationMessage = description,
                notificationId = notificationId,
                notificationIntent = notificationIntent,
                notificationIcon = notificationIcon
            ),
            ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
        )
    } else {
        startForeground(
            notificationId,
            notificationManager.getNotification(
                notificationTitle = title,
                notificationMessage = description,
                notificationId = notificationId,
                notificationIntent = notificationIntent,
                notificationIcon = notificationIcon
            )
        )
    }
}

fun Service.stopForegroundCompact() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        stopForeground(Service.STOP_FOREGROUND_DETACH)
    } else {
        stopForeground(true)
    }
}