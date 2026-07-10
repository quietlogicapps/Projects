package com.quietlogic.allisok.ui.pin

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class PinKeyboardHelper(
    private val activity: AppCompatActivity,
    private val editPin: EditText,
    private val editPinSecond: EditText
) {

    fun attachCardPinImeInsets(cardPin: View) {
        ViewCompat.setOnApplyWindowInsetsListener(cardPin) { view, insets ->
            val imeHeight = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
            val navHeight = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
            val bottomPadding = if (imeHeight > 0) (imeHeight - navHeight) / 2 else 0
            view.translationY = -bottomPadding.toFloat()
            insets
        }
    }

    fun showKeyboard(view: View) {
        val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }

    fun hideKeyboard() {
        val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val token = activity.currentFocus?.windowToken ?: return
        imm.hideSoftInputFromWindow(token, 0)
    }

    fun clearFocusAndHideKeyboard() {
        editPin.clearFocus()
        editPinSecond.clearFocus()
        hideKeyboard()
    }
}
