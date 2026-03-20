package com.quietlogic.allisok.ui.security

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.quietlogic.allisok.R
import com.quietlogic.allisok.security.PinPrefs
import com.quietlogic.allisok.ui.pin.PinActivity

class SecurityActivity : AppCompatActivity() {

    private lateinit var switchEnablePin: Switch
    private lateinit var textPinStatus: TextView
    private lateinit var buttonUserChangePin: Button
    private lateinit var buttonUserDisablePin: Button
    private lateinit var buttonAdminPin: Button
    private lateinit var buttonAdminChangePin: Button

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