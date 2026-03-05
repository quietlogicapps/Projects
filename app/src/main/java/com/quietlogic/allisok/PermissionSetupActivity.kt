package com.quietlogic.allisok

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.quietlogic.allisok.alarm.engine.PermissionGate
import com.quietlogic.allisok.ui.home.HomeActivity

class PermissionSetupActivity : AppCompatActivity() {

    private lateinit var statusText: TextView
    private lateinit var btnNotifications: Button
    private lateinit var btnExactAlarms: Button
    private lateinit var btnContinue: Button

    private val requestPostNotifications = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ ->
        refreshUi()
        proceedIfReady()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(buildContentView())
        refreshUi()
        proceedIfReady()

        btnNotifications.setOnClickListener { onEnableNotificationsClicked() }
        btnExactAlarms.setOnClickListener { onEnableExactAlarmsClicked() }
        btnContinue.setOnClickListener { proceedIfReady(force = true) }
    }

    override fun onResume() {
        super.onResume()
        refreshUi()
        proceedIfReady()
    }

    private fun proceedIfReady(force: Boolean = false) {
        val ok = PermissionGate.isFullyGranted(this)
        if (!ok) {
            if (force) {
                // No-op: we intentionally block the app until permissions are granted.
            }
            return
        }

        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }

    private fun refreshUi() {
        val needsNotif = PermissionGate.needsNotificationPermission(this)
        val needsExact = PermissionGate.needsExactAlarmPermission(this)

        val lines = mutableListOf<String>()

        if (needsNotif) {
            lines.add("• Notifications: NOT ENABLED")
        } else {
            lines.add("• Notifications: OK")
        }

        if (needsExact) {
            lines.add("• Exact alarms: NOT ENABLED")
        } else {
            lines.add("• Exact alarms: OK")
        }

        statusText.text = lines.joinToString("\n")

        btnNotifications.isEnabled = needsNotif
        btnExactAlarms.isEnabled = needsExact

        // Continue button is only visible when everything is OK (extra clarity)
        btnContinue.visibility = if (needsNotif || needsExact) View.GONE else View.VISIBLE
    }

    private fun onEnableNotificationsClicked() {
        // Android 13+ uses runtime permission; below that we open app notification settings.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPostNotifications.launch(Manifest.permission.POST_NOTIFICATIONS)
            return
        }

        openAppNotificationSettings()
    }

    private fun onEnableExactAlarmsClicked() {
        // Exact alarms special access is Android 12+ (S)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            refreshUi()
            proceedIfReady()
            return
        }

        try {
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
            intent.data = Uri.parse("package:$packageName")
            startActivity(intent)
        } catch (_: Exception) {
            // Fallback: open app details settings
            openAppDetailsSettings()
        }
    }

    private fun openAppNotificationSettings() {
        try {
            val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
            }
            startActivity(intent)
        } catch (_: Exception) {
            openAppDetailsSettings()
        }
    }

    private fun openAppDetailsSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:$packageName")
        }
        startActivity(intent)
    }

    private fun buildContentView(): View {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(20), dp(24), dp(20), dp(24))
        }

        val title = TextView(this).apply {
            text = "Setup required"
            textSize = 22f
            gravity = Gravity.CENTER_HORIZONTAL
        }

        val subtitle = TextView(this).apply {
            text = "To use the app, enable notifications and exact alarms."
            textSize = 16f
            gravity = Gravity.CENTER_HORIZONTAL
        }

        statusText = TextView(this).apply {
            textSize = 16f
            setPadding(0, dp(18), 0, dp(18))
        }

        btnNotifications = Button(this).apply {
            text = "Enable notifications"
        }

        btnExactAlarms = Button(this).apply {
            text = "Enable exact alarms"
        }

        btnContinue = Button(this).apply {
            text = "Continue"
            visibility = View.GONE
        }

        root.addView(title, lpMatchWrap())
        root.addView(subtitle, lpMatchWrap().apply { topMargin = dp(10) })
        root.addView(statusText, lpMatchWrap())
        root.addView(btnNotifications, lpMatchWrap().apply { topMargin = dp(10) })
        root.addView(btnExactAlarms, lpMatchWrap().apply { topMargin = dp(10) })
        root.addView(btnContinue, lpMatchWrap().apply { topMargin = dp(16) })

        return root
    }

    private fun lpMatchWrap(): LinearLayout.LayoutParams {
        return LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
    }

    private fun dp(value: Int): Int {
        val density = resources.displayMetrics.density
        return (value * density).toInt()
    }
}