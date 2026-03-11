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

    fun scheduleCareAlarm(
        triggerAtMillis: Long,
        careItemId: Long,
        requestCode: Int,
        title: String,
        text: String
    ): Boolean {
        return scheduler.scheduleExact(
            triggerAtMillis = triggerAtMillis,
            careItemId = careItemId,
            requestCode = requestCode,
            title = title,
            text = text
        )
    }
}