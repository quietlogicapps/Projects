package com.quietlogic.allisok

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
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
import com.quietlogic.allisok.security.LockGate
import com.quietlogic.allisok.security.PinPrefs
import com.quietlogic.allisok.security.TrialManager
import com.quietlogic.allisok.ui.home.HomeActivity
import com.quietlogic.allisok.ui.pin.PinActivity
import com.quietlogic.allisok.ui.trial.TrialEndedActivity
import java.util.Locale

class PermissionSetupActivity : AppCompatActivity() {

    private lateinit var statusText: TextView
    private lateinit var btnNotifications: Button
    private lateinit var btnExactAlarms: Button
    private lateinit var btnContinue: Button

    override fun attachBaseContext(newBase: Context) {
        val prefs = newBase.getSharedPreferences("app_settings", MODE_PRIVATE)
        val languageCode = prefs.getString("app_language", "en") ?: "en"

        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val configuration = Configuration(newBase.resources.configuration)
        configuration.setLocale(locale)

        val context = newBase.createConfigurationContext(configuration)
        super.attachBaseContext(context)
    }

    private val requestPostNotifications = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ ->
        refreshUi()
        proceedIfReady()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        resetRestoredPinDataOnFirstLaunch()

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

    private fun resetRestoredPinDataOnFirstLaunch() {
        val firstRunPrefs = getSharedPreferences(FIRST_RUN_PREFS, MODE_PRIVATE)
        val alreadyInitialized = firstRunPrefs.getBoolean(KEY_FIRST_RUN_DONE, false)

        if (alreadyInitialized) {
            return
        }

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
                // blocked until permissions are granted
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

        val intent = if (state.userPinEnabled && !state.userPinHash.isNullOrBlank()) {
            Intent(this, PinActivity::class.java).apply {
                putExtra("mode", LockGate.MODE_USER_UNLOCK)
            }
        } else {
            Intent(this, HomeActivity::class.java)
        }

        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun refreshUi() {
        val needsNotif = PermissionGate.needsNotificationPermission(this)
        val needsExact = PermissionGate.needsExactAlarmPermission(this)

        val lines = mutableListOf<String>()

        if (needsNotif) {
            lines.add(getString(R.string.permission_status_notifications_not_enabled))
        } else {
            lines.add(getString(R.string.permission_status_notifications_ok))
        }

        if (needsExact) {
            lines.add(getString(R.string.permission_status_exact_alarms_not_enabled))
        } else {
            lines.add(getString(R.string.permission_status_exact_alarms_ok))
        }

        statusText.text = lines.joinToString("\n")

        btnNotifications.isEnabled = needsNotif
        btnExactAlarms.isEnabled = needsExact
        btnContinue.visibility = if (needsNotif || needsExact) View.GONE else View.VISIBLE
    }

    private fun onEnableNotificationsClicked() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPostNotifications.launch(Manifest.permission.POST_NOTIFICATIONS)
            return
        }

        openAppNotificationSettings()
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
            text = getString(R.string.permission_setup_title)
            textSize = 22f
            gravity = Gravity.CENTER_HORIZONTAL
        }

        val subtitle = TextView(this).apply {
            text = getString(R.string.permission_setup_subtitle)
            textSize = 16f
            gravity = Gravity.CENTER_HORIZONTAL
        }

        statusText = TextView(this).apply {
            textSize = 16f
            setPadding(0, dp(18), 0, dp(18))
        }

        btnNotifications = Button(this).apply {
            text = getString(R.string.permission_enable_notifications)
        }

        btnExactAlarms = Button(this).apply {
            text = getString(R.string.permission_enable_exact_alarms)
        }

        btnContinue = Button(this).apply {
            text = getString(R.string.permission_continue)
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

    companion object {
        private const val FIRST_RUN_PREFS = "first_run_prefs"
        private const val KEY_FIRST_RUN_DONE = "first_run_done"
    }
}