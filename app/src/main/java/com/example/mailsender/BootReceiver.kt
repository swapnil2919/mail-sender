package com.example.mailsender

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.mailsender.Prefs.serviceOn

/**
 * Restarts the foreground service after the phone reboots,
 * but only if the user had it switched on.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED && context.serviceOn) {
            ForwarderService.start(context)
        }
    }
}
