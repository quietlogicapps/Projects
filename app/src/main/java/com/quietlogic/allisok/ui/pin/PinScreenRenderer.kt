package com.quietlogic.allisok.ui.pin

import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.quietlogic.allisok.R
import com.quietlogic.allisok.security.LockGate

class PinScreenRenderer(
    private val activity: AppCompatActivity,
    private val textTitle: TextView,
    private val editPin: EditText,
    private val editPinSecond: EditText,
    private val textForgot: TextView,
    private val textError: TextView,
    private val buttonPrimary: MaterialButton,
    private val buttonSecondary: MaterialButton,
    private val keyboardHelper: PinKeyboardHelper
) {

    fun renderScreen(currentScreen: String, unlockMode: String) {
        textError.visibility = View.GONE
        editPin.setText("")
        editPinSecond.setText("")

        when (currentScreen) {
            PinActivity.SCREEN_ENTER_PIN -> {
                textTitle.text =
                    if (unlockMode == LockGate.MODE_ADMIN_UNLOCK) {
                        activity.getString(R.string.menu_enter_admin)
                    } else {
                        "Enter"
                    }

                editPin.visibility = View.VISIBLE
                editPinSecond.visibility = View.GONE

                textForgot.visibility = View.GONE

                buttonPrimary.text =
                    if (unlockMode == LockGate.MODE_ADMIN_UNLOCK) {
                        activity.getString(R.string.pin_enter_button)
                    } else {
                        activity.getString(R.string.pin_unlock)
                    }

                if (unlockMode == LockGate.MODE_ADMIN_UNLOCK) {
                    buttonSecondary.visibility = View.GONE
                } else {
                    buttonSecondary.visibility = View.VISIBLE
                    buttonSecondary.text = activity.getString(R.string.pin_emergency_info)
                }

                editPin.hint = firstRowLabel(currentScreen, unlockMode)
                keyboardHelper.clearFocusAndHideKeyboard()
            }

            PinActivity.SCREEN_SET_PIN -> {
                textTitle.text = activity.getString(R.string.pin_set)

                editPin.visibility = View.VISIBLE
                editPinSecond.visibility = View.VISIBLE

                textForgot.visibility = View.GONE

                buttonPrimary.text = activity.getString(R.string.pin_save)
                buttonSecondary.visibility = View.GONE

                editPin.hint = firstRowLabel(currentScreen, unlockMode)
                editPinSecond.hint = secondRowLabel(currentScreen)
                keyboardHelper.clearFocusAndHideKeyboard()
            }

            PinActivity.SCREEN_CHANGE_PIN -> {
                textTitle.text = activity.getString(R.string.pin_title_change_pin)

                editPin.visibility = View.VISIBLE
                editPinSecond.visibility = View.VISIBLE

                textForgot.visibility = View.GONE

                buttonPrimary.text = activity.getString(R.string.pin_save)
                buttonSecondary.visibility = View.GONE

                editPin.hint = firstRowLabel(currentScreen, unlockMode)
                editPinSecond.hint = secondRowLabel(currentScreen)
                keyboardHelper.clearFocusAndHideKeyboard()
            }

            PinActivity.SCREEN_SET_ADMIN_PIN -> {
                textTitle.text = activity.getString(R.string.pin_title_set_admin_pin)

                editPin.visibility = View.VISIBLE
                editPinSecond.visibility = View.VISIBLE

                textForgot.visibility = View.GONE

                buttonPrimary.text = activity.getString(R.string.pin_save_admin)
                buttonSecondary.visibility = View.GONE

                editPin.hint = firstRowLabel(currentScreen, unlockMode)
                editPinSecond.hint = secondRowLabel(currentScreen)
                keyboardHelper.clearFocusAndHideKeyboard()
            }

            PinActivity.SCREEN_CHANGE_ADMIN_PIN_STEP_1 -> {
                textTitle.text = activity.getString(R.string.pin_title_change_admin_pin)

                editPin.visibility = View.VISIBLE
                editPinSecond.visibility = View.GONE

                textForgot.visibility = View.VISIBLE
                textForgot.text = activity.getString(R.string.pin_forgot_admin)

                buttonPrimary.text = activity.getString(R.string.pin_continue)
                buttonSecondary.visibility = View.GONE

                editPin.hint = firstRowLabel(currentScreen, unlockMode)
                keyboardHelper.clearFocusAndHideKeyboard()
            }

            PinActivity.SCREEN_CHANGE_ADMIN_PIN_STEP_2 -> {
                textTitle.text = activity.getString(R.string.pin_title_change_admin_pin)

                editPin.visibility = View.VISIBLE
                editPinSecond.visibility = View.VISIBLE

                textForgot.visibility = View.GONE

                buttonPrimary.text = activity.getString(R.string.pin_save_admin)
                buttonSecondary.visibility = View.GONE

                editPin.hint = firstRowLabel(currentScreen, unlockMode)
                editPinSecond.hint = secondRowLabel(currentScreen)
                keyboardHelper.clearFocusAndHideKeyboard()
            }
        }
    }

    fun showError(message: String) {
        textError.text = message
        textError.visibility = View.VISIBLE
    }

    fun firstRowLabel(currentScreen: String, unlockMode: String): String {
        return when (currentScreen) {
            PinActivity.SCREEN_ENTER_PIN -> {
                if (unlockMode == LockGate.MODE_ADMIN_UNLOCK) {
                    activity.getString(R.string.pin_label_admin)
                } else {
                    activity.getString(R.string.pin_label_pin)
                }
            }

            PinActivity.SCREEN_SET_PIN -> activity.getString(R.string.pin_hint_enter)
            PinActivity.SCREEN_CHANGE_PIN -> activity.getString(R.string.pin_hint_enter)
            PinActivity.SCREEN_SET_ADMIN_PIN -> activity.getString(R.string.pin_hint_enter_admin)
            PinActivity.SCREEN_CHANGE_ADMIN_PIN_STEP_1 -> activity.getString(R.string.pin_hint_current_admin)
            PinActivity.SCREEN_CHANGE_ADMIN_PIN_STEP_2 -> activity.getString(R.string.pin_hint_new_admin)
            else -> activity.getString(R.string.pin_label_pin)
        }
    }

    fun secondRowLabel(currentScreen: String): String {
        return when (currentScreen) {
            PinActivity.SCREEN_SET_PIN -> activity.getString(R.string.pin_hint_confirm)
            PinActivity.SCREEN_CHANGE_PIN -> activity.getString(R.string.pin_hint_confirm)
            PinActivity.SCREEN_SET_ADMIN_PIN -> activity.getString(R.string.pin_hint_confirm_admin)
            PinActivity.SCREEN_CHANGE_ADMIN_PIN_STEP_2 -> activity.getString(R.string.pin_hint_confirm_admin)
            else -> activity.getString(R.string.pin_hint_confirm)
        }
    }
}
