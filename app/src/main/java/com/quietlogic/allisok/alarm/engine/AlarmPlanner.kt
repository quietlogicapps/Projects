package com.quietlogic.allisok.alarm.engine

import android.content.Context
import android.util.Log
import android.widget.Toast
import java.util.Calendar

class AlarmPlanner(private val context: Context) {

    fun scheduleSimpleTestAlarm() {
        val scheduler = AlarmScheduler(context)

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
        val scheduler = AlarmScheduler(context)
        scheduler.cancel(requestCode)
        Toast.makeText(context, "TEST ALARM CANCELLED", Toast.LENGTH_SHORT).show()
        Log.d("AllIsOK", "AlarmPlanner cancelSimpleTestAlarm requestCode=$requestCode")
    }
}