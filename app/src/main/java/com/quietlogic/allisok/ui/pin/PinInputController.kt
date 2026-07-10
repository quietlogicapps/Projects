package com.quietlogic.allisok.ui.pin

import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.View
import android.widget.EditText

class PinInputController(
    private val editPin: EditText,
    private val editPinSecond: EditText,
    private val keyboardHelper: PinKeyboardHelper,
    private val screenRenderer: PinScreenRenderer,
    private val currentScreenProvider: () -> String,
    private val unlockModeProvider: () -> String
) {

    fun setupInputs() {
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
                    keyboardHelper.showKeyboard(editPinSecond)
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
            updateFieldHint(
                editPin,
                screenRenderer.firstRowLabel(currentScreenProvider(), unlockModeProvider()),
                hasFocus
            )
        }

        editPinSecond.setOnFocusChangeListener { _, hasFocus ->
            updateFieldHint(
                editPinSecond,
                screenRenderer.secondRowLabel(currentScreenProvider()),
                hasFocus
            )
        }
    }

    private fun updateFieldHint(field: EditText, label: String, hasFocus: Boolean) {
        field.hint = if (hasFocus && field.text.isNullOrEmpty()) "— — — —" else label
    }
}
