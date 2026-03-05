package com.quietlogic.allisok.alarm.engine

import android.content.Context
import android.util.Log
import java.util.Calendar
import com.quietlogic.allisok.alarm.engine.AlarmScheduler

class AlarmPlanner(private val context: Context) {

    fun scheduleSimpleTestAlarm() {

        val scheduler = AlarmScheduler(context)

        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MINUTE, 1)

        val triggerAtMillis = calendar.timeInMillis
        val requestCode = triggerAtMillis.hashCode()

        scheduler.scheduleExact(
            triggerAtMillis = triggerAtMillis,
            requestCode = requestCode,
            title = "Planner Test",
            text = "AlarmPlanner scheduled this"
        )

        Log.d(
            "AllIsOK",
            "AlarmPlanner scheduled test alarm for +1 minute"
        )
    }

    fun cancelSimpleTestAlarm(requestCode: Int) {

        val scheduler = AlarmScheduler(context)

        scheduler.cancel(requestCode)

        Log.d(
            "AllIsOK",
            "AlarmPlanner cancel called"
        )
    }
}