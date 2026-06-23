package com.example.mailsender

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.example.mailsender.Prefs.forwardNotif
import com.example.mailsender.Prefs.serviceOn
import kotlin.concurrent.thread

/**
 * Listens to notifications posted by other apps and forwards them to email.
 *
 * Requires the user to grant "Notification access" in:
 * Settings -> Apps -> Special app access -> Notification access.
 */
class NotificationForwardService : NotificationListenerService() {

    private val tag = "NotifForward"

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val ctx = applicationContext

        if (!ctx.serviceOn || !ctx.forwardNotif) return

        // Skip our own foreground-service notification to avoid loops/noise.
        if (sbn.packageName == packageName) return

        val extras = sbn.notification.extras
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: ""
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""

        // Ignore empty/ongoing system notifications
        if (title.isBlank() && text.isBlank()) return

        val appName = sbn.packageName
        Log.i(tag, "Notification from $appName: $title")

        val subject = "Notification: $title".ifBlank { "Notification from $appName" }
        val body = "App: $appName\nTitle: $title\n\n$text"

        thread {
            EmailSender.send(ctx, subject, body)
        }
    }
}
