package com.quietlogic.allisok.security

import android.content.Context
import com.quietlogic.allisok.reminders.TrialReminderScheduler

object TrialManager {

    private const val PREFS_NAME = "trial_prefs"
    private const val KEY_TRIAL_START = "trial_start_timestamp"
    private const val KEY_PURCHASED = "is_purchased"
    private const val TRIAL_DURATION_MS = 72 * 60 * 60 * 1000L
    private const val REMINDER_OFFSET_MS = 24L * 60L * 60L * 1000L
    private const val KEY_TRIAL_REMINDER_SET = "trial_reminder_set"

    fun ensureTrialStarted(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (!prefs.contains(KEY_TRIAL_START)) {
            prefs.edit().putLong(KEY_TRIAL_START, System.currentTimeMillis()).apply()
            scheduleReminderIfNeeded(context)
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
        TrialReminderScheduler.cancelTrialReminder(context)
    }

    fun getRemainingMs(context: Context): Long {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val start = prefs.getLong(KEY_TRIAL_START, 0L)
        if (start == 0L) return TRIAL_DURATION_MS
        val elapsed = System.currentTimeMillis() - start
        return maxOf(0L, TRIAL_DURATION_MS - elapsed)
    }

    private fun scheduleReminderIfNeeded(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (prefs.getBoolean(KEY_TRIAL_REMINDER_SET, false)) return

        val trialStart = prefs.getLong(KEY_TRIAL_START, 0L)
        if (trialStart == 0L) return

        val reminderTime = trialStart + TRIAL_DURATION_MS - REMINDER_OFFSET_MS
        if (reminderTime > System.currentTimeMillis()) {
            TrialReminderScheduler.scheduleTrialReminder(context, reminderTime)
            prefs.edit().putBoolean(KEY_TRIAL_REMINDER_SET, true).apply()
        }
    }
}
