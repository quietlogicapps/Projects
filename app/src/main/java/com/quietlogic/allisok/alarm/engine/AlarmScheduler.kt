package com.quietlogic.allisok.alarm.engine

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.quietlogic.allisok.alarm.receiver.AlarmReceiver
import java.util.Calendar
import java.util.Locale

class AlarmScheduler(private val context: Context) {

    private val alarmManager: AlarmManager =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleExact(
        triggerAtMillis: Long,
        requestCode: Int,
        title: String = "Reminder",
        text: String = "Care reminder"
    ): Boolean {

        // If Android 12+ and exact alarms are not allowed, we hard-fail.
        if (!PermissionGate.hasExactAlarmPermission(context)) {
            return false
        }

        val pendingIntent = buildPendingIntent(
            requestCode = requestCode,
            title = title,
            text = text,
            triggerAtMillis = triggerAtMillis
        )

        // Cancel any previous alarm with the same requestCode to avoid duplicates.
        alarmManager.cancel(pendingIntent)

        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT -> {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            }
            else -> {
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            }
        }

        return true
    }

    fun cancel(requestCode: Int) {
        val pendingIntent = buildPendingIntent(
            requestCode = requestCode,
            title = "x",
            text = "x",
            triggerAtMillis = 0L
        )
        alarmManager.cancel(pendingIntent)
    }

    fun cancelAll(requestCodes: List<Int>) {
        requestCodes.forEach { cancel(it) }
    }

    private fun buildPendingIntent(
        requestCode: Int,
        title: String,
        text: String,
        triggerAtMillis: Long
    ): PendingIntent {

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_CARE_ALARM
            putExtra(EXTRA_TITLE, title)
            putExtra(EXTRA_TEXT, text)
            putExtra(EXTRA_REQUEST_CODE, requestCode)
            putExtra(EXTRA_TIME_TEXT, formatTime(triggerAtMillis))
        }

        val flags = PendingIntent.FLAG_UPDATE_CURRENT or pendingIntentImmutableFlag()

        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            flags
        )
    }

    private fun pendingIntentImmutableFlag(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_IMMUTABLE
        } else {
            0
        }
    }

    private fun formatTime(timeMillis: Long): String {
        if (timeMillis <= 0L) return "--:--"

        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timeMillis

        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        return String.format(Locale.US, "%02d:%02d", hour, minute)
    }

    companion object {
        const val ACTION_CARE_ALARM = "com.quietlogic.allisok.ACTION_CARE_ALARM"
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_TEXT = "extra_text"
        const val EXTRA_REQUEST_CODE = "extra_request_code"
        const val EXTRA_TIME_TEXT = "extra_time_text"
    }
}