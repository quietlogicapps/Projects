package com.quietlogic.allisok.ui.pin

import android.content.Context
import androidx.annotation.StringRes
import com.quietlogic.allisok.R
import com.quietlogic.allisok.security.AdminSession
import com.quietlogic.allisok.security.LockGate
import com.quietlogic.allisok.security.PinHasher
import com.quietlogic.allisok.security.PinPrefs
import com.quietlogic.allisok.security.PinValidator
import com.quietlogic.allisok.security.UserSession

sealed class PinActionResult {
    data object AdminUnlockSuccess : PinActionResult()
    data object UserUnlockSuccess : PinActionResult()
    data class ValidationError(@StringRes val messageResId: Int) : PinActionResult()
    data class WrongPin(@StringRes val messageResId: Int, val retryFirstField: Boolean) : PinActionResult()
    data class GoToNextScreen(val nextScreen: String) : PinActionResult()
    data object UserPinSaveSuccess : PinActionResult()
    data object AdminPinSaveSuccess : PinActionResult()
}

class PinActionExecutor(
    private val pinPrefs: PinPrefs,
    private val context: Context
) {

    fun enterPin(inputPin: String, unlockMode: String): PinActionResult {
        val state = pinPrefs.getState()

        val ok = if (unlockMode == LockGate.MODE_ADMIN_UNLOCK) {
            PinHasher.verify(inputPin, state.adminPinHash)
        } else {
            PinHasher.verify(inputPin, state.userPinHash)
        }

        if (ok) {
            if (unlockMode == LockGate.MODE_ADMIN_UNLOCK) {
                AdminSession.start()
                return PinActionResult.AdminUnlockSuccess
            }

            LockGate.markUserUnlocked()
            UserSession.start(context)
            return PinActionResult.UserUnlockSuccess
        }

        return if (unlockMode == LockGate.MODE_ADMIN_UNLOCK) {
            PinActionResult.WrongPin(R.string.pin_error_wrong_admin, retryFirstField = true)
        } else {
            PinActionResult.WrongPin(R.string.pin_error_wrong, retryFirstField = true)
        }
    }

    fun setUserPin(pin: String, confirmPin: String): PinActionResult {
        return saveUserPin(pin, confirmPin)
    }

    fun changeUserPin(pin: String, confirmPin: String): PinActionResult {
        return saveUserPin(pin, confirmPin)
    }

    fun setAdminPin(pin: String, confirmPin: String): PinActionResult {
        return saveAdminPin(pin, confirmPin)
    }

    fun verifyAdminPinStep1(currentAdminPin: String): PinActionResult {
        val state = pinPrefs.getState()

        return if (PinHasher.verify(currentAdminPin, state.adminPinHash)) {
            PinActionResult.GoToNextScreen(PinActivity.SCREEN_CHANGE_ADMIN_PIN_STEP_2)
        } else {
            PinActionResult.WrongPin(R.string.pin_error_wrong_admin, retryFirstField = false)
        }
    }

    fun changeAdminPinStep2(pin: String, confirmPin: String): PinActionResult {
        return saveAdminPin(pin, confirmPin)
    }

    private fun saveUserPin(pin: String, confirmPin: String): PinActionResult {
        val state = pinPrefs.getState()

        if (!PinValidator.isValidFormat(pin)) {
            return PinActionResult.ValidationError(R.string.pin_error_format)
        }

        if (pin != confirmPin) {
            return PinActionResult.ValidationError(R.string.pin_error_mismatch)
        }

        if (!PinValidator.isDifferentFromAdmin(pin, state.adminPinHash)) {
            return PinActionResult.ValidationError(R.string.pin_error_same)
        }

        pinPrefs.setUserPin(PinHasher.hash(pin))
        LockGate.markUserUnlocked()
        return PinActionResult.UserPinSaveSuccess
    }

    private fun saveAdminPin(pin: String, confirmPin: String): PinActionResult {
        val state = pinPrefs.getState()

        if (!PinValidator.isValidFormat(pin)) {
            return PinActionResult.ValidationError(R.string.pin_error_format_admin)
        }

        if (pin != confirmPin) {
            return PinActionResult.ValidationError(R.string.pin_error_mismatch)
        }

        if (!PinValidator.isDifferentFromUser(pin, state.userPinHash)) {
            return PinActionResult.ValidationError(R.string.pin_error_same)
        }

        pinPrefs.setAdminPin(PinHasher.hash(pin))
        AdminSession.start()
        return PinActionResult.AdminPinSaveSuccess
    }
}
