package com.quietlogic.allisok.security

import android.content.Context
import androidx.appcompat.app.AlertDialog

object AdminGate {

    fun requireAdmin(
        context: Context,
        onAllowed: () -> Unit
    ) {
        val state = PinPrefs(context).getState()

        if (!state.adminPinEnabled) {
            AlertDialog.Builder(context)
                .setTitle("Admin required")
                .setMessage("Admin PIN is not set yet.")
                .setPositiveButton("OK", null)
                .show()
            return
        }

        onAllowed()
    }
}