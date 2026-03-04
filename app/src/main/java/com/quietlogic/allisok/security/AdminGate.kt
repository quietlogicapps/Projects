package com.quietlogic.allisok.security

import android.content.Context
import androidx.appcompat.app.AlertDialog

object AdminGate {

    private const val PREFS = "admin_gate"
    private const val KEY_ADMIN_PIN_SET = "admin_pin_set"

    fun isAdminPinSet(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_ADMIN_PIN_SET, false)
    }

    fun markAdminPinAsSet(context: Context) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_ADMIN_PIN_SET, true).apply()
    }

    fun requireAdmin(
        context: Context,
        onAllowed: () -> Unit
    ) {
        if (!isAdminPinSet(context)) {
            showNotReadyDialog(context)
            return
        }

        // PIN UI ще се добави в T16.
        // Засега само пропускаме, ако PIN е "сетнат".
        onAllowed()
    }

    private fun showNotReadyDialog(context: Context) {
        AlertDialog.Builder(context)
            .setTitle("Admin required")
            .setMessage("Admin PIN is not set yet.")
            .setPositiveButton("OK", null)
            .show()
    }
}