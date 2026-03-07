package com.quietlogic.allisok.ui.pin

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.quietlogic.allisok.R
import com.quietlogic.allisok.security.AdminSession
import com.quietlogic.allisok.security.LockGate
import com.quietlogic.allisok.security.PinHasher
import com.quietlogic.allisok.security.PinPrefs
import com.quietlogic.allisok.security.PinValidator
import com.quietlogic.allisok.ui.home.HomeActivity

class PinActivity : AppCompatActivity() {

    private lateinit var textTitle: TextView
    private lateinit var editPin: EditText
    private lateinit var editPinSecond: EditText
    private lateinit var textForgot: TextView
    private lateinit var textError: TextView
    private lateinit var buttonPrimary: Button
    private lateinit var buttonSecondary: Button

    private lateinit var pinPrefs: PinPrefs

    private var currentScreen: String = SCREEN_ENTER_PIN
    private var unlockMode: String = LockGate.MODE_USER_UNLOCK

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pin)

        pinPrefs = PinPrefs(this)

        textTitle = findViewById(R.id.textTitle)
        editPin = findViewById(R.id.editPin)
        editPinSecond = findViewById(R.id.editPinSecond)
        textForgot = findViewById(R.id.textForgot)
        textError = findViewById(R.id.textError)
        buttonPrimary = findViewById(R.id.buttonPrimary)
        buttonSecondary = findViewById(R.id.buttonSecondary)

        unlockMode = intent.getStringExtra("mode") ?: LockGate.MODE_USER_UNLOCK

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
                SCREEN_ENTER_PIN -> handleEnterPin()
                SCREEN_CHANGE_PIN -> handleChangeUserPin()
                SCREEN_SET_ADMIN_PIN -> handleSetAdminPin()
                SCREEN_CHANGE_ADMIN_PIN_STEP_1 -> handleAdminPinStep1()
                SCREEN_CHANGE_ADMIN_PIN_STEP_2 -> handleAdminPinStep2()
            }
        }

        buttonSecondary.setOnClickListener {
            if (currentScreen == SCREEN_ENTER_PIN && unlockMode == LockGate.MODE_USER_UNLOCK) {
                setResult(RESULT_OPEN_EMERGENCY_INFO)
                finish()
            }
        }
    }

    private fun renderScreen() {
        textError.visibility = View.GONE
        editPin.setText("")
        editPinSecond.setText("")

        when (currentScreen) {

            SCREEN_ENTER_PIN -> {
                if (unlockMode == LockGate.MODE_ADMIN_UNLOCK) {
                    textTitle.text = "Enter with Admin PIN"
                } else {
                    textTitle.text = "Enter PIN"
                }

                editPin.visibility = View.VISIBLE
                editPin.hint =
                    if (unlockMode == LockGate.MODE_ADMIN_UNLOCK) {
                        "Admin PIN"
                    } else {
                        "PIN"
                    }

                editPinSecond.visibility = View.GONE

                textForgot.visibility = View.GONE

                buttonPrimary.text =
                    if (unlockMode == LockGate.MODE_ADMIN_UNLOCK) {
                        "Enter"
                    } else {
                        "Unlock"
                    }

                if (unlockMode == LockGate.MODE_ADMIN_UNLOCK) {
                    buttonSecondary.visibility = View.GONE
                } else {
                    buttonSecondary.visibility = View.VISIBLE
                    buttonSecondary.text = "Emergency Info"
                }
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

    private fun handleEnterPin() {

        val inputPin = editPin.text.toString().trim()
        val state = pinPrefs.getState()

        val ok = if (unlockMode == LockGate.MODE_ADMIN_UNLOCK) {
            PinHasher.verify(inputPin, state.adminPinHash)
        } else {
            PinHasher.verify(inputPin, state.userPinHash)
        }

        if (ok) {

            if (unlockMode == LockGate.MODE_ADMIN_UNLOCK) {
                AdminSession.start()
                setResult(Activity.RESULT_OK)
                finish()
                return
            }

            LockGate.markUserUnlocked()

            val homeIntent = Intent(this, HomeActivity::class.java)
            homeIntent.flags =
                Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TASK

            startActivity(homeIntent)
            finish()

        } else {
            showError("Wrong PIN")
        }
    }

    private fun handleChangeUserPin() {

        val pin = editPin.text.toString().trim()
        val confirmPin = editPinSecond.text.toString().trim()
        val state = pinPrefs.getState()

        if (!PinValidator.isValidFormat(pin)) {
            showError("PIN must be 4 digits")
            return
        }

        if (pin != confirmPin) {
            showError("PINs do not match")
            return
        }

        if (!PinValidator.isDifferentFromAdmin(pin, state.adminPinHash)) {
            showError("PIN must be different")
            return
        }

        pinPrefs.setUserPin(PinHasher.hash(pin))

        setResult(Activity.RESULT_OK)
        finish()
    }

    private fun handleSetAdminPin() {

        val pin = editPin.text.toString().trim()
        val confirmPin = editPinSecond.text.toString().trim()
        val state = pinPrefs.getState()

        if (!PinValidator.isValidFormat(pin)) {
            showError("Admin PIN must be 4 digits")
            return
        }

        if (pin != confirmPin) {
            showError("PINs do not match")
            return
        }

        if (!PinValidator.isDifferentFromUser(pin, state.userPinHash)) {
            showError("PIN must be different")
            return
        }

        pinPrefs.setAdminPin(PinHasher.hash(pin))

        AdminSession.start()

        setResult(Activity.RESULT_OK)
        finish()
    }

    private fun handleAdminPinStep1() {

        val currentAdminPin = editPin.text.toString().trim()
        val state = pinPrefs.getState()

        if (PinHasher.verify(currentAdminPin, state.adminPinHash)) {

            currentScreen = SCREEN_CHANGE_ADMIN_PIN_STEP_2
            renderScreen()

        } else {
            showError("Wrong Admin PIN")
        }
    }

    private fun handleAdminPinStep2() {

        val pin = editPin.text.toString().trim()
        val confirmPin = editPinSecond.text.toString().trim()
        val state = pinPrefs.getState()

        if (!PinValidator.isValidFormat(pin)) {
            showError("Admin PIN must be 4 digits")
            return
        }

        if (pin != confirmPin) {
            showError("PINs do not match")
            return
        }

        if (!PinValidator.isDifferentFromUser(pin, state.userPinHash)) {
            showError("PIN must be different")
            return
        }

        pinPrefs.setAdminPin(PinHasher.hash(pin))

        AdminSession.start()

        setResult(Activity.RESULT_OK)
        finish()
    }

    private fun showError(message: String) {
        textError.text = message
        textError.visibility = View.VISIBLE
    }

    companion object {

        const val RESULT_OPEN_EMERGENCY_INFO = 1002

        private const val SCREEN_ENTER_PIN = "screen_enter_pin"
        private const val SCREEN_CHANGE_PIN = "screen_change_pin"
        private const val SCREEN_SET_ADMIN_PIN = "screen_set_admin_pin"
        private const val SCREEN_CHANGE_ADMIN_PIN_STEP_1 = "screen_change_admin_pin_step_1"
        private const val SCREEN_CHANGE_ADMIN_PIN_STEP_2 = "screen_change_admin_pin_step_2"
    }
}