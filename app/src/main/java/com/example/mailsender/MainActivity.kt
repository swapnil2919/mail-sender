package com.example.mailsender

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.mailsender.Prefs.appPassword
import com.example.mailsender.Prefs.forwardNotif
import com.example.mailsender.Prefs.forwardSms
import com.example.mailsender.Prefs.fromEmail
import com.example.mailsender.Prefs.serviceOn
import com.example.mailsender.Prefs.toEmail
import com.example.mailsender.databinding.ActivityMainBinding
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {

    private lateinit var b: ActivityMainBinding

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { /* result handled by status refresh */ updateStatus() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)

        loadIntoUi()

        b.btnSave.setOnClickListener { save() }
        b.btnStart.setOnClickListener { startService() }
        b.btnStop.setOnClickListener { stopService() }
        b.btnTest.setOnClickListener { sendTestEmail() }
        b.btnNotifAccess.setOnClickListener {
            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        }
    }

    override fun onResume() {
        super.onResume()
        updateStatus()
    }

    private fun loadIntoUi() {
        b.etTo.setText(toEmail)
        b.etFrom.setText(fromEmail)
        b.etPassword.setText(appPassword)
        b.swSms.isChecked = forwardSms
        b.swNotif.isChecked = forwardNotif
    }

    private fun save() {
        toEmail = b.etTo.text.toString().trim()
        fromEmail = b.etFrom.text.toString().trim()
        appPassword = b.etPassword.text.toString().trim()
        forwardSms = b.swSms.isChecked
        forwardNotif = b.swNotif.isChecked
        Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show()
        updateStatus()
    }

    private fun startService() {
        save()
        if (!Prefs.isConfigured(this)) {
            Toast.makeText(this, "Fill in all email fields first", Toast.LENGTH_LONG).show()
            return
        }
        requestNeededPermissions()
        serviceOn = true
        ForwarderService.start(this)
        Toast.makeText(this, "Service started", Toast.LENGTH_SHORT).show()
        updateStatus()
    }

    private fun stopService() {
        serviceOn = false
        ForwarderService.stop(this)
        Toast.makeText(this, "Service stopped", Toast.LENGTH_SHORT).show()
        updateStatus()
    }

    private fun sendTestEmail() {
        save()
        if (!Prefs.isConfigured(this)) {
            Toast.makeText(this, "Fill in all email fields first", Toast.LENGTH_LONG).show()
            return
        }
        Toast.makeText(this, "Sending test email...", Toast.LENGTH_SHORT).show()
        thread {
            val ok = EmailSender.send(
                applicationContext,
                "Mail Sender test",
                "If you can read this, your setup works! 🎉"
            )
            runOnUiThread {
                Toast.makeText(
                    this,
                    if (ok) "Test email sent ✓" else "Failed — check email/app password",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun requestNeededPermissions() {
        val needed = mutableListOf<String>()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS)
            != PackageManager.PERMISSION_GRANTED
        ) needed += Manifest.permission.RECEIVE_SMS
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS)
            != PackageManager.PERMISSION_GRANTED
        ) needed += Manifest.permission.READ_SMS
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) needed += Manifest.permission.POST_NOTIFICATIONS
        }
        if (needed.isNotEmpty()) permissionLauncher.launch(needed.toTypedArray())
    }

    private fun updateStatus() {
        b.tvStatus.text = if (serviceOn) "Status: ● Running" else "Status: ○ Stopped"
    }
}
