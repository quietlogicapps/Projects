package com.quietlogic.allisok.alarm.engine

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.quietlogic.allisok.R
import com.quietlogic.allisok.alarm.receiver.AlarmReceiver
import java.util.Calendar
import java.util.Locale

class AlarmScheduler(private val context: Context) {

    private val alarmManager: AlarmManager =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleExact(
        triggerAtMillis: Long,
        careItemId: Long,
        requestCode: Int,
        title: String = context.getString(R.string.alarm_default_title),
        text: String = context.getString(R.string.alarm_scheduler_default_text)
    ): Boolean {

        if (!PermissionGate.hasExactAlarmPermission(context)) {
            Log.d(
                "AllIsOK",
                "AlarmScheduler.scheduleExact blockedMissingPermission rc=$requestCode careItemId=$careItemId"
            )
            return false
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            !alarmManager.canScheduleExactAlarms()
        ) {
            Log.d(
                "AllIsOK",
                "AlarmScheduler.scheduleExact blockedBySystem rc=$requestCode careItemId=$careItemId"
            )
            return false
        }

        val pendingIntent = buildPendingIntent(
            triggerAtMillis = triggerAtMillis,
            careItemId = careItemId,
            requestCode = requestCode,
            title = title,
            text = text
        )

        Log.d(
            "AllIsOK",
            "AlarmScheduler.scheduleExact scheduling rc=$requestCode careItemId=$careItemId triggerAt=$triggerAtMillis"
        )

        alarmManager.cancel(pendingIntent)

        return try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
            Log.d(
                "AllIsOK",
                "AlarmScheduler.scheduleExact scheduled rc=$requestCode careItemId=$careItemId triggerAt=$triggerAtMillis"
            )
            true
        } catch (_: SecurityException) {
            Log.d(
                "AllIsOK",
                "AlarmScheduler.scheduleExact securityException rc=$requestCode careItemId=$careItemId"
            )
            false
        }
    }

    fun scheduleExactGrouped(
        triggerAtMillis: Long,
        requestCode: Int,
        careItemIds: LongArray,
        careItemNames: Array<String>,
        careItemInstructions: Array<String>,
        title: String = context.getString(R.string.alarm_default_title),
        text: String = context.getString(R.string.alarm_scheduler_default_text)
    ): Boolean {

        if (!PermissionGate.hasExactAlarmPermission(context)) {
            Log.d(
                "AllIsOK",
                "AlarmScheduler.scheduleExactGrouped blockedMissingPermission rc=$requestCode ids=${careItemIds.toList()}"
            )
            return false
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            !alarmManager.canScheduleExactAlarms()
        ) {
            Log.d(
                "AllIsOK",
                "AlarmScheduler.scheduleExactGrouped blockedBySystem rc=$requestCode ids=${careItemIds.toList()}"
            )
            return false
        }

        val pendingIntent = buildPendingIntentGrouped(
            triggerAtMillis = triggerAtMillis,
            requestCode = requestCode,
            careItemIds = careItemIds,
            careItemNames = careItemNames,
            careItemInstructions = careItemInstructions,
            title = title,
            text = text
        )

        Log.d(
            "AllIsOK",
            "AlarmScheduler.scheduleExactGrouped scheduling rc=$requestCode ids=${careItemIds.toList()} triggerAt=$triggerAtMillis"
        )

        alarmManager.cancel(pendingIntent)

        return try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
            Log.d(
                "AllIsOK",
                "AlarmScheduler.scheduleExactGrouped scheduled rc=$requestCode ids=${careItemIds.toList()} triggerAt=$triggerAtMillis"
            )
            true
        } catch (_: SecurityException) {
            Log.d(
                "AllIsOK",
                "AlarmScheduler.scheduleExactGrouped securityException rc=$requestCode"
            )
            false
        }
    }

    fun cancel(requestCode: Int) {
        val pendingIntent = buildPendingIntent(
            triggerAtMillis = 0L,
            careItemId = -1L,
            requestCode = requestCode,
            title = "",
            text = ""
        )
        Log.d(
            "AllIsOK",
            "AlarmScheduler.cancel rc=$requestCode"
        )
        alarmManager.cancel(pendingIntent)
    }

    fun cancelAll(requestCodes: List<Int>) {
        requestCodes.forEach { cancel(it) }
    }

    private fun buildPendingIntent(
        triggerAtMillis: Long,
        careItemId: Long,
        requestCode: Int,
        title: String,
        text: String
    ): PendingIntent {

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_CARE_ALARM
            putExtra(EXTRA_TITLE, title)
            putExtra(EXTRA_TEXT, text)
            putExtra(EXTRA_REQUEST_CODE, requestCode)
            putExtra(EXTRA_TIME_TEXT, formatTime(triggerAtMillis))
            putExtra(EXTRA_CARE_ITEM_ID, careItemId)
        }

        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE

        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            flags
        )
    }

    private fun buildPendingIntentGrouped(
        triggerAtMillis: Long,
        requestCode: Int,
        careItemIds: LongArray,
        careItemNames: Array<String>,
        careItemInstructions: Array<String>,
        title: String,
        text: String
    ): PendingIntent {

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_CARE_ALARM
            putExtra(EXTRA_TITLE, title)
            putExtra(EXTRA_TEXT, text)
            putExtra(EXTRA_REQUEST_CODE, requestCode)
            putExtra(EXTRA_TIME_TEXT, formatTime(triggerAtMillis))
            if (careItemIds.isNotEmpty()) {
                putExtra(EXTRA_CARE_ITEM_ID, careItemIds.first())
            }
            putExtra(EXTRA_CARE_ITEM_IDS, careItemIds)
            putExtra(EXTRA_CARE_ITEM_NAMES, careItemNames)
            putExtra(EXTRA_CARE_ITEM_INSTRUCTIONS, careItemInstructions)
        }

        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE

        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            flags
        )
    }

    private fun formatTime(timeMillis: Long): String {
        if (timeMillis <= 0L) return context.getString(R.string.alarm_time_default)

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
        const val EXTRA_CARE_ITEM_ID = "extra_care_item_id"
        const val EXTRA_CARE_ITEM_IDS = "extra_care_item_ids"
        const val EXTRA_CARE_ITEM_NAMES = "extra_care_item_names"
        const val EXTRA_CARE_ITEM_INSTRUCTIONS = "extra_care_item_instructions"
    }
}