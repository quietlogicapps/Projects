package com.quietlogic.allisok.ui.security

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.switchmaterial.SwitchMaterial
import com.quietlogic.allisok.R
import com.quietlogic.allisok.security.PinPrefs
import com.quietlogic.allisok.ui.pin.PinActivity

class SecurityActivity : AppCompatActivity() {

    private lateinit var switchEnablePin: SwitchMaterial
    private lateinit var textPinStatus: TextView
    private lateinit var buttonUserChangePin: MaterialButton
    private lateinit var buttonUserDisablePin: MaterialButton
    private lateinit var frameAdminPinSetup: FrameLayout
    private lateinit var textAdminPinStatus: TextView
    private lateinit var buttonAdminPin: MaterialButton
    private lateinit var buttonAdminChangePin: MaterialButton

    private lateinit var pinPrefs: PinPrefs

    private var isUpdatingUi = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_security)

        pinPrefs = PinPrefs(this)

        switchEnablePin = findViewById(R.id.switchEnablePin)
        textPinStatus = findViewById(R.id.textPinStatus)
        buttonUserChangePin = findViewById(R.id.buttonUserChangePin)
        buttonUserDisablePin = findViewById(R.id.buttonUserDisablePin)
        frameAdminPinSetup = findViewById(R.id.frameAdminPinSetup)
        textAdminPinStatus = findViewById(R.id.textAdminPinStatus)
        buttonAdminPin = findViewById(R.id.buttonAdminPin)
        buttonAdminChangePin = findViewById(R.id.buttonAdminChangePin)

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
            if (pinPrefs.getState().adminPinEnabled) {
                return@setOnClickListener
            }

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
        val adminPinEnabled = pinPrefs.getState().adminPinEnabled

        isUpdatingUi = true
        switchEnablePin.isChecked = enabled
        isUpdatingUi = false

        if (enabled) {
            textPinStatus.text = getString(R.string.pin_status_enabled)
        } else {
            textPinStatus.text = getString(R.string.pin_status_disabled)
        }

        buttonAdminPin.isEnabled = !adminPinEnabled
        frameAdminPinSetup.alpha = if (adminPinEnabled) 0.5f else 1f
        textAdminPinStatus.visibility = if (adminPinEnabled) View.VISIBLE else View.GONE
    }
}
