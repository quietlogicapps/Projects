package com.quietlogic.allisok

import android.Manifest
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.quietlogic.allisok.alarm.engine.PermissionGate
import com.quietlogic.allisok.security.LockGate
import com.quietlogic.allisok.security.PinPrefs
import com.quietlogic.allisok.security.TrialManager
import com.quietlogic.allisok.security.UserSession
import com.quietlogic.allisok.ui.home.HomeActivity
import com.quietlogic.allisok.ui.pin.PinActivity
import com.quietlogic.allisok.ui.trial.TrialEndedActivity

class PermissionSetupActivity : AppCompatActivity() {

    private lateinit var labelNotifications: TextView
    private lateinit var labelExactAlarms: TextView
    private lateinit var labelOverlay: TextView
    private lateinit var statusNotifications: TextView
    private lateinit var statusExactAlarms: TextView
    private lateinit var statusOverlay: TextView
    private lateinit var btnNotifications: MaterialButton
    private lateinit var btnExactAlarms: MaterialButton
    private lateinit var btnOverlay: MaterialButton

    private val requestPostNotifications = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ ->
        refreshUi()
        proceedIfReady()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        resetRestoredPinDataOnFirstLaunch()
        UserSession.stop(this)

        setContentView(R.layout.activity_permission_setup)
        window.decorView.setBackgroundColor(Color.BLACK)

        labelNotifications = findViewById(R.id.labelNotifications)
        labelExactAlarms = findViewById(R.id.labelExactAlarms)
        labelOverlay = findViewById(R.id.labelOverlay)
        labelNotifications.text = permissionLabel(R.string.permission_status_notifications_ok)
        labelExactAlarms.text = permissionLabel(R.string.permission_status_exact_alarms_ok)
        labelOverlay.text = permissionLabel(R.string.permission_status_overlay_ok)

        statusNotifications = findViewById(R.id.statusNotifications)
        statusExactAlarms = findViewById(R.id.statusExactAlarms)
        statusOverlay = findViewById(R.id.statusOverlay)
        btnNotifications = findViewById(R.id.btnNotifications)
        btnExactAlarms = findViewById(R.id.btnExactAlarms)
        btnOverlay = findViewById(R.id.btnOverlay)

        refreshUi()
        proceedIfReady()

        btnNotifications.setOnClickListener { onEnableNotificationsClicked() }
        btnExactAlarms.setOnClickListener { onEnableExactAlarmsClicked() }
        btnOverlay.setOnClickListener { onEnableOverlayClicked() }
    }

    override fun onResume() {
        super.onResume()
        refreshUi()
        proceedIfReady()
    }

    private fun resetRestoredPinDataOnFirstLaunch() {
        val firstRunPrefs = getSharedPreferences(FIRST_RUN_PREFS, MODE_PRIVATE)
        val alreadyInitialized = firstRunPrefs.getBoolean(KEY_FIRST_RUN_DONE, false)

        if (alreadyInitialized) return

        getSharedPreferences("pin_prefs", MODE_PRIVATE)
            .edit()
            .clear()
            .commit()

        firstRunPrefs.edit()
            .putBoolean(KEY_FIRST_RUN_DONE, true)
            .commit()
    }

    private fun proceedIfReady(force: Boolean = false) {
        val ok = PermissionGate.isFullyGranted(this)

        if (!ok) {
            if (force) {
                // blocked
            }
            return
        }

        openNextScreen()
    }

    private fun openNextScreen() {
        TrialManager.ensureTrialStarted(this)

        if (!TrialManager.isTrialActive(this)) {
            val intent = Intent(this, TrialEndedActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
            return
        }

        val state = PinPrefs(this).getState()

        val skipPin = intent.getBooleanExtra("skip_pin", false)
        val nextIntent = if (!skipPin && !UserSession.isActive(this) && state.userPinEnabled && !state.userPinHash.isNullOrBlank()) {
            Intent(this, PinActivity::class.java).apply {
                putExtra("mode", LockGate.MODE_USER_UNLOCK)
            }
        } else {
            Intent(this, HomeActivity::class.java)
        }

        nextIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(nextIntent)
        finish()
    }

    private fun refreshUi() {
        val needsNotif = PermissionGate.needsNotificationPermission(this)
        val needsExact = PermissionGate.needsExactAlarmPermission(this)
        val needsOverlay = PermissionGate.needsOverlayPermission(this)

        updateStatusRow(
            statusView = statusNotifications,
            needsPermission = needsNotif,
            okRes = R.string.permission_status_notifications_ok,
            notEnabledRes = R.string.permission_status_notifications_not_enabled
        )

        updateStatusRow(
            statusView = statusExactAlarms,
            needsPermission = needsExact,
            okRes = R.string.permission_status_exact_alarms_ok,
            notEnabledRes = R.string.permission_status_exact_alarms_not_enabled
        )

        updateStatusRow(
            statusView = statusOverlay,
            needsPermission = needsOverlay,
            okRes = R.string.permission_status_overlay_ok,
            notEnabledRes = R.string.permission_status_overlay_not_enabled
        )

        btnNotifications.isEnabled = needsNotif
        btnExactAlarms.isEnabled = needsExact
        btnOverlay.isEnabled = needsOverlay
    }

    private fun updateStatusRow(
        statusView: TextView,
        needsPermission: Boolean,
        okRes: Int,
        notEnabledRes: Int
    ) {
        val fullText = getString(if (needsPermission) notEnabledRes else okRes)
        statusView.text = permissionStatusOnly(fullText)
        statusView.setTextColor(if (needsPermission) COLOR_STATUS_MISSING else COLOR_STATUS_OK)
    }

    private fun permissionLabel(statusOkRes: Int): String {
        val cleaned = getString(statusOkRes).removePrefix("•").trim()
        val colonIndex = cleaned.indexOf(':')
        return if (colonIndex >= 0) {
            cleaned.substring(0, colonIndex + 1).trim()
        } else {
            cleaned
        }
    }

    private fun permissionStatusOnly(fullText: String): String {
        val cleaned = fullText.removePrefix("•").trim()
        val colonIndex = cleaned.indexOf(':')
        return if (colonIndex >= 0) {
            cleaned.substring(colonIndex + 1).trim()
        } else {
            cleaned
        }
    }

    private fun onEnableNotificationsClicked() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPostNotifications.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            openAppNotificationSettings()
        }
    }

    private fun onEnableExactAlarmsClicked() {
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
            openAppDetailsSettings()
        }
    }

    private fun onEnableOverlayClicked() {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:$packageName")
        )
        startActivity(intent)
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

    companion object {
        private const val FIRST_RUN_PREFS = "first_run_prefs"
        private const val KEY_FIRST_RUN_DONE = "first_run_done"
        private const val COLOR_STATUS_OK = 0xFF4CAF50.toInt()
        private const val COLOR_STATUS_MISSING = 0xFFCE93D8.toInt()
    }
}
