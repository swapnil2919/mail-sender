package com.example.mailsender

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log
import com.example.mailsender.Prefs.forwardSms
import com.example.mailsender.Prefs.serviceOn
import kotlin.concurrent.thread

/**
 * Fires whenever a new SMS arrives. It assembles the message and
 * forwards it to the configured email address.
 */
class SmsReceiver : BroadcastReceiver() {

    private val tag = "SmsReceiver"

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        // Respect the on/off switch and the SMS toggle
        if (!context.serviceOn || !context.forwardSms) return

        // A single SMS can be split into multiple parts; join them.
        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent) ?: return
        if (messages.isEmpty()) return

        val sender = messages[0].displayOriginatingAddress ?: "Unknown"
        val fullBody = buildString {
            for (m in messages) append(m.displayMessageBody)
        }

        Log.i(tag, "SMS from $sender")

        val subject = "SMS from $sender"
        val body = "From: $sender\n\n$fullBody"

        // Network work must be off the main thread.
        thread {
            EmailSender.send(context.applicationContext, subject, body)
        }
    }
}
