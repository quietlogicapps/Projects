package com.quietlogic.allisok.alarm.engine

import android.content.Context
import java.time.LocalTime

class AlarmPlanner(private val context: Context) {

    private val scheduler = AlarmScheduler(context)

    fun buildRequestCode(careItemId: Long, time: LocalTime): Int {
        return "${careItemId}_${time}".hashCode()
    }

    fun cancelCareItemAlarms(careItemId: Long, times: List<LocalTime>) {
        times.forEach { time ->
            scheduler.cancel(buildRequestCode(careItemId, time))
        }
    }

    // Real alarm scheduling will be implemented later
    fun scheduleCareAlarm(
        triggerAtMillis: Long,
        requestCode: Int,
        title: String,
        text: String
    ): Boolean {
        return scheduler.scheduleExact(
            triggerAtMillis = triggerAtMillis,
            requestCode = requestCode,
            title = title,
            text = text
        )
    }
}