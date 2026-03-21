package com.quietlogic.allisok.security

import android.content.Context
import androidx.appcompat.app.AlertDialog
import com.quietlogic.allisok.R

object AdminGate {

    fun requireAdmin(
        context: Context,
        onAllowed: () -> Unit
    ) {
        val state = PinPrefs(context).getState()

        if (!state.adminPinEnabled) {
            AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.admin_required_title))
                .setMessage(context.getString(R.string.admin_required_message))
                .setPositiveButton(context.getString(R.string.dialog_ok), null)
                .show()
            return
        }

        onAllowed()
    }
}