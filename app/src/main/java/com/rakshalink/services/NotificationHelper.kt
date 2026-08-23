package com.rakshalink.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.rakshalink.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        const val CHANNEL_SOS = "channel_sos"
        const val CHANNEL_FALL = "channel_fall"
        const val CHANNEL_ZONE = "channel_zone"
        const val CHANNEL_BATTERY = "channel_battery"
        const val CHANNEL_GENERAL = "channel_general"
    }

    fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val sosChannel = NotificationChannel(
                CHANNEL_SOS,
                context.getString(R.string.notification_channel_sos),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.notification_channel_sos_desc)
                enableVibration(true)
            }

            val fallChannel = NotificationChannel(
                CHANNEL_FALL,
                context.getString(R.string.notification_channel_fall),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.notification_channel_fall_desc)
                enableVibration(true)
            }

            val zoneChannel = NotificationChannel(
                CHANNEL_ZONE,
                context.getString(R.string.notification_channel_zone),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = context.getString(R.string.notification_channel_zone_desc)
            }

            val batteryChannel = NotificationChannel(
                CHANNEL_BATTERY,
                context.getString(R.string.notification_channel_battery),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = context.getString(R.string.notification_channel_battery_desc)
            }

            val generalChannel = NotificationChannel(
                CHANNEL_GENERAL,
                context.getString(R.string.notification_channel_general),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = context.getString(R.string.notification_channel_general_desc)
            }

            manager.createNotificationChannels(
                listOf(sosChannel, fallChannel, zoneChannel, batteryChannel, generalChannel)
            )
        }
    }
}
