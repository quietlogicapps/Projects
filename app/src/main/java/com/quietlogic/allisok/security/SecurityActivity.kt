package com.quietlogic.allisok.ui.security

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.quietlogic.allisok.R
import com.quietlogic.allisok.ui.pin.PinActivity

class SecurityActivity : AppCompatActivity() {

    private lateinit var switchEnablePin: Switch
    private lateinit var textPinStatus: TextView
    private lateinit var buttonUserChangePin: Button
    private lateinit var buttonUserDisablePin: Button
    private lateinit var buttonAdminPin: Button
    private lateinit var buttonAdminChangePin: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_security)

        switchEnablePin = findViewById(R.id.switchEnablePin)
        textPinStatus = findViewById(R.id.textPinStatus)
        buttonUserChangePin = findViewById(R.id.buttonUserChangePin)
        buttonUserDisablePin = findViewById(R.id.buttonUserDisablePin)
        buttonAdminPin = findViewById(R.id.buttonAdminPin)
        buttonAdminChangePin = findViewById(R.id.buttonAdminChangePin)

        switchEnablePin.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                textPinStatus.text = "App PIN enabled"
                val intent = Intent(this, PinActivity::class.java)
                intent.putExtra("PIN_TITLE", "Change PIN")
                startActivity(intent)
            } else {
                textPinStatus.text = "App opens without PIN"
            }
        }

        buttonUserChangePin.setOnClickListener {
            val intent = Intent(this, PinActivity::class.java)
            intent.putExtra("PIN_TITLE", "Change PIN")
            startActivity(intent)
        }

        buttonUserDisablePin.setOnClickListener {
            switchEnablePin.isChecked = false
            textPinStatus.text = "App opens without PIN"
        }

        buttonAdminPin.setOnClickListener {
            val intent = Intent(this, PinActivity::class.java)
            intent.putExtra("PIN_TITLE", "Set Admin PIN")
            startActivity(intent)
        }

        buttonAdminChangePin.setOnClickListener {
            val intent = Intent(this, PinActivity::class.java)
            intent.putExtra("PIN_TITLE", "Change Admin PIN")
            startActivity(intent)
        }
    }
}
