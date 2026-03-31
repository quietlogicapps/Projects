package com.quietlogic.allisok.security

import android.content.Context

object UserSession {

    private const val PREFS_NAME = "user_session_prefs"
    private const val KEY_ACTIVE = "session_active"

    fun isActive(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_ACTIVE, false)
    }

    fun start(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_ACTIVE, true).apply()
    }

    fun stop(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_ACTIVE, false).apply()
    }
}
