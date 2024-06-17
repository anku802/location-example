package com.protuts.location.service

import android.app.Service
import android.content.Intent
import com.protuts.location.R
import com.protuts.location.notification.NotificationManager
import com.protuts.location.utils.startForegroundCompact
import com.protuts.location.utils.stopForegroundCompact

class LocationForegroundService : Service() {

    private val notificationId = System.currentTimeMillis().toInt()

    private val notificationManager = lazy {
        NotificationManager(this)
    }

    override fun onBind(intent: Intent) = null

    /**
     * Throw persistent notification of type
     * Location
     */
    override fun onCreate() {
        super.onCreate()
        startForegroundCompact(
            notificationId = notificationId,
            notificationManager = notificationManager.value,
            title = getString(R.string.location_request),
            description = getString(R.string.location_is_being_tracked),
            notificationIcon = R.drawable.ic_location
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForegroundCompact(
            notificationId = notificationId,
            notificationManager = notificationManager.value,
            title = getString(R.string.location_request),
            description = getString(R.string.location_is_being_tracked),
            notificationIcon = R.drawable.ic_location
        )
        return START_REDELIVER_INTENT
    }


    override fun onDestroy() {
        super.onDestroy()
        //TODO: Show completed notification.
        stopForegroundCompact()
    }


}