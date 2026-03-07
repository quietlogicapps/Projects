package com.quietlogic.allisok.ui.pin

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.quietlogic.allisok.R

class PinActivity : AppCompatActivity() {

    private lateinit var textTitle: TextView
    private lateinit var editPin: EditText
    private lateinit var editPinSecond: EditText
    private lateinit var textForgot: TextView
    private lateinit var textError: TextView
    private lateinit var buttonPrimary: Button
    private lateinit var buttonSecondary: Button

    private var currentScreen: String = SCREEN_ENTER_PIN

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pin)

        textTitle = findViewById(R.id.textTitle)
        editPin = findViewById(R.id.editPin)
        editPinSecond = findViewById(R.id.editPinSecond)
        textForgot = findViewById(R.id.textForgot)
        textError = findViewById(R.id.textError)
        buttonPrimary = findViewById(R.id.buttonPrimary)
        buttonSecondary = findViewById(R.id.buttonSecondary)

        val titleFromIntent = intent.getStringExtra("PIN_TITLE").orEmpty()

        currentScreen = when (titleFromIntent) {
            "Change PIN" -> SCREEN_CHANGE_PIN
            "Set Admin PIN" -> SCREEN_SET_ADMIN_PIN
            "Change Admin PIN" -> SCREEN_CHANGE_ADMIN_PIN_STEP_1
            else -> SCREEN_ENTER_PIN
        }

        renderScreen()

        buttonPrimary.setOnClickListener {
            when (currentScreen) {
                SCREEN_CHANGE_ADMIN_PIN_STEP_1 -> {
                    currentScreen = SCREEN_CHANGE_ADMIN_PIN_STEP_2
                    renderScreen()
                }

                else -> {
                }
            }
        }

        buttonSecondary.setOnClickListener {
        }
    }

    private fun renderScreen() {
        textError.visibility = View.GONE
        editPin.setText("")
        editPinSecond.setText("")

        when (currentScreen) {
            SCREEN_ENTER_PIN -> {
                textTitle.text = "Enter PIN"

                editPin.visibility = View.VISIBLE
                editPin.hint = "PIN"

                editPinSecond.visibility = View.GONE

                textForgot.visibility = View.GONE

                buttonPrimary.text = "Unlock"
                buttonSecondary.visibility = View.VISIBLE
                buttonSecondary.text = "Emergency Info"
            }

            SCREEN_CHANGE_PIN -> {
                textTitle.text = "Change PIN"

                editPin.visibility = View.VISIBLE
                editPin.hint = "Enter PIN"

                editPinSecond.visibility = View.VISIBLE
                editPinSecond.hint = "Confirm PIN"

                textForgot.visibility = View.GONE

                buttonPrimary.text = "Save PIN"
                buttonSecondary.visibility = View.GONE
            }

            SCREEN_SET_ADMIN_PIN -> {
                textTitle.text = "Set Admin PIN"

                editPin.visibility = View.VISIBLE
                editPin.hint = "Enter Admin PIN"

                editPinSecond.visibility = View.VISIBLE
                editPinSecond.hint = "Confirm Admin PIN"

                textForgot.visibility = View.GONE

                buttonPrimary.text = "Save Admin PIN"
                buttonSecondary.visibility = View.GONE
            }

            SCREEN_CHANGE_ADMIN_PIN_STEP_1 -> {
                textTitle.text = "Change Admin PIN"

                editPin.visibility = View.VISIBLE
                editPin.hint = "Current Admin PIN"

                editPinSecond.visibility = View.GONE

                textForgot.visibility = View.VISIBLE
                textForgot.text = "Forgot Admin PIN? Reset app"

                buttonPrimary.text = "Continue"
                buttonSecondary.visibility = View.GONE
            }

            SCREEN_CHANGE_ADMIN_PIN_STEP_2 -> {
                textTitle.text = "Change Admin PIN"

                editPin.visibility = View.VISIBLE
                editPin.hint = "New Admin PIN"

                editPinSecond.visibility = View.VISIBLE
                editPinSecond.hint = "Confirm Admin PIN"

                textForgot.visibility = View.GONE

                buttonPrimary.text = "Save Admin PIN"
                buttonSecondary.visibility = View.GONE
            }
        }
    }

    companion object {
        private const val SCREEN_ENTER_PIN = "screen_enter_pin"
        private const val SCREEN_CHANGE_PIN = "screen_change_pin"
        private const val SCREEN_SET_ADMIN_PIN = "screen_set_admin_pin"
        private const val SCREEN_CHANGE_ADMIN_PIN_STEP_1 = "screen_change_admin_pin_step_1"
        private const val SCREEN_CHANGE_ADMIN_PIN_STEP_2 = "screen_change_admin_pin_step_2"
    }
}