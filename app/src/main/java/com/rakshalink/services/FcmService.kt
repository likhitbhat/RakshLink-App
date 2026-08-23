package com.rakshalink.services

import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.rakshalink.MainActivity
import com.rakshalink.R
import com.rakshalink.data.remote.dto.PushSubscriptionDto
import com.rakshalink.data.remote.supabase.SupabaseClientProvider
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class FcmService : FirebaseMessagingService() {

    @Inject
    lateinit var supabaseProvider: SupabaseClientProvider

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        val userId = supabaseProvider.auth.currentSessionOrNull()?.user?.id ?: return
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val dto = PushSubscriptionDto(userId = userId, fcmToken = token)
                supabaseProvider.db.from("push_subscriptions").insert(dto)
            } catch (e: Exception) {
                // Token update fallback
            }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val title = message.notification?.title ?: message.data["title"] ?: "RakshaLink Alert"
        val body = message.notification?.body ?: message.data["body"] ?: "Emergency update received."
        val channelId = when (message.data["type"]) {
            "SOS" -> NotificationHelper.CHANNEL_SOS
            "FALL" -> NotificationHelper.CHANNEL_FALL
            "ZONE" -> NotificationHelper.CHANNEL_ZONE
            "BATTERY" -> NotificationHelper.CHANNEL_BATTERY
            else -> NotificationHelper.CHANNEL_GENERAL
        }

        val alertId = message.data["alert_id"] ?: ""
        val deepLinkIntent = Intent(Intent.ACTION_VIEW, Uri.parse("rakshalink://alert?id=$alertId"), this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, deepLinkIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, channelId)
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        try {
            NotificationManagerCompat.from(this).notify(System.currentTimeMillis().toInt(), builder.build())
        } catch (e: SecurityException) {
            // Permission missing
        }
    }
}
