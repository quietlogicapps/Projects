package com.quietlogic.allisok.security

import android.content.Context
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.quietlogic.allisok.R

object AdminGate {

    fun requireAdmin(
        context: Context,
        onAllowed: () -> Unit
    ) {
        val state = PinPrefs(context).getState()

        if (!state.adminPinEnabled) {
            MaterialAlertDialogBuilder(context, R.style.AllIsOK_MaterialAlertDialog)
                .setTitle(context.getString(R.string.admin_required_title))
                .setMessage(context.getString(R.string.admin_required_message))
                .setPositiveButton(context.getString(R.string.dialog_ok), null)
                .show()
            return
        }

        onAllowed()
    }
}