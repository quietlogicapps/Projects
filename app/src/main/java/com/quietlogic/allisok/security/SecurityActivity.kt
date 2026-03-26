package com.quietlogic.allisok.ui.security

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.google.android.material.button.MaterialButton
import com.quietlogic.allisok.ui.home.Button3D
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.quietlogic.allisok.R
import com.quietlogic.allisok.security.PinPrefs
import com.quietlogic.allisok.ui.pin.PinActivity

class SecurityActivity : AppCompatActivity() {

    private lateinit var switchEnablePin: Switch
    private lateinit var textPinStatus: TextView
    private lateinit var buttonUserChangePin: MaterialButton
    private lateinit var buttonUserDisablePin: MaterialButton
    private lateinit var buttonAdminPin: MaterialButton
    private lateinit var buttonAdminChangePin: MaterialButton

    private lateinit var pinPrefs: PinPrefs

    private var isUpdatingUi = false

    override fun attachBaseContext(newBase: Context) {
        val prefs = newBase.getSharedPreferences("app_settings", MODE_PRIVATE)
        val languageCode = prefs.getString("app_language", "en") ?: "en"
        val locale = if (languageCode.contains("-")) {
            val parts = languageCode.split("-")
            java.util.Locale(parts[0], parts[1])
        } else {
            java.util.Locale(languageCode)
        }
        java.util.Locale.setDefault(locale)
        val configuration = android.content.res.Configuration(newBase.resources.configuration)
        configuration.setLocale(locale)
        val context = newBase.createConfigurationContext(configuration)
        super.attachBaseContext(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_security)

        pinPrefs = PinPrefs(this)

        switchEnablePin = findViewById(R.id.switchEnablePin)
        textPinStatus = findViewById(R.id.textPinStatus)
        buttonUserChangePin = findViewById(R.id.buttonUserChangePin)
        buttonUserDisablePin = findViewById(R.id.buttonUserDisablePin)
        buttonAdminPin = findViewById(R.id.buttonAdminPin)
        buttonAdminChangePin = findViewById(R.id.buttonAdminChangePin)

        Button3D.apply(buttonUserChangePin, cornerDp = 16f, depthDp = 6f)
        Button3D.apply(buttonUserDisablePin, cornerDp = 16f, depthDp = 6f)
        Button3D.apply(buttonAdminPin, cornerDp = 16f, depthDp = 6f)
        Button3D.apply(buttonAdminChangePin, cornerDp = 16f, depthDp = 6f)

        updateState()

        switchEnablePin.setOnCheckedChangeListener { _, isChecked ->

            if (isUpdatingUi) {
                return@setOnCheckedChangeListener
            }

            if (isChecked) {
                val intent = Intent(this, PinActivity::class.java)
                intent.putExtra("PIN_TITLE", getString(R.string.pin_title_change_pin))
                startActivity(intent)
            } else {
                pinPrefs.disableUserPin()
                updateState()
            }
        }

        buttonUserChangePin.setOnClickListener {
            val intent = Intent(this, PinActivity::class.java)
            intent.putExtra("PIN_TITLE", getString(R.string.pin_title_change_pin))
            startActivity(intent)
        }

        buttonUserDisablePin.setOnClickListener {
            pinPrefs.disableUserPin()
            updateState()
        }

        buttonAdminPin.setOnClickListener {
            val intent = Intent(this, PinActivity::class.java)
            intent.putExtra("PIN_TITLE", getString(R.string.pin_title_set_admin_pin))
            startActivity(intent)
        }

        buttonAdminChangePin.setOnClickListener {
            val intent = Intent(this, PinActivity::class.java)
            intent.putExtra("PIN_TITLE", getString(R.string.pin_title_change_admin_pin))
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        updateState()
    }

    private fun updateState() {

        val enabled = pinPrefs.isUserPinEnabled()

        isUpdatingUi = true
        switchEnablePin.isChecked = enabled
        isUpdatingUi = false

        if (enabled) {
            textPinStatus.text = getString(R.string.pin_status_enabled)
        } else {
            textPinStatus.text = getString(R.string.pin_status_disabled)
        }
    }
}