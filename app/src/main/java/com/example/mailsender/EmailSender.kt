package com.example.mailsender

import android.content.Context
import android.util.Log
import com.example.mailsender.Prefs.appPassword
import com.example.mailsender.Prefs.fromEmail
import com.example.mailsender.Prefs.toEmail
import java.util.Properties
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

/**
 * Sends an email through Gmail's SMTP server using the credentials
 * the user saved in the app.
 *
 * NOTE: The "from" account must use a Gmail **App Password**
 * (Google Account -> Security -> 2-Step Verification -> App passwords),
 * NOT the normal login password.
 */
object EmailSender {

    private const val TAG = "EmailSender"
    private const val SMTP_HOST = "smtp.gmail.com"
    private const val SMTP_PORT = "587"

    /**
     * Sends an email. MUST be called off the main thread.
     * Returns true on success.
     */
    fun send(ctx: Context, subject: String, body: String): Boolean {
        val to = ctx.toEmail
        val from = ctx.fromEmail
        val password = ctx.appPassword

        if (to.isBlank() || from.isBlank() || password.isBlank()) {
            Log.w(TAG, "Email not configured; skipping send.")
            return false
        }

        val props = Properties().apply {
            put("mail.smtp.auth", "true")
            put("mail.smtp.starttls.enable", "true")
            put("mail.smtp.host", SMTP_HOST)
            put("mail.smtp.port", SMTP_PORT)
        }

        val session = Session.getInstance(props, object : Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication {
                return PasswordAuthentication(from, password)
            }
        })

        return try {
            val message = MimeMessage(session).apply {
                setFrom(InternetAddress(from))
                addRecipient(Message.RecipientType.TO, InternetAddress(to))
                setSubject(subject)
                setText(body)
            }
            Transport.send(message)
            Log.i(TAG, "Email sent: $subject")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send email", e)
            false
        }
    }
}
