package com.quietlogic.allisok.alarm.engine

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.quietlogic.allisok.alarm.receiver.AlarmReceiver

class AlarmScheduler(private val context: Context) {

    private val alarmManager: AlarmManager =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleExact(
        triggerAtMillis: Long,
        requestCode: Int,
        title: String = "Reminder",
        text: String = "Care reminder"
    ): Boolean {

        if (!PermissionGate.hasExactAlarmPermission(context)) {
            return false
        }

        val pendingIntent = buildPendingIntent(requestCode, title, text)

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
        val pendingIntent = buildPendingIntent(requestCode, "x", "x")
        alarmManager.cancel(pendingIntent)
    }

    fun cancelAll(requestCodes: List<Int>) {
        requestCodes.forEach { cancel(it) }
    }

    private fun buildPendingIntent(
        requestCode: Int,
        title: String,
        text: String
    ): PendingIntent {

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_CARE_ALARM
            putExtra(EXTRA_TITLE, title)
            putExtra(EXTRA_TEXT, text)
            putExtra(EXTRA_REQUEST_CODE, requestCode)
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

    companion object {
        const val ACTION_CARE_ALARM = "com.quietlogic.allisok.ACTION_CARE_ALARM"
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_TEXT = "extra_text"
        const val EXTRA_REQUEST_CODE = "extra_request_code"
    }
}