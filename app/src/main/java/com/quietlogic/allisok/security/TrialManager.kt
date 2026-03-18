package com.quietlogic.allisok.security

import android.content.Context

object TrialManager {

    private const val PREFS_NAME = "trial_prefs"
    private const val KEY_TRIAL_START = "trial_start_timestamp"
    private const val KEY_PURCHASED = "is_purchased"
    private const val TRIAL_DURATION_MS = 72 * 60 * 60 * 1000L

    fun ensureTrialStarted(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (!prefs.contains(KEY_TRIAL_START)) {
            prefs.edit().putLong(KEY_TRIAL_START, System.currentTimeMillis()).apply()
        }
    }

    fun isTrialActive(context: Context): Boolean {
        if (isPurchased(context)) return true
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val start = prefs.getLong(KEY_TRIAL_START, 0L)
        if (start == 0L) return true
        return System.currentTimeMillis() - start < TRIAL_DURATION_MS
    }

    fun isPurchased(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_PURCHASED, false)
    }

    fun setPurchased(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_PURCHASED, true).apply()
    }

    fun getRemainingMs(context: Context): Long {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val start = prefs.getLong(KEY_TRIAL_START, 0L)
        if (start == 0L) return TRIAL_DURATION_MS
        val elapsed = System.currentTimeMillis() - start
        return maxOf(0L, TRIAL_DURATION_MS - elapsed)
    }
}
