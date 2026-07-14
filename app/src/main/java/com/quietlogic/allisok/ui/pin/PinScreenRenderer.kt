package com.quietlogic.allisok.ui.pin

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.text.InputType
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
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

    private var editPinVisible = false
    private var editPinSecondVisible = false

    fun renderScreen(currentScreen: String, unlockMode: String) {
        textError.visibility = View.GONE
        editPin.setText("")
        editPinSecond.setText("")
        resetSetPinVisualOverrides()

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

            PinActivity.SCREEN_SET_PIN,
            PinActivity.SCREEN_CHANGE_PIN -> {
                applySetPinVisualOverrides(
                    if (currentScreen == PinActivity.SCREEN_SET_PIN) {
                        "SET PIN"
                    } else {
                        "CHANGE PIN"
                    }
                )

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
                applySetPinVisualOverrides(activity.getString(R.string.pin_title_set_admin_pin))

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

    private fun pinRootLayout(): LinearLayout? {
        val parent = textTitle.parent as? LinearLayout ?: return null
        return if (parent.id == R.id.cardPin) null else parent
    }

    private fun activityPinCardLayout(): LinearLayout? {
        if (pinRootLayout() == null) return null
        val card = editPin.parent as? LinearLayout ?: return null
        return if (card.id == R.id.cardPin) card else null
    }

    private fun dp(value: Int): Int =
        (value * activity.resources.displayMetrics.density).toInt()

    private fun availablePinContentWidth(root: LinearLayout): Int {
        val measured = root.width - root.paddingLeft - root.paddingRight
        if (measured > 0) return measured
        return activity.resources.displayMetrics.widthPixels - dp(48)
    }

    private fun applySetPinVisualOverrides(title: String) {
        pinRootLayout()?.setBackgroundColor(Color.parseColor("#000000"))

        textTitle.text = title
        textTitle.setTextColor(Color.parseColor("#FFFFFF"))
        textTitle.setTypeface(textTitle.typeface, Typeface.BOLD)
        textTitle.textSize = 22f
        textTitle.gravity = Gravity.CENTER_HORIZONTAL

        val titleParams = textTitle.layoutParams as LinearLayout.LayoutParams
        titleParams.width = LinearLayout.LayoutParams.MATCH_PARENT
        titleParams.topMargin = dp(5)
        titleParams.gravity = Gravity.CENTER_HORIZONTAL
        textTitle.layoutParams = titleParams

        val root = pinRootLayout() ?: return
        val card = activityPinCardLayout() ?: return
        val cardParams = card.layoutParams as LinearLayout.LayoutParams
        cardParams.width = (availablePinContentWidth(root) * 0.8f).toInt()
        cardParams.height = dp(305)
        cardParams.gravity = Gravity.CENTER_HORIZONTAL
        card.layoutParams = cardParams
        card.setPadding(dp(20), dp(26), dp(20), dp(26))
        card.setBackgroundResource(R.drawable.bg_pin_set_card)

        applySetPinFieldColors()
        applySetPinLockToggles()
        applySetPinPrimaryButtonStyle()
    }

    private fun applySetPinFieldColors() {
        if (activityPinCardLayout() == null) return

        val white = Color.parseColor("#FFFFFF")
        val white70 = Color.parseColor("#B3FFFFFF")
        listOf(editPin, editPinSecond).forEach { field ->
            field.setTextColor(white)
            field.setHintTextColor(white70)
            field.backgroundTintList = ColorStateList.valueOf(white70)
        }
    }

    private fun resetSetPinFieldColors() {
        if (activityPinCardLayout() == null) return

        val textColor = Color.parseColor("#111111")
        val hintColor = Color.parseColor("#6B7280")
        listOf(editPin, editPinSecond).forEach { field ->
            field.setTextColor(textColor)
            field.setHintTextColor(hintColor)
            field.backgroundTintList = ColorStateList.valueOf(textColor)
        }
    }

    private fun applySetPinPrimaryButtonStyle() {
        if (activityPinCardLayout() == null) return

        val buttonParams = buttonPrimary.layoutParams as LinearLayout.LayoutParams
        buttonParams.width = LinearLayout.LayoutParams.MATCH_PARENT
        buttonParams.height = dp(64)
        buttonParams.topMargin = dp(64)
        buttonPrimary.layoutParams = buttonParams
        buttonPrimary.cornerRadius = dp(18)
        buttonPrimary.elevation = 0f
        buttonPrimary.translationZ = 0f
        buttonPrimary.stateListAnimator = null
        buttonPrimary.insetTop = 0
        buttonPrimary.insetBottom = 0
        buttonPrimary.rippleColor = ColorStateList.valueOf(Color.parseColor("#33FFFFFF"))
        buttonPrimary.setTextColor(Color.parseColor("#FFFFFF"))
        buttonPrimary.textSize = 18f
        buttonPrimary.setTypeface(buttonPrimary.typeface, Typeface.BOLD)
        buttonPrimary.isAllCaps = false
        buttonPrimary.gravity = Gravity.CENTER
    }

    private fun resetSetPinPrimaryButtonStyle() {
        if (activityPinCardLayout() == null) return

        val buttonParams = buttonPrimary.layoutParams as LinearLayout.LayoutParams
        buttonParams.width = LinearLayout.LayoutParams.MATCH_PARENT
        buttonParams.height = dp(64)
        buttonParams.topMargin = dp(24)
        buttonPrimary.layoutParams = buttonParams
        buttonPrimary.backgroundTintList = ColorStateList.valueOf(
            ContextCompat.getColor(activity, R.color.expirely_primary)
        )
        buttonPrimary.cornerRadius = dp(4)
        buttonPrimary.elevation = 0f
        buttonPrimary.translationZ = 0f
        buttonPrimary.stateListAnimator = null
        buttonPrimary.insetTop = 0
        buttonPrimary.insetBottom = 0
        buttonPrimary.rippleColor = null
        buttonPrimary.setTextColor(ContextCompat.getColor(activity, R.color.expirely_onPrimary))
        buttonPrimary.textSize = 18f
        buttonPrimary.setTypeface(buttonPrimary.typeface, Typeface.BOLD)
        buttonPrimary.isAllCaps = false
        buttonPrimary.gravity = Gravity.CENTER
    }

    private fun resetSetPinVisualOverrides() {
        pinRootLayout()?.setBackgroundResource(R.drawable.bg_home_gradient)

        textTitle.setTextColor(ContextCompat.getColor(activity, R.color.expirely_primary))
        textTitle.setTypeface(textTitle.typeface, Typeface.BOLD)
        textTitle.textSize = 22f
        textTitle.gravity = Gravity.NO_GRAVITY

        val titleParams = textTitle.layoutParams as LinearLayout.LayoutParams
        titleParams.width = LinearLayout.LayoutParams.WRAP_CONTENT
        titleParams.topMargin = 0
        titleParams.gravity = Gravity.NO_GRAVITY
        textTitle.layoutParams = titleParams

        val card = activityPinCardLayout() ?: return
        val cardParams = card.layoutParams as LinearLayout.LayoutParams
        cardParams.width = LinearLayout.LayoutParams.MATCH_PARENT
        cardParams.height = LinearLayout.LayoutParams.WRAP_CONTENT
        cardParams.topMargin = dp(40)
        cardParams.gravity = Gravity.NO_GRAVITY
        card.layoutParams = cardParams
        card.setPadding(dp(24), dp(24), dp(24), dp(24))
        card.setBackgroundResource(R.drawable.bg_form_card)

        resetSetPinFieldColors()
        resetSetPinLockToggles()
        resetSetPinPrimaryButtonStyle()
    }

    private fun applySetPinLockToggles() {
        if (activityPinCardLayout() == null) return

        editPinVisible = false
        editPinSecondVisible = false

        setupPinFieldLock(editPin) {
            editPinVisible = !editPinVisible
            setPinFieldVisibility(editPin, editPinVisible)
        }
        setupPinFieldLock(editPinSecond) {
            editPinSecondVisible = !editPinSecondVisible
            setPinFieldVisibility(editPinSecond, editPinSecondVisible)
        }

        setPinFieldVisibility(editPin, false)
        setPinFieldVisibility(editPinSecond, false)
    }

    private fun setupPinFieldLock(field: EditText, onToggle: () -> Unit) {
        field.compoundDrawablePadding = dp(12)
        field.setOnTouchListener(createLockTouchListener(field, onToggle))
    }

    private fun createLockTouchListener(
        field: EditText,
        onToggle: () -> Unit
    ): View.OnTouchListener {
        return View.OnTouchListener { _, event ->
            if (event.action != MotionEvent.ACTION_UP) {
                return@OnTouchListener false
            }

            val drawable = field.compoundDrawablesRelative[2] ?: return@OnTouchListener false
            val drawableWidth = if (drawable.bounds.width() > 0) {
                drawable.bounds.width()
            } else {
                dp(24)
            }
            val touchableStart = field.width - field.paddingEnd - field.compoundDrawablePadding - drawableWidth
            if (event.x >= touchableStart) {
                onToggle()
                return@OnTouchListener true
            }

            false
        }
    }

    private fun setPinFieldVisibility(field: EditText, visible: Boolean) {
        val typeface = field.typeface
        val selectionStart = field.selectionStart
        val selectionEnd = field.selectionEnd

        field.inputType = if (visible) {
            InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_NORMAL
        } else {
            InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD
        }
        field.typeface = typeface
        setLockDrawable(field, visible)

        val length = field.text?.length ?: 0
        val safeStart = selectionStart.coerceIn(0, length)
        val safeEnd = selectionEnd.coerceIn(0, length)
        field.setSelection(safeStart, safeEnd)
    }

    private fun setLockDrawable(field: EditText, isOpen: Boolean) {
        val iconRes = if (isOpen) R.drawable.ic_lock_open else R.drawable.ic_lock
        val drawable = ContextCompat.getDrawable(activity, iconRes)?.mutate() ?: return
        DrawableCompat.setTint(drawable, Color.parseColor("#FFFFFF"))
        field.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, drawable, null)
    }

    private fun resetSetPinLockToggles() {
        if (activityPinCardLayout() == null) return

        editPinVisible = false
        editPinSecondVisible = false

        listOf(editPin, editPinSecond).forEach { field ->
            field.setOnTouchListener(null)
            field.setCompoundDrawablesRelative(null, null, null, null)
            field.compoundDrawablePadding = 0

            val typeface = field.typeface
            field.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD
            field.typeface = typeface
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
