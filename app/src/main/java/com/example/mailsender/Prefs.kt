package com.example.mailsender

import android.content.Context

/**
 * Simple wrapper around SharedPreferences.
 * Stores all configuration the user enters in the app:
 *  - recipient email (where to forward)
 *  - sender Gmail + app password (the account used to send via SMTP)
 *  - toggles for what to forward
 */
object Prefs {
    private const val FILE = "mail_sender_prefs"

    private const val KEY_TO = "to_email"
    private const val KEY_FROM = "from_email"
    private const val KEY_APP_PASSWORD = "app_password"
    private const val KEY_FORWARD_SMS = "forward_sms"
    private const val KEY_FORWARD_NOTIF = "forward_notif"
    private const val KEY_SERVICE_ON = "service_on"

    private fun prefs(ctx: Context) =
        ctx.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    var Context.toEmail: String
        get() = prefs(this).getString(KEY_TO, "") ?: ""
        set(v) { prefs(this).edit().putString(KEY_TO, v).apply() }

    var Context.fromEmail: String
        get() = prefs(this).getString(KEY_FROM, "") ?: ""
        set(v) { prefs(this).edit().putString(KEY_FROM, v).apply() }

    var Context.appPassword: String
        get() = prefs(this).getString(KEY_APP_PASSWORD, "") ?: ""
        set(v) { prefs(this).edit().putString(KEY_APP_PASSWORD, v).apply() }

    var Context.forwardSms: Boolean
        get() = prefs(this).getBoolean(KEY_FORWARD_SMS, true)
        set(v) { prefs(this).edit().putBoolean(KEY_FORWARD_SMS, v).apply() }

    var Context.forwardNotif: Boolean
        get() = prefs(this).getBoolean(KEY_FORWARD_NOTIF, false)
        set(v) { prefs(this).edit().putBoolean(KEY_FORWARD_NOTIF, v).apply() }

    var Context.serviceOn: Boolean
        get() = prefs(this).getBoolean(KEY_SERVICE_ON, false)
        set(v) { prefs(this).edit().putBoolean(KEY_SERVICE_ON, v).apply() }

    fun isConfigured(ctx: Context): Boolean {
        return ctx.toEmail.isNotBlank() &&
                ctx.fromEmail.isNotBlank() &&
                ctx.appPassword.isNotBlank()
    }
}
