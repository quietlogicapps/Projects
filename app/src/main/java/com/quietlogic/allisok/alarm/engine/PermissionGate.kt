package com.quietlogic.allisok.alarm.engine

import android.app.AlarmManager
import android.content.Context
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat

object PermissionGate {

    fun hasNotificationPermission(context: Context): Boolean {
        return NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    fun hasExactAlarmPermission(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            return true
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        return alarmManager.canScheduleExactAlarms()
    }

    fun hasOverlayPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else {
            true
        }
    }

    fun needsOverlayPermission(context: Context): Boolean {
        return !hasOverlayPermission(context)
    }

    fun isFullyGranted(context: Context): Boolean {
        val notifications = hasNotificationPermission(context)
        val exactAlarms = hasExactAlarmPermission(context)
        val overlay = hasOverlayPermission(context)
        return notifications && exactAlarms && overlay
    }

    fun needsNotificationPermission(context: Context): Boolean {
        return !hasNotificationPermission(context)
    }

    fun needsExactAlarmPermission(context: Context): Boolean {
        return !hasExactAlarmPermission(context)
    }
}