package com.protuts.location.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.os.Build
import androidx.annotation.DrawableRes
import androidx.core.app.NotificationCompat

class NotificationManager(private val context: Context) {

    private var notificationManager = lazy {
        context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
    }

    fun getNotification(
        channelId: String = CHANNEL_ID,
        channelName: String = CHANNEL_NAME,
        notificationTitle: String,
        notificationMessage: String,
        notificationId: Int,
        notificationIntent: Intent? = null,
        @DrawableRes notificationIcon: Int
    ): Notification {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) notificationManager.value.createNotificationChannel(
            NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH)
        )
        val builder = NotificationCompat.Builder(context, channelId)
        builder.setSmallIcon(notificationIcon)
        builder.setContentTitle(notificationTitle)
        builder.setContentText(notificationMessage)
        builder.setStyle(
            NotificationCompat.BigTextStyle().setBigContentTitle(notificationTitle)
                .bigText(notificationMessage)
        )

        val pendingIntent = PendingIntent.getActivity(
            context, notificationId, notificationIntent,
            if (Build.VERSION.SDK_INT >= 31) PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE else PendingIntent.FLAG_UPDATE_CURRENT
        )
        builder.setContentIntent(pendingIntent)

        return builder.build()

    }

    companion object {
        const val CHANNEL_ID = "com.protuts.location.locationchannel"
        const val CHANNEL_NAME = "Location Tracking"
    }

}