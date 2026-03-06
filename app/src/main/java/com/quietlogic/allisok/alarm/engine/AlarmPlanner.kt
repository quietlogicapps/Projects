package com.quietlogic.allisok.alarm.engine

import android.content.Context
import android.util.Log
import android.widget.Toast
import java.time.LocalTime
import java.util.Calendar

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

    fun scheduleSimpleTestAlarm() {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MINUTE, 1)

        val triggerAtMillis = calendar.timeInMillis
        val requestCode = triggerAtMillis.hashCode()

        val ok = scheduler.scheduleExact(
            triggerAtMillis = triggerAtMillis,
            requestCode = requestCode,
            title = "Planner Test",
            text = "AlarmPlanner scheduled this"
        )

        val msg = if (ok) "TEST ALARM SCHEDULED (+1 min)" else "FAILED: EXACT ALARM NOT ALLOWED"
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()

        Log.d("AllIsOK", "AlarmPlanner scheduleSimpleTestAlarm ok=$ok triggerAtMillis=$triggerAtMillis requestCode=$requestCode")
    }

    fun cancelSimpleTestAlarm(requestCode: Int) {
        scheduler.cancel(requestCode)
        Toast.makeText(context, "TEST ALARM CANCELLED", Toast.LENGTH_SHORT).show()
        Log.d("AllIsOK", "AlarmPlanner cancelSimpleTestAlarm requestCode=$requestCode")
    }
}