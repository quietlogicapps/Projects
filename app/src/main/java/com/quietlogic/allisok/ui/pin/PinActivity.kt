package com.quietlogic.allisok.ui.pin

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import com.google.android.material.button.MaterialButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.quietlogic.allisok.R
import com.quietlogic.allisok.security.LockGate
import com.quietlogic.allisok.security.PinPrefs
import com.quietlogic.allisok.ui.home.HomeActivity
import com.quietlogic.allisok.ui.info.InfoActivity

class PinActivity : AppCompatActivity() {

    private lateinit var textTitle: TextView
    private lateinit var editPin: EditText
    private lateinit var editPinSecond: EditText
    private lateinit var textForgot: TextView
    private lateinit var textError: TextView
    private lateinit var buttonPrimary: MaterialButton
    private lateinit var buttonSecondary: MaterialButton

    private lateinit var pinPrefs: PinPrefs
    private lateinit var keyboardHelper: PinKeyboardHelper
    private lateinit var screenRenderer: PinScreenRenderer
    private lateinit var inputController: PinInputController
    private lateinit var actionExecutor: PinActionExecutor

    private var currentScreen: String = SCREEN_ENTER_PIN
    private var unlockMode: String = LockGate.MODE_USER_UNLOCK

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intentMode = intent.getStringExtra("mode")
        val intentTitle = intent.getStringExtra("PIN_TITLE")
        if (intentMode == LockGate.MODE_USER_UNLOCK && intentTitle.isNullOrEmpty()) {
            setContentView(R.layout.activity_pin_enter)
        } else {
            setContentView(R.layout.activity_pin)
        }

        pinPrefs = PinPrefs(this)
        actionExecutor = PinActionExecutor(pinPrefs, this)

        textTitle = findViewById(R.id.textTitle)
        editPin = findViewById(R.id.editPin)
        editPinSecond = findViewById(R.id.editPinSecond)
        textForgot = findViewById(R.id.textForgot)
        textError = findViewById(R.id.textError)
        buttonPrimary = findViewById(R.id.buttonPrimary)
        buttonSecondary = findViewById(R.id.buttonSecondary)

        val keyboardHelper = PinKeyboardHelper(
            activity = this,
            editPin = editPin,
            editPinSecond = editPinSecond
        )
        this.keyboardHelper = keyboardHelper

        screenRenderer = PinScreenRenderer(
            activity = this,
            textTitle = textTitle,
            editPin = editPin,
            editPinSecond = editPinSecond,
            textForgot = textForgot,
            textError = textError,
            buttonPrimary = buttonPrimary,
            buttonSecondary = buttonSecondary,
            keyboardHelper = keyboardHelper
        )

        inputController = PinInputController(
            editPin = editPin,
            editPinSecond = editPinSecond,
            keyboardHelper = keyboardHelper,
            screenRenderer = screenRenderer,
            currentScreenProvider = { currentScreen },
            unlockModeProvider = { unlockMode }
        )

        if (intentMode == LockGate.MODE_USER_UNLOCK && intentTitle.isNullOrEmpty()) {
            keyboardHelper.attachCardPinImeInsets(findViewById(R.id.cardPin))
        }

        unlockMode = intentMode ?: LockGate.MODE_USER_UNLOCK

        val titleFromIntent = intentTitle.orEmpty()

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

        if (currentScreen == SCREEN_SET_ADMIN_PIN && pinPrefs.getState().adminPinEnabled) {
            finish()
            return
        }

        inputController.setupInputs()
        screenRenderer.renderScreen(currentScreen, unlockMode)

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

    private fun handleEnterPin() {
        when (val result = actionExecutor.enterPin(editPin.text.toString().trim(), unlockMode)) {
            PinActionResult.AdminUnlockSuccess -> {
                setResult(Activity.RESULT_OK)
                finish()
            }

            PinActionResult.UserUnlockSuccess -> {
                val homeIntent = Intent(this, HomeActivity::class.java)
                homeIntent.flags =
                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(homeIntent)
                finish()
            }

            is PinActionResult.WrongPin -> {
                screenRenderer.showError(getString(result.messageResId))
                if (result.retryFirstField) {
                    editPin.text.clear()
                    editPin.hint = "— — — —"
                    editPin.requestFocus()
                }
            }

            else -> Unit
        }
    }

    private fun handleSetUserPin() {
        when (val result = actionExecutor.setUserPin(
            editPin.text.toString().trim(),
            editPinSecond.text.toString().trim()
        )) {
            is PinActionResult.ValidationError -> {
                screenRenderer.showError(getString(result.messageResId))
            }

            PinActionResult.UserPinSaveSuccess -> {
                setResult(Activity.RESULT_OK)
                finish()
            }

            else -> Unit
        }
    }

    private fun handleChangeUserPin() {
        when (val result = actionExecutor.changeUserPin(
            editPin.text.toString().trim(),
            editPinSecond.text.toString().trim()
        )) {
            is PinActionResult.ValidationError -> {
                screenRenderer.showError(getString(result.messageResId))
            }

            PinActionResult.UserPinSaveSuccess -> {
                setResult(Activity.RESULT_OK)
                finish()
            }

            else -> Unit
        }
    }

    private fun handleSetAdminPin() {
        when (val result = actionExecutor.setAdminPin(
            editPin.text.toString().trim(),
            editPinSecond.text.toString().trim()
        )) {
            is PinActionResult.ValidationError -> {
                screenRenderer.showError(getString(result.messageResId))
            }

            PinActionResult.AdminPinSaveSuccess -> {
                setResult(Activity.RESULT_OK)
                finish()
            }

            PinActionResult.AdminPinSetupBlocked -> finish()

            else -> Unit
        }
    }

    private fun handleAdminPinStep1() {
        when (val result = actionExecutor.verifyAdminPinStep1(editPin.text.toString().trim())) {
            is PinActionResult.GoToNextScreen -> {
                currentScreen = result.nextScreen
                screenRenderer.renderScreen(currentScreen, unlockMode)
            }

            is PinActionResult.WrongPin -> {
                screenRenderer.showError(getString(result.messageResId))
            }

            else -> Unit
        }
    }

    private fun handleAdminPinStep2() {
        when (val result = actionExecutor.changeAdminPinStep2(
            editPin.text.toString().trim(),
            editPinSecond.text.toString().trim()
        )) {
            is PinActionResult.ValidationError -> {
                screenRenderer.showError(getString(result.messageResId))
            }

            PinActionResult.AdminPinSaveSuccess -> {
                setResult(Activity.RESULT_OK)
                finish()
            }

            else -> Unit
        }
    }

    companion object {
        const val RESULT_OPEN_EMERGENCY_INFO = 1002

        internal const val SCREEN_ENTER_PIN = "screen_enter_pin"
        internal const val SCREEN_SET_PIN = "screen_set_pin"
        internal const val SCREEN_CHANGE_PIN = "screen_change_pin"
        internal const val SCREEN_SET_ADMIN_PIN = "screen_set_admin_pin"
        internal const val SCREEN_CHANGE_ADMIN_PIN_STEP_1 = "screen_change_admin_pin_step_1"
        internal const val SCREEN_CHANGE_ADMIN_PIN_STEP_2 = "screen_change_admin_pin_step_2"
    }
}
