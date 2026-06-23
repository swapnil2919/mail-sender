package com.example.mailsender

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat

/**
 * A foreground service. Its only job is to keep the app "alive" so that
 * the SMS receiver and notification listener keep working reliably,
 * and to show the persistent status notification required by modern Android.
 *
 * The actual SMS/notification capturing happens in SmsReceiver and
 * NotificationForwardService; this service is the anchor that keeps the
 * process from being killed.
 */
class ForwarderService : Service() {

    companion object {
        const val CHANNEL_ID = "mail_sender_service"
        const val NOTIF_ID = 1001

        fun start(ctx: Context) {
            val intent = Intent(ctx, ForwarderService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ctx.startForegroundService(intent)
            } else {
                ctx.startService(intent)
            }
        }

        fun stop(ctx: Context) {
            ctx.stopService(Intent(ctx, ForwarderService::class.java))
        }
    }

    override fun onCreate() {
        super.onCreate()
        createChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIF_ID, buildNotification())
        // If killed by the system, ask Android to recreate it.
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun buildNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Mail Sender running")
            .setContentText("Forwarding messages to your email")
            .setSmallIcon(android.R.drawable.ic_dialog_email)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Mail Sender Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Keeps the forwarding service running"
            }
            val mgr = getSystemService(NotificationManager::class.java)
            mgr.createNotificationChannel(channel)
        }
    }
}
