package com.rakshalink.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.rakshalink.R
import com.rakshalink.services.NotificationHelper

class GeofenceBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent) ?: return
        if (geofencingEvent.hasError()) return

        val transition = geofencingEvent.geofenceTransition
        val transitionType = when (transition) {
            Geofence.GEOFENCE_TRANSITION_ENTER -> "Entered Safe Zone"
            Geofence.GEOFENCE_TRANSITION_EXIT -> "Exited Safe Zone"
            else -> return
        }

        val notification = NotificationCompat.Builder(context, NotificationHelper.CHANNEL_ZONE)
            .setContentTitle("Safe Zone Event")
            .setContentText("Wearer $transitionType")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(System.currentTimeMillis().toInt(), notification)
        } catch (e: SecurityException) {
            // Permission catch
        }
    }
}

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Re-initialize foreground location service if enabled
        }
    }
}
