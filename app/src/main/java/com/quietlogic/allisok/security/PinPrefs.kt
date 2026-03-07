package com.quietlogic.allisok.security

import android.content.Context
import android.content.SharedPreferences

class PinPrefs(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("pin_prefs", Context.MODE_PRIVATE)

    companion object {

        private const val KEY_USER_PIN_ENABLED = "user_pin_enabled"
        private const val KEY_USER_PIN_HASH = "user_pin_hash"

        private const val KEY_ADMIN_PIN_ENABLED = "admin_pin_enabled"
        private const val KEY_ADMIN_PIN_HASH = "admin_pin_hash"
    }

    fun getState(): PinState {

        val userEnabled = prefs.getBoolean(KEY_USER_PIN_ENABLED, false)
        val userHash = prefs.getString(KEY_USER_PIN_HASH, null)

        val adminEnabled = prefs.getBoolean(KEY_ADMIN_PIN_ENABLED, false)
        val adminHash = prefs.getString(KEY_ADMIN_PIN_HASH, null)

        return PinState(
            userPinEnabled = userEnabled,
            userPinHash = userHash,
            adminPinEnabled = adminEnabled,
            adminPinHash = adminHash
        )
    }

    fun isUserPinEnabled(): Boolean {
        return prefs.getBoolean(KEY_USER_PIN_ENABLED, false)
    }

    fun setUserPin(hash: String) {

        prefs.edit()
            .putBoolean(KEY_USER_PIN_ENABLED, true)
            .putString(KEY_USER_PIN_HASH, hash)
            .apply()
    }

    fun setAdminPin(hash: String) {

        prefs.edit()
            .putBoolean(KEY_ADMIN_PIN_ENABLED, true)
            .putString(KEY_ADMIN_PIN_HASH, hash)
            .apply()
    }

    fun disableUserPin() {

        prefs.edit()
            .putBoolean(KEY_USER_PIN_ENABLED, false)
            .apply()
    }

}