package com.quietlogic.allisok.security

import android.app.Activity
import android.content.Intent
import com.quietlogic.allisok.ui.pin.PinActivity

object LockGate {

    const val MODE_USER_UNLOCK = "MODE_USER_UNLOCK"
    const val MODE_ADMIN_UNLOCK = "MODE_ADMIN_UNLOCK"

    const val REQUEST_USER_UNLOCK = 1001
    const val REQUEST_ADMIN_UNLOCK = 1002

    fun requireUserUnlock(activity: Activity) {

        val prefs = PinPrefs(activity)

        if (!prefs.isUserPinEnabled()) {
            return
        }

        val intent = Intent(activity, PinActivity::class.java)
        intent.putExtra("mode", MODE_USER_UNLOCK)

        activity.startActivityForResult(
            intent,
            REQUEST_USER_UNLOCK
        )
    }

    fun requireAdminUnlock(activity: Activity) {

        val intent = Intent(activity, PinActivity::class.java)
        intent.putExtra("mode", MODE_ADMIN_UNLOCK)

        activity.startActivityForResult(
            intent,
            REQUEST_ADMIN_UNLOCK
        )
    }

}