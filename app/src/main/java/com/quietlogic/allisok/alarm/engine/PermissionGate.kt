package com.quietlogic.allisok.alarm.engine

import android.app.AlarmManager
import android.content.Context
import android.os.Build
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

    fun isFullyGranted(context: Context): Boolean {
        val notifications = hasNotificationPermission(context)
        val exactAlarms = hasExactAlarmPermission(context)
        return notifications && exactAlarms
    }

    fun needsNotificationPermission(context: Context): Boolean {
        return !hasNotificationPermission(context)
    }

    fun needsExactAlarmPermission(context: Context): Boolean {
        return !hasExactAlarmPermission(context)
    }
}