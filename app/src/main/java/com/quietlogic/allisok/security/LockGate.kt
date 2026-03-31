package com.quietlogic.allisok.security

import android.app.Activity
import android.content.Intent
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.quietlogic.allisok.security.UserSession
import com.quietlogic.allisok.ui.pin.PinActivity

object LockGate {

    const val MODE_USER_UNLOCK = "MODE_USER_UNLOCK"
    const val MODE_ADMIN_UNLOCK = "MODE_ADMIN_UNLOCK"

    const val REQUEST_USER_UNLOCK = 1001
    const val REQUEST_ADMIN_UNLOCK = 1002

    private var isObserverRegistered = false
    private var isUserUnlockedForCurrentForeground = false

    fun requireUserUnlock(activity: Activity) {

        ensureObserverRegistered()

        val state = PinPrefs(activity).getState()

        // ❗ КРИТИЧНО: ако няма hash → няма PIN
        if (state.userPinHash.isNullOrBlank()) {
            return
        }

        if (!state.userPinEnabled) {
            return
        }

        if (isUserUnlockedForCurrentForeground) {
            return
        }

        if (UserSession.isActive(activity)) {
            markUserUnlocked()
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

    fun markUserUnlocked() {
        isUserUnlockedForCurrentForeground = true
    }

    private fun ensureObserverRegistered() {

        if (isObserverRegistered) return

        ProcessLifecycleOwner.get().lifecycle.addObserver(
            object : DefaultLifecycleObserver {
                override fun onStop(owner: LifecycleOwner) {
                    isUserUnlockedForCurrentForeground = false
                }
            }
        )

        isObserverRegistered = true
    }
}