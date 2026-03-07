package com.quietlogic.allisok.ui.pin

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.quietlogic.allisok.security.PinHasher
import com.quietlogic.allisok.security.PinPrefs
import com.quietlogic.allisok.security.PinValidator

class AdminPinChangeActivity : AppCompatActivity() {

    private lateinit var pinPrefs: PinPrefs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        pinPrefs = PinPrefs(this)
    }

    fun changeAdminPin(
        currentPin: String,
        newPin: String,
        confirmPin: String
    ): Boolean {

        val state = pinPrefs.getState()

        if (!PinHasher.verify(currentPin, state.adminPinHash)) {
            return false
        }

        if (!PinValidator.isValidFormat(newPin)) {
            return false
        }

        if (newPin != confirmPin) {
            return false
        }

        if (!PinValidator.isDifferentFromUser(newPin, state.userPinHash)) {
            return false
        }

        pinPrefs.setAdminPin(PinHasher.hash(newPin))

        return true
    }
}