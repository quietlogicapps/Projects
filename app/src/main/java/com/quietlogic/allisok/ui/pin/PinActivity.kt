package com.quietlogic.allisok.ui.pin

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
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
import com.quietlogic.allisok.ui.info.InfoActivity

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
            getString(R.string.pin_title_change_pin) -> {
                if (pinPrefs.isUserPinEnabled()) {
                    SCREEN_CHANGE_PIN
                } else {
                    SCREEN_SET_PIN
                }
            }

            getString(R.string.pin_title_set_admin_pin) -> SCREEN_SET_ADMIN_PIN
            getString(R.string.pin_title_change_admin_pin) -> SCREEN_CHANGE_ADMIN_PIN_STEP_1
            else -> SCREEN_ENTER_PIN
        }

        setupInputs()
        renderScreen()

        buttonPrimary.setOnClickListener {
            when (currentScreen) {
                SCREEN_ENTER_PIN -> handleEnterPin()
                SCREEN_SET_PIN -> handleSetUserPin()
                SCREEN_CHANGE_PIN -> handleChangeUserPin()
                SCREEN_SET_ADMIN_PIN -> handleSetAdminPin()
                SCREEN_CHANGE_ADMIN_PIN_STEP_1 -> handleAdminPinStep1()
                SCREEN_CHANGE_ADMIN_PIN_STEP_2 -> handleAdminPinStep2()
            }
        }

        buttonSecondary.setOnClickListener {
            if (currentScreen == SCREEN_ENTER_PIN && unlockMode == LockGate.MODE_USER_UNLOCK) {
                if (callingActivity != null) {
                    setResult(RESULT_OPEN_EMERGENCY_INFO)
                    finish()
                } else {
                    startActivity(Intent(this, InfoActivity::class.java))
                }
            }
        }
    }

    private fun setupInputs() {

        editPin.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD
        editPinSecond.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD

        editPin.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val value = s?.toString().orEmpty()

                if (value.length > 4) {
                    editPin.setText(value.take(4))
                    editPin.setSelection(editPin.text.length)
                    return
                }

                if (value.length == 4 && editPinSecond.visibility == View.VISIBLE) {
                    editPinSecond.requestFocus()
                    editPinSecond.setSelection(editPinSecond.text.length)
                    showKeyboard(editPinSecond)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        editPinSecond.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val value = s?.toString().orEmpty()

                if (value.length > 4) {
                    editPinSecond.setText(value.take(4))
                    editPinSecond.setSelection(editPinSecond.text.length)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        editPin.setOnFocusChangeListener { _, hasFocus ->
            updateFieldHint(editPin, firstRowLabel(), hasFocus)
        }

        editPinSecond.setOnFocusChangeListener { _, hasFocus ->
            updateFieldHint(editPinSecond, secondRowLabel(), hasFocus)
        }
    }

    private fun renderScreen() {
        textError.visibility = View.GONE
        editPin.setText("")
        editPinSecond.setText("")

        when (currentScreen) {

            SCREEN_ENTER_PIN -> {
                textTitle.text =
                    if (unlockMode == LockGate.MODE_ADMIN_UNLOCK) {
                        getString(R.string.menu_enter_admin)
                    } else {
                        getString(R.string.pin_enter)
                    }

                editPin.visibility = View.VISIBLE
                editPinSecond.visibility = View.GONE

                textForgot.visibility = View.GONE

                buttonPrimary.text =
                    if (unlockMode == LockGate.MODE_ADMIN_UNLOCK) {
                        getString(R.string.pin_enter_button)
                    } else {
                        getString(R.string.pin_unlock)
                    }

                if (unlockMode == LockGate.MODE_ADMIN_UNLOCK) {
                    buttonSecondary.visibility = View.GONE
                } else {
                    buttonSecondary.visibility = View.VISIBLE
                    buttonSecondary.text = getString(R.string.pin_emergency_info)
                }

                editPin.hint = firstRowLabel()
                clearFocusAndHideKeyboard()
            }

            SCREEN_SET_PIN -> {
                textTitle.text = getString(R.string.pin_set)

                editPin.visibility = View.VISIBLE
                editPinSecond.visibility = View.VISIBLE

                textForgot.visibility = View.GONE

                buttonPrimary.text = getString(R.string.pin_save)
                buttonSecondary.visibility = View.GONE

                editPin.hint = firstRowLabel()
                editPinSecond.hint = secondRowLabel()
                clearFocusAndHideKeyboard()
            }

            SCREEN_CHANGE_PIN -> {
                textTitle.text = getString(R.string.pin_title_change_pin)

                editPin.visibility = View.VISIBLE
                editPinSecond.visibility = View.VISIBLE

                textForgot.visibility = View.GONE

                buttonPrimary.text = getString(R.string.pin_save)
                buttonSecondary.visibility = View.GONE

                editPin.hint = firstRowLabel()
                editPinSecond.hint = secondRowLabel()
                clearFocusAndHideKeyboard()
            }

            SCREEN_SET_ADMIN_PIN -> {
                textTitle.text = getString(R.string.pin_title_set_admin_pin)

                editPin.visibility = View.VISIBLE
                editPinSecond.visibility = View.VISIBLE

                textForgot.visibility = View.GONE

                buttonPrimary.text = getString(R.string.pin_save_admin)
                buttonSecondary.visibility = View.GONE

                editPin.hint = firstRowLabel()
                editPinSecond.hint = secondRowLabel()
                clearFocusAndHideKeyboard()
            }

            SCREEN_CHANGE_ADMIN_PIN_STEP_1 -> {
                textTitle.text = getString(R.string.pin_title_change_admin_pin)

                editPin.visibility = View.VISIBLE
                editPinSecond.visibility = View.GONE

                textForgot.visibility = View.VISIBLE
                textForgot.text = getString(R.string.pin_forgot_admin)

                buttonPrimary.text = getString(R.string.pin_continue)
                buttonSecondary.visibility = View.GONE

                editPin.hint = firstRowLabel()
                clearFocusAndHideKeyboard()
            }

            SCREEN_CHANGE_ADMIN_PIN_STEP_2 -> {
                textTitle.text = getString(R.string.pin_title_change_admin_pin)

                editPin.visibility = View.VISIBLE
                editPinSecond.visibility = View.VISIBLE

                textForgot.visibility = View.GONE

                buttonPrimary.text = getString(R.string.pin_save_admin)
                buttonSecondary.visibility = View.GONE

                editPin.hint = firstRowLabel()
                editPinSecond.hint = secondRowLabel()
                clearFocusAndHideKeyboard()
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
                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

            startActivity(homeIntent)
            finish()

        } else {
            if (unlockMode == LockGate.MODE_ADMIN_UNLOCK) {
                showError(getString(R.string.pin_error_wrong_admin))
            } else {
                showError(getString(R.string.pin_error_wrong))
            }
        }
    }

    private fun handleSetUserPin() {

        val pin = editPin.text.toString().trim()
        val confirmPin = editPinSecond.text.toString().trim()
        val state = pinPrefs.getState()

        if (!PinValidator.isValidFormat(pin)) {
            showError(getString(R.string.pin_error_format))
            return
        }

        if (pin != confirmPin) {
            showError(getString(R.string.pin_error_mismatch))
            return
        }

        if (!PinValidator.isDifferentFromAdmin(pin, state.adminPinHash)) {
            showError(getString(R.string.pin_error_same))
            return
        }

        pinPrefs.setUserPin(PinHasher.hash(pin))
        LockGate.markUserUnlocked()
        setResult(Activity.RESULT_OK)
        finish()
    }

    private fun handleChangeUserPin() {

        val pin = editPin.text.toString().trim()
        val confirmPin = editPinSecond.text.toString().trim()
        val state = pinPrefs.getState()

        if (!PinValidator.isValidFormat(pin)) {
            showError(getString(R.string.pin_error_format))
            return
        }

        if (pin != confirmPin) {
            showError(getString(R.string.pin_error_mismatch))
            return
        }

        if (!PinValidator.isDifferentFromAdmin(pin, state.adminPinHash)) {
            showError(getString(R.string.pin_error_same))
            return
        }

        pinPrefs.setUserPin(PinHasher.hash(pin))
        LockGate.markUserUnlocked()
        setResult(Activity.RESULT_OK)
        finish()
    }

    private fun handleSetAdminPin() {

        val pin = editPin.text.toString().trim()
        val confirmPin = editPinSecond.text.toString().trim()
        val state = pinPrefs.getState()

        if (!PinValidator.isValidFormat(pin)) {
            showError(getString(R.string.pin_error_format_admin))
            return
        }

        if (pin != confirmPin) {
            showError(getString(R.string.pin_error_mismatch))
            return
        }

        if (!PinValidator.isDifferentFromUser(pin, state.userPinHash)) {
            showError(getString(R.string.pin_error_same))
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
            showError(getString(R.string.pin_error_wrong_admin))
        }
    }

    private fun handleAdminPinStep2() {

        val pin = editPin.text.toString().trim()
        val confirmPin = editPinSecond.text.toString().trim()
        val state = pinPrefs.getState()

        if (!PinValidator.isValidFormat(pin)) {
            showError(getString(R.string.pin_error_format_admin))
            return
        }

        if (pin != confirmPin) {
            showError(getString(R.string.pin_error_mismatch))
            return
        }

        if (!PinValidator.isDifferentFromUser(pin, state.userPinHash)) {
            showError(getString(R.string.pin_error_same))
            return
        }

        pinPrefs.setAdminPin(PinHasher.hash(pin))
        AdminSession.start()
        setResult(Activity.RESULT_OK)
        finish()
    }

    private fun updateFieldHint(field: EditText, label: String, hasFocus: Boolean) {
        field.hint = if (hasFocus && field.text.isNullOrEmpty()) "- - - -" else label
    }

    private fun firstRowLabel(): String {
        return when (currentScreen) {
            SCREEN_ENTER_PIN -> {
                if (unlockMode == LockGate.MODE_ADMIN_UNLOCK) {
                    getString(R.string.pin_label_admin)
                } else {
                    getString(R.string.pin_label_pin)
                }
            }

            SCREEN_SET_PIN -> getString(R.string.pin_hint_enter)
            SCREEN_CHANGE_PIN -> getString(R.string.pin_hint_enter)
            SCREEN_SET_ADMIN_PIN -> getString(R.string.pin_hint_enter_admin)
            SCREEN_CHANGE_ADMIN_PIN_STEP_1 -> getString(R.string.pin_hint_current_admin)
            SCREEN_CHANGE_ADMIN_PIN_STEP_2 -> getString(R.string.pin_hint_new_admin)
            else -> getString(R.string.pin_label_pin)
        }
    }

    private fun secondRowLabel(): String {
        return when (currentScreen) {
            SCREEN_SET_PIN -> getString(R.string.pin_hint_confirm)
            SCREEN_CHANGE_PIN -> getString(R.string.pin_hint_confirm)
            SCREEN_SET_ADMIN_PIN -> getString(R.string.pin_hint_confirm_admin)
            SCREEN_CHANGE_ADMIN_PIN_STEP_2 -> getString(R.string.pin_hint_confirm_admin)
            else -> getString(R.string.pin_hint_confirm)
        }
    }

    private fun clearFocusAndHideKeyboard() {
        editPin.clearFocus()
        editPinSecond.clearFocus()
        hideKeyboard()
    }

    private fun showKeyboard(view: View) {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        val token = currentFocus?.windowToken ?: return
        imm.hideSoftInputFromWindow(token, 0)
    }

    private fun showError(message: String) {
        textError.text = message
        textError.visibility = View.VISIBLE
    }

    companion object {
        const val RESULT_OPEN_EMERGENCY_INFO = 1002

        private const val SCREEN_ENTER_PIN = "screen_enter_pin"
        private const val SCREEN_SET_PIN = "screen_set_pin"
        private const val SCREEN_CHANGE_PIN = "screen_change_pin"
        private const val SCREEN_SET_ADMIN_PIN = "screen_set_admin_pin"
        private const val SCREEN_CHANGE_ADMIN_PIN_STEP_1 = "screen_change_admin_pin_step_1"
        private const val SCREEN_CHANGE_ADMIN_PIN_STEP_2 = "screen_change_admin_pin_step_2"
    }
}